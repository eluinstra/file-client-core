package dev.luin.fc.core.download;

import java.time.Duration;

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

	public Option<DownloadTask> getNextTask()
	{
		return downloadTaskDAO.getNextTask();
	}

	public Seq<DownloadTask> getTasks(List<DownloadStatus> statuses)
	{
		return statuses.length() == 0 ? downloadTaskDAO.getTasks() : downloadTaskDAO.getTasks(statuses);
	}

	public DownloadTask createTask(long fileId, String createUrl)
	{
		val task = DownloadTask.of(fileId,createUrl);
		return downloadTaskDAO.insert(task);
	}

	public DownloadTask createNextTask(DownloadTask task)
	{
		DownloadTask result = task
				.withScheduleTime(task.getScheduleTime().plus(Duration.ofSeconds((task.getRetries() + 1) * 1800)))
				.withRetries(task.getRetries() + 1);
		downloadTaskDAO.update(result);
		return result;
	}

	public DownloadTask createSucceededTask(DownloadTask task)
	{
		val result = task.withStatus(DownloadStatus.SUCCEEDED);
		downloadTaskDAO.update(result);
		return result;
	}

	public DownloadTask createFailedTask(DownloadTask task)
	{
		DownloadTask result = task.withStatus(DownloadStatus.FAILED);
		downloadTaskDAO.update(result);
		return result;
	}

	public boolean deleteTask(long fileId)
	{
		return downloadTaskDAO.delete(fileId) > 0;
	}
}
