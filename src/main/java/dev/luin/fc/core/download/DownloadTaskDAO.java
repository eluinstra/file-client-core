package dev.luin.fc.core.download;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

public interface DownloadTaskDAO
{
	Option<DownloadTask> getNextTask();
	Seq<DownloadTask> getTasks();
	Seq<DownloadTask> getTasks(List<DownloadStatus> statuses);
	DownloadTask insert(DownloadTask task);
	long update(DownloadTask task);
	long delete(long fileId);
}
