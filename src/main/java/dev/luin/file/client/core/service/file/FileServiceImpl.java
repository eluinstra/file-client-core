/**
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.activation.DataHandler;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.service.NotFoundException;
import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.File;
import dev.luin.file.client.core.service.model.FileDataSource;
import dev.luin.file.client.core.service.model.FileInfo;
import dev.luin.file.client.core.service.model.NewFSFileFromFsImpl;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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
		attachments.add(new Attachment("sha256Checksum","text/plain",file.getSha256Checksum()));
		attachments.add(new Attachment("file",file.getContent(),new MultivaluedHashMap<>()));
		return new MultipartBody(attachments,true);
	}

	@Override
	public File downloadFile(@PathParam("id") Long id) throws ServiceException
	{
		log.debug("getFile {}",id);
		return Try.of(() -> fs.findFile(new FileId(id))
					.filter(FSFile::isCompleted)
					.peek(f -> log.info("Retreived file {}",f))
					.map(f -> new File(f,new DataHandler(FileDataSource.of(f))))
					.getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION))
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("/fs/{id}/{filename}")
	@Override
	public FileInfo downloadFileToFs(@PathParam("id") @NonNull Long id, @PathParam("filename") @NonNull String filename) throws ServiceException
	{
		log.debug("getFileInfo {}",id);
		java.nio.file.Path validatedFilename =
				Try.of(() -> NewFSFileFromFsImpl.validateFilename(filename,sharedFs)).getOrElseThrow(ServiceException.defaultExceptionProvider);
		return Try.of(() -> fs.findFile(new FileId(id))
				.filter(FSFile::isCompleted)
				.peek(writeToFile(validatedFilename))
				.peek(f -> log.info("Retreived file {}",f))
				.map(FileInfo::new)
				.getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION)).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private Consumer<FSFile> writeToFile(java.nio.file.Path filename)
	{
		// TODO handle exceptions
		return f -> Try.withResources(() -> new FileInputStream(f.getFile()),() -> new FileOutputStream(filename.toFile())).of(IOUtils::copyLarge);
	}

	@GET
	@Path("{id}/info")
	@Override
	public FileInfo getFileInfo(@PathParam("id") Long id) throws ServiceException
	{
		log.debug("getFileInfo {}",id);
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
