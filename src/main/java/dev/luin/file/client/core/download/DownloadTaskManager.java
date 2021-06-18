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

import dev.luin.file.client.core.Retries;
import dev.luin.file.client.core.ScheduleTime;
import dev.luin.file.client.core.download.DownloadStatus.Status;
import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.file.Url;
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

	public Option<DownloadTask> getTask(FileId fileId)
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

	public DownloadTask createTask(FileId fileId, Url url)
	{
		return createTask(fileId,url,null,null);
	}

	public DownloadTask createTask(FileId fileId, Url url, Instant startDate, Instant endDate)
	{
		val task = DownloadTask.of(fileId,url,startDate,endDate);
		return downloadTaskDAO.insert(task);
	}

	public DownloadTask createNextTask(DownloadTask task)
	{
		val retries = task.getRetries().increment();
		Option<ScheduleTime> nextScheduleTime = getNextScheduleTime(task,retries);
		val result = nextScheduleTime
				.map(t -> task
					.withScheduleTime(t)
					.withRetries(retries))
					.getOrElse(task.withStatus(new DownloadStatus(Status.FAILED)));
		downloadTaskDAO.update(result);
		return result;
	}

	private Option<ScheduleTime> getNextScheduleTime(DownloadTask task, final Retries retries)
	{
		val result = task.getScheduleTime().plus(Duration.ofMinutes((retries.getValue() > retryMaxMultiplier ? retryMaxMultiplier : retries.getValue()) * retryInterval));
		return task.getValidTimeFrame().getEndDate() != null && result.isAfter(task.getValidTimeFrame().getEndDate()) ? Option.none() : Option.of(result);
	}

	public DownloadTask createSucceededTask(DownloadTask task)
	{
		val result = task.withStatus(new DownloadStatus(Status.SUCCEEDED));
		downloadTaskDAO.update(result);
		return result;
	}

	public DownloadTask createFailedTask(DownloadTask task)
	{
		val result = task.withStatus(new DownloadStatus(Status.FAILED));
		downloadTaskDAO.update(result);
		return result;
	}

	public boolean deleteTask(FileId fileId)
	{
		return downloadTaskDAO.delete(fileId) > 0;
	}
}
