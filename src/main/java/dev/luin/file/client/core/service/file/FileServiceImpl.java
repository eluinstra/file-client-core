/*
 * Copyright 2020 E.Luinstra
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.luin.file.client.core.service.file;

import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.service.NotFoundException;
import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.File;
import dev.luin.file.client.core.service.model.FileInfo;
import dev.luin.file.client.core.service.model.NewFSFileFromFsImpl;
import io.vavr.control.Try;
import jakarta.activation.DataHandler;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
public class FileServiceImpl implements FileService
{
	private static final NotFoundException FILE_NOT_FOUND_EXCEPTION = new NotFoundException("File not found!");
	@NonNull
	FileSystem fs;
	java.nio.file.Path sharedFs;

	@NonNull

	@GET
	@Path("{id}")
	@Produces(MediaType.MULTIPART_FORM_DATA)
	public MultipartBody downloadFileRest(@PathParam("id") Long id) throws ServiceException
	{
		return toMultipartBody(downloadFile(id));
	}

	public MultipartBody toMultipartBody(File file)
	{
		val attachments = new LinkedList<Attachment>();
		attachments.add(new Attachment("sha256Checksum", "text/plain", file.getSha256Checksum()));
		attachments.add(new Attachment("file", file.getContent(), new MultivaluedHashMap<>()));
		return new MultipartBody(attachments, true);
	}

	@Override
	public File downloadFile(@PathParam("id") Long id) throws ServiceException
	{
		log.debug("getFile {}", id);
		return Try
				.of(
						() -> fs.findFile(new FileId(id))
								.filter(FSFile::isCompleted)
								.peek(f -> log.info("Retreived file {}", f))
								.map(toFile())
								.getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION))
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private Function<? super FSFile, ? extends File> toFile()
	{
		return f -> new File(f, new DataHandler(fs.toDecryptedDataSource(f)));
	}

	@GET
	@Path("/fs/{id}/{filename}")
	@Override
	public FileInfo downloadFileToFs(@PathParam("id") @NonNull Long id, @PathParam("filename") @NonNull String filename) throws ServiceException
	{
		log.debug("getFileInfo {}", id);
		java.nio.file.Path validatedFilename =
				Try.of(() -> NewFSFileFromFsImpl.validateFilename(filename, sharedFs)).getOrElseThrow(ServiceException.defaultExceptionProvider);
		return Try
				.of(
						() -> fs.findFile(new FileId(id))
								.filter(FSFile::isCompleted)
								.peek(fs.decryptToFile(validatedFilename))
								.peek(f -> log.info("Retreived file {}", f))
								.map(FileInfo::new)
								.getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION))
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("{id}/info")
	@Override
	public FileInfo getFileInfo(@PathParam("id") Long id) throws ServiceException
	{
		log.debug("getFileInfo {}", id);
		return Try.of(() ->
		{
			val fsFile = fs.findFile(new FileId(id));
			return fsFile.map(f -> new FileInfo(f)).getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION);
		}).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("")
	@Override
	public List<FileInfo> getFiles() throws ServiceException
	{
		log.debug("getFiles");
		return Try.of(() ->
		{
			val fsFile = fs.getFiles();
			return fsFile.map(f -> new FileInfo(f)).asJava();
		}).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}
}
