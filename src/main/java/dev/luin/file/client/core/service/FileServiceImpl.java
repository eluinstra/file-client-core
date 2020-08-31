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
import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.service.model.DownloadTask;
import dev.luin.file.client.core.service.model.DownloadTaskMapper;
import dev.luin.file.client.core.service.model.File;
import dev.luin.file.client.core.service.model.FileInfo;
import dev.luin.file.client.core.service.model.FileInfoMapper;
import dev.luin.file.client.core.service.model.FileMapper;
import dev.luin.file.client.core.service.model.UploadTask;
import dev.luin.file.client.core.service.model.UploadTaskMapper;
import dev.luin.file.client.core.upload.UploadStatus;
import dev.luin.file.client.core.upload.UploadTaskManager;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;

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
	public UploadTask uploadFile(File file, String creationUrl) throws ServiceException
	{
		return Try.of(() -> 
		{
			try
			{
				val fsFile = createFile(file);
				val task = uploadTaskManager.createTask(fsFile.getId(),creationUrl);
				return UploadTaskMapper.INSTANCE.toUploadTask(task);
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
		return Try.of(() ->
		{
			val task = uploadTaskManager.getTask(fileId).getOrElseThrow(() -> new ServiceException("Task " + fileId + " not found"));
			return UploadTaskMapper.INSTANCE.toUploadTask(task);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<UploadTask> getUploadTasks(List<UploadStatus> status) throws ServiceException
	{
		return Try.of(() -> 
		{
			return uploadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
					.map(t -> UploadTaskMapper.INSTANCE.toUploadTask(t))
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	@Transactional("dataSourceTransactionManager")
	public void deleteUploadTask(Long fileId) throws ServiceException
	{
		Try.of(() -> 
		{
			try
			{
				val fsFile = fs.findFile(fileId).getOrElseThrow(() -> new FileNotFoundException("File " + fileId + " not found"));
				fs.deleteFile(fsFile,true);
				uploadTaskManager.deleteTask(fileId);
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
		return Try.of(() -> 
		{
			try
			{
				val fsFile = fs.createEmptyFile(url);
				val task = downloadTaskManager.createTask(fsFile.getId(),url,startDate,endDate);
				return DownloadTaskMapper.INSTANCE.toDownloadTask(task);
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
		return Try.of(() ->
		{
			val task = downloadTaskManager.getTask(fileId).getOrElseThrow(() -> new ServiceException("Task " + fileId + " not found"));
			return DownloadTaskMapper.INSTANCE.toDownloadTask(task);
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<DownloadTask> getDownloadTasks(List<DownloadStatus> status) throws ServiceException
	{
		return Try.of(() -> 
		{
			return downloadTaskManager.getTasks(status != null ? io.vavr.collection.List.ofAll(status) : io.vavr.collection.List.empty())
					.map(t -> DownloadTaskMapper.INSTANCE.toDownloadTask(t))
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	@Transactional("dataSourceTransactionManager")
	public void deleteDownloadTask(Long fileId) throws ServiceException
	{
		Try.of(() -> 
		{
			try
			{
				val fsFile = fs.findFile(fileId).getOrElseThrow(() -> new FileNotFoundException("File " + fileId + " not found"));
				fs.deleteFile(fsFile,true);
				downloadTaskManager.deleteTask(fileId);
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
		return Try.of(() ->
		{
			val fsFile = fs.findFile(id);
			val dataSource = fsFile.map(f -> fs.createDataSource(f));
			return fsFile.filter(f -> f.isCompleted())
					.flatMap(f -> dataSource.map(d -> FileMapper.INSTANCE.toFile(f,new DataHandler(d))))
					.getOrElseThrow(() -> new ServiceException("File " + id + " not found!"));
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public FileInfo getFileInfo(Long id) throws ServiceException
	{
		return Try.of(() ->
		{
			val fsFile = fs.findFile(id);
			return fsFile.map(f -> FileInfoMapper.INSTANCE.toFileInfo(f))
					.getOrElseThrow(() -> new ServiceException("File " + id + " not found!"));
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<FileInfo> getFiles() throws ServiceException
	{
		return Try.of(() -> 
		{
			val fsFile = fs.getFiles();
			return fsFile.map(f -> FileInfoMapper.INSTANCE.toFileInfo(f))
					.asJava();
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private FSFile createFile(final File file) throws IOException
	{
		return fs.createFile(file.getName(),file.getContentType(),file.getSha256Checksum(),file.getContent().getInputStream());
	}
}
