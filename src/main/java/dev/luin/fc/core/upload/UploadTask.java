package dev.luin.fc.core.upload;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UploadTask
{
	@With
	Long fileId;
	@NonNull
	URL creationUrl;
	@NonNull
	@With
	UploadStatus status;
	@NonNull
	@With
	Instant scheduleTime;
	@With
	int retries;

	static UploadTask of(long fileId, String createUrl)
	{
		try
		{
			return new UploadTask(fileId,new URL(createUrl),UploadStatus.CREATED,Instant.now(),0);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("CreateUrl " + createUrl + " is not a valid URL");
		}
	}
}
