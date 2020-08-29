package dev.luin.fc.core.upload;

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

	public Option<UploadTask> getNextTask()
	{
		return uploadTaskDAO.getNextTask();
	}

	public Seq<UploadTask> getTasks(List<UploadStatus> statuses)
	{
		return statuses.length() == 0 ? uploadTaskDAO.getTasks() : uploadTaskDAO.getTasks(statuses);
	}

	public UploadTask createTask(Long fileId, String creationUrl)
	{
		val task = UploadTask.of(fileId,creationUrl);
		return uploadTaskDAO.insert(task);
	}

	public UploadTask createNextTask(UploadTask task)
	{
		val result = task
				.withScheduleTime(task.getScheduleTime().plus(Duration.ofSeconds((task.getRetries() + 1) * 1800)))
				.withRetries(task.getRetries() + 1);
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
