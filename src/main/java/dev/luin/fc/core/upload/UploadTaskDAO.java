package dev.luin.fc.core.upload;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

public interface UploadTaskDAO
{
	Option<UploadTask> getNextTask();
	Seq<UploadTask> getTasks();
	Seq<UploadTask> getTasks(List<UploadStatus> statuses);
	UploadTask insert(UploadTask task);
	long update(UploadTask task);
	long delete(long fileId);
}
