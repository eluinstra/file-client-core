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
package dev.luin.fc.core.service;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import javax.activation.DataHandler;

import dev.luin.fc.core.download.DownloadTask;
import dev.luin.fc.core.download.DownloadTaskManager;
import dev.luin.fc.core.file.FSFile;
import dev.luin.fc.core.file.FileSystem;
import dev.luin.fc.core.service.model.File;
import dev.luin.fc.core.service.model.FileInfo;
import dev.luin.fc.core.service.model.FileInfoMapper;
import dev.luin.fc.core.service.model.FileMapper;
import dev.luin.fc.core.transaction.TransactionTemplate;
import dev.luin.fc.core.upload.UploadTask;
import dev.luin.fc.core.upload.UploadTaskManager;
import io.vavr.CheckedFunction0;
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
	TransactionTemplate transactionTemplate;
	@NonNull
	FileSystem fs;
	@NonNull
	UploadTaskManager uploadTaskManager;
	@NonNull
	DownloadTaskManager downloadTaskManager;

	@Override
	public FileInfo uploadFile(@NonNull final File file, final String url) throws ServiceException
	{
			return Try.of(() -> 
					{
						CheckedFunction0<FileInfo> producer = () ->
						{
							val creationUrl = new URL(url);
							val result = createFile(file);
							val task = new UploadTask(result.getId(),creationUrl,Instant.now(),0);
							uploadTaskManager.createTask(task);
							return FileInfoMapper.INSTANCE.toFileInfo(result);
						};
						return transactionTemplate.executeTransactionWithResult(producer);
					})
					.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public FileInfo downloadFile(final String url) throws ServiceException
	{
		return Try.of(() -> 
				{
					val u = new URL(url);
					val result = fs.createEmptyFile(url);
					val task = new DownloadTask(u,null,null,result.getId(),Instant.now(),0);
					downloadTaskManager.createTask(task);
					return FileInfoMapper.INSTANCE.toFileInfo(result);
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
					.flatMap(f -> 
							dataSource.map(d -> FileMapper.INSTANCE.toFile(f,new DataHandler(d))))
					.getOrElseThrow(() -> new ServiceException("File " + id + " not found!"));
		})
		.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public List<FileInfo> getFileInfo() throws ServiceException
	{
		return Try.of(() -> 
				{
					val fsFile = fs.getFiles();
					return fsFile.map(f -> FileInfoMapper.INSTANCE.toFileInfo(f))
							.asJava();
				})
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	@Override
	public void deleteFile(final Long id, final Boolean force) throws ServiceException
	{
		Try.of(() -> 
				{
					val fsFile = fs.findFile(id);
					val deleted = fsFile.map(f -> fs.deleteFile(fsFile.get(),force != null && force))
							.getOrElseThrow(() -> new ServiceException("File " + id + " not found!"));
					if (!deleted)
						throw new ServiceException("Unable to delete " + id + "!");
					return null;
				})
				.getOrElseThrow(ServiceException.defaultExceptionProvider);
	}

	private FSFile createFile(final File file) throws IOException
	{
		return fs.createFile(file.getName(),file.getContentType(),file.getSha256Checksum(),file.getStartDate(),file.getEndDate(),file.getContent().getInputStream());
	}
}
