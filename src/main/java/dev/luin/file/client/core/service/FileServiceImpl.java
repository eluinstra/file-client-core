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
package dev.luin.file.client.core.service;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import dev.luin.file.client.core.download.DownloadStatus;
import dev.luin.file.client.core.download.DownloadTaskManager;
import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.file.Url;
import dev.luin.file.client.core.service.model.DownloadTask;
import dev.luin.file.client.core.service.model.File;
import dev.luin.file.client.core.service.model.FileDataSource;
import dev.luin.file.client.core.service.model.FileInfo;
import dev.luin.file.client.core.service.model.NewFSFileImpl;
import dev.luin.file.client.core.service.model.NewFile;
import dev.luin.file.client.core.service.model.UploadTask;
import dev.luin.file.client.core.upload.UploadStatus;
import dev.luin.file.client.core.upload.UploadTaskManager;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
public class FileServiceImpl implements FileService
{
	private static final NotFoundException FILE_NOT_FOUND_EXCEPTION = new NotFoundException("File not found!");
	private static final NotFoundException TASK_NOT_FOUND_EXCEPTION = new NotFoundException("Task not found!");
	@NonNull
	FileSystem fs;
	@NonNull
	UploadTaskManager uploadTaskManager;
	@NonNull
	DownloadTaskManager downloadTaskManager;

	@POST
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public UploadTask uploadFile(
			@Multipart("creationUrl") String creationUrl,
			@Multipart(value = "sha256Checksum", required = false) String sha256Checksum,
			@Multipart("file") Attachment file) throws ServiceException
	{
		return uploadFile(creationUrl,new NewFile(sha256Checksum,file.getDataHandler()));
	}

	@Override
	public UploadTask uploadFile(String creationUrl, NewFile file) throws ServiceException
	{
		log.debug("uploadFile creationUrl={}, {}",creationUrl,file);
		return Try.of(() -> 
		{
			try
			{
				val fsFile = createFile(new File(file));
				val task = uploadTaskManager.createTask(fsFile.getId(),new Url(creationUrl));
				log.info("Created uploadTask {}",task);
				return new UploadTask(task);
			}
			catch (Exception e)
			{
				throw new ServiceException(e);
			}
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("upload/{fileId}")
	@Override
	public UploadTask getUploadTask(@PathParam("fileId") Long fileId) throws ServiceException
	{
		log.debug("getUploadTask {}",fileId);
		return Try.of(() ->
		{
			val task = uploadTaskManager.getTask(new FileId(fileId)).getOrElseThrow(() -> TASK_NOT_FOUND_EXCEPTION);
			return new UploadTask(task);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("upload")
	@Override
	public List<UploadTask> getUploadTasks(@QueryParam("status") List<UploadStatus.Status> status) throws ServiceException
	{
		log.debug("getUploadTasks {}",status);
		return Try.of(() -> 
		{
			return uploadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
					.map(UploadTask::new)
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@DELETE
	@Path("upload/{fileId}")
	@Override
	public void deleteUploadTask(@PathParam("fileId") Long fileId) throws ServiceException
	{
		log.debug("deleteUploadTask {}",fileId);
		Try.of(() -> 
		{
			FileId id = new FileId(fileId);
			val fsFile = fs.findFile(id).getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION);
			uploadTaskManager.deleteTask(id);
			fs.deleteFile(fsFile,true);
			log.info("Deleted uploadTask {}",fileId);
			return null;
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@POST
	@Path("download")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Override
	public DownloadTask downloadFile(
		@Multipart("url") String url,
		@Multipart(value = "startDate", required = false) Instant startDate,
		@Multipart(value = "endDate", required = false) Instant endDate) throws ServiceException
	{
		log.debug("downloadFile {}",url);
		return Try.of(() -> 
		{
			try
			{
				val fsFile = fs.createEmptyFile(url);
				val task = downloadTaskManager.createTask(fsFile.getId(),new Url(url),startDate,endDate);
				log.info("Created downloadTask {}",task);
				return new DownloadTask(task);
			}
			catch (IOException e)
			{
				throw new ServiceException(e);
			}
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("download/{fileId}")
	@Override
	public DownloadTask getDownloadTask(@PathParam("fileId") Long fileId) throws ServiceException
	{
		log.debug("getDownloadTask {}",fileId);
		return Try.of(() ->
		{
			val task = downloadTaskManager.getTask(new FileId(fileId)).getOrElseThrow(() -> TASK_NOT_FOUND_EXCEPTION);
			return new DownloadTask(task);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("download")
	@Override
	public List<DownloadTask> getDownloadTasks(@QueryParam("status") List<DownloadStatus.Status> status) throws ServiceException
	{
		log.debug("getDownloadTasks");
		return Try.of(() -> 
		{
			return downloadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
					.map(DownloadTask::new)
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@DELETE
	@Path("download/{fileId}")
	@Override
	public void deleteDownloadTask(@PathParam("fileId") Long fileId) throws ServiceException
	{
		log.debug("deleteDownloadTask {}",fileId);
		Try.of(() -> 
		{
			val fsFile = fs.findFile(new FileId(fileId)).getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION);
			downloadTaskManager.deleteTask(new FileId(fileId));
			fs.deleteFile(fsFile,true);
			log.info("Deleted downloadTask {}",fileId);
			return null;
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.MULTIPART_FORM_DATA)
	public MultipartBody getFileRest(@PathParam("id") Long id) throws ServiceException
	{
		return toMultipartBody(getFile(id));
	}

	public MultipartBody toMultipartBody(File file)
	{
		val attachments = new LinkedList<Attachment>();
		attachments.add(new Attachment("sha256Checksum","text/plain",file.getSha256Checksum()));
		attachments.add(new Attachment("file",file.getContent(),new MultivaluedHashMap<>()));
		return new MultipartBody(attachments,true);  
	}

	@Override
	public File getFile(Long id) throws ServiceException
	{
		log.debug("getFile {}",id);
		return Try.of(() ->
		{
			val fsFile = fs.findFile(new FileId(id));
			val dataSource = fsFile.map(FileDataSource::of);
			return fsFile.filter(f -> f.isCompleted())
					.peek(f -> log.info("Retreived file {}",f))
					.flatMap(f -> dataSource.map(d -> new File(f,new DataHandler(d))))
					.getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
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
			return fsFile.map(f -> new FileInfo(f))
					.getOrElseThrow(() -> FILE_NOT_FOUND_EXCEPTION);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
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
			return fsFile.map(f -> new FileInfo(f))
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private FSFile createFile(final File file) throws IOException
	{
		return fs.createNewFile(NewFSFileImpl.of(file));
	}
}
