package dev.luin.fc.core.upload;

import io.vavr.control.Option;

public interface UploadTaskDAO
{
	Option<UploadTask> getNextTask();
	UploadTask insert(UploadTask task);
	long update(UploadTask task);
	long delete(long fileId);
}
