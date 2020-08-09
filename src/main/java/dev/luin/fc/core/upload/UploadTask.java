package dev.luin.fc.core.upload;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
public class UploadTask
{
	@With
	Long fileId;
	@NonNull
	URL creationUrl;
	@NonNull
	@With
	Instant scheduleTime;
	@With
	int retries;

	public static UploadTask of(long fileId, String createUrl)
	{
		try
		{
			return new UploadTask(fileId,new URL(createUrl),Instant.now(),0);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("CreateUrl " + createUrl + " is not a valid URL");
		}
	}
}
