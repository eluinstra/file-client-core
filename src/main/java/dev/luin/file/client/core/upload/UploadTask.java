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
package dev.luin.file.client.core.upload;

import java.time.Instant;

import dev.luin.file.client.core.Retries;
import dev.luin.file.client.core.ScheduleTime;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.Timestamp;
import dev.luin.file.client.core.file.Url;
import dev.luin.file.client.core.upload.UploadStatus.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.val;

@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UploadTask
{
	@With
	FileId fileId;
	@NonNull
	Url creationUrl;
	@NonNull
	Timestamp timestamp;
	@With
	@NonNull
	UploadStatus status;
	@With
	@NonNull
	ScheduleTime scheduleTime;
	@With
	Retries retries;

	static UploadTask of(FileId fileId, Url createUrl)
	{
		val now = Instant.now();
		return new UploadTask(fileId,createUrl,new Timestamp(now),new UploadStatus(Status.CREATED,now),new ScheduleTime(now),new Retries());
	}

	public UploadTask(FileId fileId, @NonNull Url creationUrl, @NonNull Timestamp timestamp, @NonNull Status status, @NonNull Instant statusTime, @NonNull ScheduleTime scheduleTime, Retries retries)
	{
		this.fileId = fileId;
		this.creationUrl = creationUrl;
		this.timestamp = timestamp;
		this.status = new UploadStatus(status,statusTime);
		this.scheduleTime = scheduleTime;
		this.retries = retries;
	}
}
