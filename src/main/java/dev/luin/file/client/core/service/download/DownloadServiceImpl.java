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
package dev.luin.file.client.core.service.download;

import java.io.IOException;
import java.time.Instant;
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

import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import dev.luin.file.client.core.download.DownloadStatus;
import dev.luin.file.client.core.download.DownloadTaskManager;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.file.Url;
import dev.luin.file.client.core.service.NotFoundException;
import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.DownloadTask;
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
public class DownloadServiceImpl implements DownloadService
{
	private static final NotFoundException FILE_NOT_FOUND_EXCEPTION = new NotFoundException("File not found!");
	private static final NotFoundException TASK_NOT_FOUND_EXCEPTION = new NotFoundException("Task not found!");
	@NonNull
	FileSystem fs;
	@NonNull
	DownloadTaskManager downloadTaskManager;

  @POST
	@Path("")
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
	@Path("{fileId}")
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
	@Path("")
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
	@Path("{fileId}")
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

}
