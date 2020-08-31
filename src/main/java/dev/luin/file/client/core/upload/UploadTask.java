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
	Instant timestamp;
	@With
	@NonNull
	UploadStatus status;
	@With
	@NonNull
	Instant statusTime;
	@With
	@NonNull
	Instant scheduleTime;
	@With
	int retries;

	static UploadTask of(long fileId, String createUrl)
	{
		try
		{
			Instant now = Instant.now();
			return new UploadTask(fileId,new URL(createUrl),now,UploadStatus.CREATED,now,now,0);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("CreateUrl " + createUrl + " is not a valid URL");
		}
	}
}
