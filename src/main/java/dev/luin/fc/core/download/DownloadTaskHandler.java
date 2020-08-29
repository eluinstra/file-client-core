package dev.luin.fc.core.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;

import dev.luin.fc.core.file.FileSystem;
import dev.luin.fc.core.transaction.TransactionTemplate;
import dev.luin.fc.core.upload.SSLFactoryManager;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusExecutor;
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
	private static int maxRetries = 5;
	@NonNull
	SSLFactoryManager sslFactoryManager;
	@NonNull
	FileSystem fs;
	@NonNull
	TransactionTemplate transactionTemplate;
	@NonNull
	DownloadTaskManager downloadTaskManager;

	@Scheduled(fixedDelayString = "${downloadTaskHandler.delay}")
	public void run()
	{
		val task = downloadTaskManager.getNextTask();
		task.map(t -> Try.of(() -> run(t)).onFailure(e -> log.error("",e)));
	}

	public Future<Void> run(DownloadTask task) throws IOException
	{
		log.info("Start task {}",task);
		val file = fs.findFile(task.getFileId()).getOrElseThrow(() -> new IllegalStateException("File " + task.getFileId() + " not found"));
		log.info("Downloading {}",file);
		val executor = new TusExecutor()
		{
			@Override
			protected void makeAttempt() throws ProtocolException, IOException
			{
				var connection = createConnection(task.getUrl());
				connection.setRequestMethod("HEAD");
				val contentLength = connection.getContentLengthLong();
				var fileLength = file.getFile().length();
				while (fileLength < contentLength)
				{
					connection = createConnection(task.getUrl());
				  connection.setRequestProperty("Range","bytes=" + fileLength + "-" + contentLength);
					try (val output = new FileOutputStream(file.getFile(),true))
					{
						IOUtils.copyLarge(connection.getInputStream(),output);
					}
					fileLength = file.getFile().length();
				}
				if (fileLength == contentLength)
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
