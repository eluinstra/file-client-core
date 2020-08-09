package dev.luin.fc.core.download;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
public class DownloadTask
{
	@NonNull
	URL url;
	Instant startDate;
	Instant endDate;
	Long fileId;
	@NonNull
	@With
	Instant scheduleTime;
	@With
	int retries;

	public static DownloadTask of(long fileId, String createUrl)
	{
		try
		{
			return new DownloadTask(new URL(createUrl),null,null,fileId,Instant.now(),0);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("CreateUrl " + createUrl + " is not a valid URL");
		}
	}
}
