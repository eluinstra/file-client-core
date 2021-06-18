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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.activation.DataHandler;

import org.springframework.transaction.annotation.Transactional;

import dev.luin.file.client.core.download.DownloadStatus;
import dev.luin.file.client.core.download.DownloadTaskManager;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.FSFile;
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
class FileServiceImpl implements FileService
{
	@NonNull
	FileSystem fs;
	@NonNull
	UploadTaskManager uploadTaskManager;
	@NonNull
	DownloadTaskManager downloadTaskManager;

	@Override
	@Transactional("dataSourceTransactionManager")
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

	@Override
	public UploadTask getUploadTask(Long fileId) throws ServiceException
	{
		log.debug("getUploadTask {}",fileId);
		return Try.of(() ->
		{
			val task = uploadTaskManager.getTask(new FileId(fileId)).getOrElseThrow(() -> new ServiceException("Task " + fileId + " not found"));
			return new UploadTask(task);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<UploadTask> getUploadTasks(List<UploadStatus> status) throws ServiceException
	{
		log.debug("getUploadTasks {}",status);
		return Try.of(() -> 
		{
			return uploadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
					.map(t -> new UploadTask(t))
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	@Transactional("dataSourceTransactionManager")
	public void deleteUploadTask(Long fileId) throws ServiceException
	{
		log.debug("deleteUploadTask {}",fileId);
		Try.of(() -> 
		{
			try
			{
				FileId id = new FileId(fileId);
				val fsFile = fs.findFile(id).getOrElseThrow(() -> new FileNotFoundException("File " + fileId + " not found"));
				fs.deleteFile(fsFile,true);
				uploadTaskManager.deleteTask(id);
				log.info("Deleted uploadTask {}",fileId);
			}
			catch (FileNotFoundException e)
			{
				throw new ServiceException(e);
			}
			return null;
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	@Transactional("dataSourceTransactionManager")
	public DownloadTask downloadFile(String url, Instant startDate, Instant endDate) throws ServiceException
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

	@Override
	public DownloadTask getDownloadTask(Long fileId) throws ServiceException
	{
		log.debug("getDownloadTask {}",fileId);
		return Try.of(() ->
		{
			val task = downloadTaskManager.getTask(new FileId(fileId)).getOrElseThrow(() -> new ServiceException("Task " + fileId + " not found"));
			return new DownloadTask(task);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<DownloadTask> getDownloadTasks(List<DownloadStatus> status) throws ServiceException
	{
		log.debug("getDownloadTasks");
		return Try.of(() -> 
		{
			return downloadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
					.map(t -> new DownloadTask(t))
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	@Transactional("dataSourceTransactionManager")
	public void deleteDownloadTask(Long fileId) throws ServiceException
	{
		log.debug("deleteDownloadTask {}",fileId);
		Try.of(() -> 
		{
			try
			{
				val fsFile = fs.findFile(new FileId(fileId)).getOrElseThrow(() -> new FileNotFoundException("File " + fileId + " not found"));
				fs.deleteFile(fsFile,true);
				downloadTaskManager.deleteTask(new FileId(fileId));
				log.info("Deleted downloadTask {}",fileId);
			}
			catch (FileNotFoundException e)
			{
				throw new ServiceException(e);
			}
			return null;
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
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
					.getOrElseThrow(() -> new ServiceException("File " + id + " not found!"));
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public FileInfo getFileInfo(Long id) throws ServiceException
	{
		log.debug("getFileInfo {}",id);
		return Try.of(() ->
		{
			val fsFile = fs.findFile(new FileId(id));
			return fsFile.map(f -> new FileInfo(f))
					.getOrElseThrow(() -> new ServiceException("File " + id + " not found!"));
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

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
