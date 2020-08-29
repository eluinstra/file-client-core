package dev.luin.fc.core.upload;

import java.io.IOException;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;

import dev.luin.fc.core.file.FileSystem;
import dev.luin.fc.core.transaction.TransactionException;
import dev.luin.fc.core.transaction.TransactionTemplate;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
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
public class UploadTaskHandler
{
	private static final int chunkSize = 1024 * 1024 * 100;
	private static int maxRetries = 5;
	@NonNull
	SSLFactoryManager sslFactoryManager;
	@NonNull
	FileSystem fs;
	@NonNull
	TransactionTemplate transactionTemplate;
	@NonNull
	UploadTaskManager uploadTaskManager;

	@Scheduled(fixedDelayString = "${uploadTaskHandler.delay}")
	public void run()
	{
		val task = uploadTaskManager.getNextTask();
		task.map(t -> Try.of(() -> run(t)).onFailure(e -> log.error("",e)));
	}

	public Future<Void> run(UploadTask task) throws ProtocolException, IOException
	{
		log.info("Start task {}",task);
		val file = fs.findFile(task.getFileId()).getOrElseThrow(() -> new IllegalStateException("File " + task.getFileId() + " not found"));
		val client = new Client(sslFactoryManager.getSslSocketFactory());
		client.setUploadCreationURL(task.getCreationUrl());
		client.enableResuming(uploadTaskManager);
		val upload = Try.of(() -> new TusUpload(file.getFile())).get();
		upload.setFingerprint(task.getFileId().toString());
		log.info("Uploading {}",file);
		val executor = new TusExecutor()
		{
			@Override
			protected void makeAttempt() throws ProtocolException, IOException
			{
				val uploader = client.resumeOrCreateUpload(upload);
				uploader.setChunkSize(chunkSize);
				do
				{
					if (log.isDebugEnabled())
						log.debug("Upload {} at {}%",file.getId(),getProgress(upload,uploader));
				} while (uploader.uploadChunk() > -1);
				val newFile = file.withUrl(uploader.getUploadURL());
				Runnable runnable = () ->
				{
					try
					{
						fs.updateFile(newFile);
						uploadTaskManager.createSucceededTask(task);
						uploader.finish();
					}
					catch (ProtocolException | IOException e)
					{
						throw new TransactionException(e);
					}
				};
				transactionTemplate.executeTransaction(runnable);
				log.info("Uploaded {}",newFile);
			}
		};
		try
		{
			if (!executor.makeAttempts())
			{
				if (task.getRetries() < maxRetries)
					uploadTaskManager.createNextTask(task);
				else
					uploadTaskManager.createFailedTask(task);
			}
		}
		catch (Exception e)
		{
			uploadTaskManager.createNextTask(task);
		}
		log.info("Finished task {}",task);
		return new AsyncResult<Void>(null);
	}

	private double getProgress(final TusUpload upload, TusUploader uploader)
	{
		val totalBytes = upload.getSize();
		val bytesUploaded = uploader.getOffset();
		return (double)bytesUploaded / totalBytes * 100;
	}
}
