package dev.luin.fc.core.download;

import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
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

	public DownloadTask createTask(DownloadTask task)
	{
		return downloadTaskDAO.insert(task);
	}

	public long updateTask(DownloadTask task)
	{
		return downloadTaskDAO.update(task);
	}

	public long deleteTask(long fileId)
	{
		return downloadTaskDAO.delete(fileId);
	}
}
