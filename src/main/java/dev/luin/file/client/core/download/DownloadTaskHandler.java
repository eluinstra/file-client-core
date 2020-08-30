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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;

import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.upload.SSLFactoryManager;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusExecutor;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class DownloadTaskHandler
{
	@NonNull
	SSLFactoryManager sslFactoryManager;
	@NonNull
	FileSystem fs;
	@NonNull
	DownloadTaskManager downloadTaskManager;
	int maxRetries;

	@Scheduled(fixedDelayString = "${downloadTaskHandler.delay}")
	public void run()
	{
		val task = downloadTaskManager.getNextTask();
		task.map(t -> Try.of(() -> run(t)).onFailure(e -> log.error("",e)));
	}

	public Future<Void> run(DownloadTask task) throws IOException
	{
		log.info("Start task {}",task);
		val executor = new TusExecutor()
		{
			@Override
			protected void makeAttempt() throws ProtocolException, IOException
			{
				var file = fs.findFile(task.getFileId()).getOrElseThrow(() -> new IllegalStateException("File " + task.getFileId() + " not found"));
				log.info("Downloading {}",file);
				var connection = createConnection(task.getUrl());
				connection.setRequestMethod("HEAD");
				val filename = HeaderValue.of(connection.getHeaderField("Content-Disposition"))
						.flatMap(h -> h.getParams().get("filename"))
						.getOrNull();
				file = file.withLength(getContentLength(connection).getOrElseThrow(() -> new IllegalStateException("No Content-Length found")))
						.withContentType(connection.getContentType())
						.withName(filename);
				while (!file.isCompleted())
				{
					connection = createConnection(task.getUrl());
				  connection.setRequestProperty("Range","bytes=" + file.getFileLength() + "-" + file.getLength());
				  file = fs.append(file,connection.getInputStream());
				}
				if (file.isCompleted())
					downloadTaskManager.createSucceededTask(task);
				log.info("Downloaded {}",file);
			}
		};
		try
		{
			if (!executor.makeAttempts())
			{
				if (task.getRetries() < maxRetries)
					downloadTaskManager.createNextTask(task);
				else
					downloadTaskManager.createFailedTask(task);
			}
		}
		catch (Exception e)
		{
			downloadTaskManager.createNextTask(task);
		}
		log.info("Finished task {}",task);
		return new AsyncResult<Void>(null);
	}

	private Option<Long> getContentLength(java.net.HttpURLConnection connection)
	{
		val result = connection.getContentLengthLong();
		return result != -1 ? Option.of(result) : Option.none();
	}

	private java.net.HttpURLConnection createConnection(final URL url) throws IOException
	{
		val connection = (HttpURLConnection)url.openConnection();
		if (connection instanceof HttpsURLConnection)
		{
			HttpsURLConnection secureConnection = (HttpsURLConnection)connection;
			secureConnection.setSSLSocketFactory(sslFactoryManager.getSslSocketFactory());
	  }
		return connection;
	}
}
