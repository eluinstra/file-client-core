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

import java.time.Instant;

import dev.luin.file.client.core.Retries;
import dev.luin.file.client.core.ScheduleTime;
import dev.luin.file.client.core.download.DownloadStatus.Status;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.Timestamp;
import dev.luin.file.client.core.file.Url;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.val;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadTask
{
	FileId fileId;
	@NonNull
	Url url;
	TimeFrame validTimeFrame;
	@NonNull
	Timestamp timestamp;
	@With
	@NonNull
	DownloadStatus status;
	@With
	@NonNull
	ScheduleTime scheduleTime;
	@With
	Retries retries;

	static DownloadTask of(FileId fileId, Url url, Instant startDate, Instant endDate)
	{
		val now = Instant.now();
		val scheduleTime = new ScheduleTime(startDate != null ? startDate : now);
		return new DownloadTask(fileId,url,new TimeFrame(startDate,endDate),new Timestamp(now),new DownloadStatus(Status.CREATED,now),scheduleTime,new Retries());
	}

	public DownloadTask(FileId fileId, @NonNull Url url, Instant startDate, Instant endDate, @NonNull Timestamp timestamp, @NonNull Status status, @NonNull Instant statusTime, @NonNull ScheduleTime scheduleTime, Retries retries)
	{
		this.fileId = fileId;
		this.url = url;
		this.validTimeFrame = new TimeFrame(startDate,endDate);
		this.timestamp = timestamp;
		this.status = new DownloadStatus(status,statusTime);
		this.scheduleTime = scheduleTime;
		this.retries = retries;
	}

}
