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
	Instant timestamp;
	@With
	@NonNull
	DownloadStatus status;
	@With
	@NonNull
	Instant statusTime;
	@With
	@NonNull
	Instant scheduleTime;
	@With
	int retries;

	static DownloadTask of(long fileId, String url, Instant startDate, Instant endDate)
	{
		try
		{
			Instant now = Instant.now();
			Instant scheduleTime = startDate != null ? startDate : now;
			return new DownloadTask(fileId,new URL(url),startDate,endDate,now,DownloadStatus.CREATED,now,scheduleTime,0);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("CreateUrl " + url + " is not a valid URL");
		}
	}
}
