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

import java.time.Duration;
import java.time.Instant;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class DownloadTaskManager
{
	@NonNull
	DownloadTaskDAO downloadTaskDAO;
	int retryInterval;
	int retryMaxMultiplier;

	public Option<DownloadTask> getTask(long fileId)
	{
		return downloadTaskDAO.getTask(fileId);
	}

	public Option<DownloadTask> getNextTask()
	{
		return downloadTaskDAO.getNextTask();
	}

	public Seq<DownloadTask> getTasks(List<DownloadStatus> statuses)
	{
		return statuses.length() == 0 ? downloadTaskDAO.getTasks() : downloadTaskDAO.getTasks(statuses);
	}

	public DownloadTask createTask(long fileId, String url)
	{
		return createTask(fileId,url,null,null);
	}

	public DownloadTask createTask(long fileId, String url, Instant startDate, Instant endDate)
	{
		val task = DownloadTask.of(fileId,url,startDate,endDate);
		return downloadTaskDAO.insert(task);
	}

	public DownloadTask createNextTask(DownloadTask task)
	{
		val retries = task.getRetries() + 1;
		Option<Instant> nextScheduleTime = getNextScheduleTime(task,retries);
		val result = nextScheduleTime.map(t -> task
				.withScheduleTime(t)
				.withRetries(retries))
				.getOrElse(task.withStatus(DownloadStatus.FAILED)
						.withStatusTime(Instant.now()));
		downloadTaskDAO.update(result);
		return result;
	}

	private Option<Instant> getNextScheduleTime(DownloadTask task, final int retries)
	{
		val result = task.getScheduleTime().plus(Duration.ofMinutes((retries > retryMaxMultiplier ? retryMaxMultiplier : retries) * retryInterval));
		return result.isAfter(task.getEndDate()) ? Option.none() : Option.of(result);
	}

	public DownloadTask createSucceededTask(DownloadTask task)
	{
		val result = task.withStatus(DownloadStatus.SUCCEEDED)
				.withStatusTime(Instant.now());
		downloadTaskDAO.update(result);
		return result;
	}

	public DownloadTask createFailedTask(DownloadTask task)
	{
		val result = task.withStatus(DownloadStatus.FAILED)
				.withStatusTime(Instant.now());
		downloadTaskDAO.update(result);
		return result;
	}

	public boolean deleteTask(long fileId)
	{
		return downloadTaskDAO.delete(fileId) > 0;
	}
}
