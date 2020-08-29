package dev.luin.fc.core.download;

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
public class DownloadTask
{
	long fileId;
	@NonNull
	URL url;
	Instant startDate;
	Instant endDate;
	@NonNull
	@With
	DownloadStatus status;
	@NonNull
	@With
	Instant scheduleTime;
	@With
	int retries;

	static DownloadTask of(long fileId, String url)
	{
		try
		{
			return new DownloadTask(fileId,new URL(url),null,null,DownloadStatus.CREATED,Instant.now(),0);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("CreateUrl " + url + " is not a valid URL");
		}
	}
}
