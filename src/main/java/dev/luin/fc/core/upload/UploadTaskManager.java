package dev.luin.fc.core.upload;

import java.net.URL;

import io.tus.java.client.TusURLStore;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
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

	public UploadTask createTask(UploadTask task)
	{
		return uploadTaskDAO.insert(task);
	}

	public long updateTask(UploadTask task)
	{
		return uploadTaskDAO.update(task);
	}

	public long deleteTask(long fileId)
	{
		return uploadTaskDAO.delete(fileId);
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
