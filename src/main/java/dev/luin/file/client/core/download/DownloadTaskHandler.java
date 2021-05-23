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
package dev.luin.file.client.core.download;

import java.io.IOException;

import org.springframework.scheduling.annotation.Scheduled;

import dev.luin.file.client.core.file.FileSystem;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusExecutor;
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
public class DownloadTaskHandler
{
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@AllArgsConstructor
	private static class DownloadTaskExecutor extends TusExecutor
	{
		@NonNull
		FileSystem fs;
		@NonNull
		HttpClient client;
		@NonNull
		DownloadTask task;
		
		@Override
		protected void makeAttempt() throws ProtocolException, IOException
		{
			val file = fs.findFile(task.getFileId()).getOrElseThrow(() -> new IllegalStateException("File " + task.getFileId() + " not found"));
			client.download(file,task.getUrl());
		}
	}

	@NonNull
	FileSystem fs;
	@NonNull
	HttpClient client;
	@NonNull
	DownloadTaskManager downloadTaskManager;
	int maxRetries;

	@Scheduled(fixedDelayString = "${downloadTaskHandler.delay}")
	public void run()
	{
		val task = downloadTaskManager.getNextTask();
		task.map(t -> Try.of(() -> handle(t)).onFailure(e -> log.error("",e)));
	}

	private DownloadTask handle(DownloadTask task) throws IOException
	{
		log.info("Start task {}",task);
		val executor = new DownloadTaskExecutor(fs,client,task);
		val newTask = handleTask(executor,task);
		log.info("Finished task {}\nCreated task {}",task,newTask);
		return newTask;
	}

	private DownloadTask handleTask(TusExecutor executor, DownloadTask task)
	{
		try
		{
			if (!executor.makeAttempts())
			{
				if (task.getRetries() < maxRetries)
					return downloadTaskManager.createNextTask(task);
				else
					return downloadTaskManager.createFailedTask(task);
			}
			else
				return downloadTaskManager.createSucceededTask(task);
		}
		catch (Exception e)
		{
			return downloadTaskManager.createNextTask(task);
		}
	}
}
