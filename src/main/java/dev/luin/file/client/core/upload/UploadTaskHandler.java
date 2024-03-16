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
package dev.luin.file.client.core.upload;

import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.file.Url;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UploadTaskHandler
{
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@AllArgsConstructor
	private static class UploadTaskExecutor extends TusExecutor
	{
		@NonNull
		SSLFactoryManager sslFactoryManager;
		@NonNull
		FileSystem fs;
		@NonNull
		UploadTaskManager uploadTaskManager;
		@NonNull
		UploadTask task;

		@Override
		protected void makeAttempt() throws ProtocolException, IOException
		{
			val file = fs.findFile(task.getFileId()).getOrElseThrow(() -> new IllegalStateException("File " + task.getFileId() + " not found"));
			val client = createClient();
			val upload = createUpload(file);
			log.info("Uploading {}", file);
			val uploader = client.resumeOrCreateUpload(upload);
			do
			{
				if (log.isDebugEnabled())
					log.debug("Upload {} at {}%", file, getProgress(upload, uploader));
			} while (uploader.uploadChunk() > -1);
			val newFile = file.withUrl(new Url(uploader.getUploadURL()));
			fs.updateFile(newFile);
			uploader.finish();
			log.info("Uploaded {}", newFile);
		}

		private Client createClient()
		{
			val client = new Client(sslFactoryManager.getSslSocketFactory());
			client.setUploadCreationURL(task.getCreationUrl().toURL());
			client.enableResuming(uploadTaskManager);
			return client;
		}

		private TusUpload createUpload(final FSFile file)
		{
			val upload = Try.of(() -> new TusUpload(file.getFile())).get();
			upload.setFingerprint(task.getFileId().getStringValue());
			upload.setMetadata(createMetaData(file));
			return upload;
		}

		private Map<String, String> createMetaData(FSFile file)
		{
			val result = new HashMap<String, String>();
			result.put("filename", file.getName().getValue());
			result.put("Content-Type", file.getContentType().getValue());
			return result;
		}

		private double getProgress(final TusUpload upload, TusUploader uploader)
		{
			val totalBytes = upload.getSize();
			val bytesUploaded = uploader.getOffset();
			return (double)bytesUploaded / totalBytes * 100;
		}
	}

	@NonNull
	SSLFactoryManager sslFactoryManager;
	@NonNull
	FileSystem fs;
	@NonNull
	UploadTaskManager uploadTaskManager;
	int maxRetries;

	@Scheduled(fixedDelayString = "${uploadTaskHandler.delay}")
	public void run()
	{
		val task = uploadTaskManager.getNextTask();
		task.map(t -> Try.of(() -> handle(t)).onFailure(e -> log.error("", e)));
	}

	private UploadTask handle(UploadTask task) throws ProtocolException, IOException
	{
		log.info("Start task {}", task);
		val executor = new UploadTaskExecutor(sslFactoryManager, fs, uploadTaskManager, task);
		val newTask = handleTask(executor, task);
		log.info("Finished task {}\nCreated task {}", task, newTask);
		return newTask;
	}

	private UploadTask handleTask(TusExecutor executor, UploadTask task)
	{
		try
		{
			if (!executor.makeAttempts())
			{
				if (task.getRetries().getValue() < maxRetries)
					return uploadTaskManager.createNextTask(task);
				else
					return uploadTaskManager.createFailedTask(task);
			}
			else
				return uploadTaskManager.createSucceededTask(task);
		}
		catch (Exception e)
		{
			log.error("", e);
			return uploadTaskManager.createNextTask(task);
		}
	}
}
