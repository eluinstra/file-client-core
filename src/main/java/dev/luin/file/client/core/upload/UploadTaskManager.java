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

import java.net.URL;
import java.time.Duration;

import io.tus.java.client.TusURLStore;
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
public class UploadTaskManager implements TusURLStore
{
	@NonNull
	UploadTaskDAO uploadTaskDAO;
	@NonNull
	TusURLStore tusDAO;
	int retryInterval;
	int retryMaxMultiplier;

	public Option<UploadTask> getTask(long fileId)
	{
		return uploadTaskDAO.getTask(fileId);
	}

	public Option<UploadTask> getNextTask()
	{
		return uploadTaskDAO.getNextTask();
	}

	public Seq<UploadTask> getTasks(List<UploadStatus> statuses)
	{
		return statuses.length() == 0 ? uploadTaskDAO.getTasks() : uploadTaskDAO.getTasks(statuses);
	}

	public UploadTask createTask(long fileId, String creationUrl)
	{
		val task = UploadTask.of(fileId,creationUrl);
		return uploadTaskDAO.insert(task);
	}

	public UploadTask createNextTask(UploadTask task)
	{
		int retries = task.getRetries() + 1;
		val result = task
				.withScheduleTime(task.getScheduleTime().plus(Duration.ofMinutes((retries > retryMaxMultiplier ? retryMaxMultiplier : retries) * retryInterval)))
				.withRetries(retries);
		uploadTaskDAO.update(result);
		return result;
	}

	public UploadTask createSucceededTask(UploadTask task)
	{
		val result = task.withStatus(UploadStatus.SUCCEEDED);
		uploadTaskDAO.update(result);
		return result;
	}

	public UploadTask createFailedTask(UploadTask task)
	{
		val result = task.withStatus(UploadStatus.FAILED);
		uploadTaskDAO.update(result);
		return result;
	}

	public boolean deleteTask(long fileId)
	{
		return uploadTaskDAO.delete(fileId) > 0;
	}

	@Override
	public void set(String id, URL url)
	{
		tusDAO.set(id,url);
	}
	@Override
	public URL get(String id)
	{
		return tusDAO.get(id);
	}
	@Override
	public void remove(String id)
	{
		tusDAO.remove(id);
	}
}
