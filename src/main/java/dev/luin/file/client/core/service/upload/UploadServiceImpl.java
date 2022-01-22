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
package dev.luin.file.client.core.service.upload;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.file.Filename;
import dev.luin.file.client.core.file.Url;
import dev.luin.file.client.core.service.NotFoundException;
import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.NewFSFileFromFsImpl;
import dev.luin.file.client.core.service.model.NewFSFileImpl;
import dev.luin.file.client.core.service.model.NewFile;
import dev.luin.file.client.core.service.model.NewFileFromFs;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
public class UploadServiceImpl implements UploadService
{
	private static final NotFoundException FILE_NOT_FOUND_EXCEPTION = new NotFoundException("File not found!");
	private static final NotFoundException TASK_NOT_FOUND_EXCEPTION = new NotFoundException("Task not found!");
	@NonNull
	FileSystem fs;
	@NonNull
	java.nio.file.Path sharedFs;
	@NonNull
	UploadTaskManager uploadTaskManager;

	@POST
	@Path("")
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
				val fsFile = createFile(file);
				val task = uploadTaskManager.createTask(fsFile.getId(),new Url(creationUrl));
				log.info("Created uploadTask {}",task);
				return new UploadTask(task);
			}
			catch (Exception e)
			{
				throw new ServiceException(e);
			}
		}).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@POST
	@Path("/fs")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public UploadTask uploadFileFromFs(
			@Multipart("creationUrl") String creationUrl,
			@Multipart(value = "sha256Checksum", required = false) String sha256Checksum,
			@Multipart("filename") String filename) throws ServiceException
	{
		return uploadFileFromFs(creationUrl,new NewFileFromFs(sha256Checksum,new Filename(filename)));
	}
		
	@Override
	public UploadTask uploadFileFromFs(String creationUrl, NewFileFromFs file) throws ServiceException
	{
		log.debug("uploadFile creationUrl={}, {}",creationUrl,file);
		return Try.of(() ->
		{
			try
			{
				val fsFile = createFile(file);
				val task = uploadTaskManager.createTask(fsFile.getId(),new Url(creationUrl));
				log.info("Created uploadTask {}",task);
				return new UploadTask(task);
			}
			catch (Exception e)
			{
				throw new ServiceException(e);
			}
		}).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("{fileId}")
	@Override
	public UploadTask getUploadTask(@PathParam("fileId") Long fileId) throws ServiceException
	{
		log.debug("getUploadTask {}",fileId);
		return Try.of(() ->
		{
			val task = uploadTaskManager.getTask(new FileId(fileId)).getOrElseThrow(() -> TASK_NOT_FOUND_EXCEPTION);
			return new UploadTask(task);
		}).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@GET
	@Path("")
	@Override
	public List<UploadTask> getUploadTasks(@QueryParam("status") List<UploadStatus.Status> status) throws ServiceException
	{
		log.debug("getUploadTasks {}",status);
		return Try.of(() -> uploadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
				.map(UploadTask::new)
				.asJava()).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@DELETE
	@Path("{fileId}")
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
		}).getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private FSFile createFile(final NewFile file) throws IOException
	{
		return fs.createNewFile(NewFSFileImpl.of(file));
	}

	private FSFile createFile(final NewFileFromFs file) throws IOException
	{
		return fs.createNewFile(NewFSFileFromFsImpl.of(file, sharedFs));
	}

}
