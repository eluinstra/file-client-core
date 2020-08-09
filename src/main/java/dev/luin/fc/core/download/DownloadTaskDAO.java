package dev.luin.fc.core.download;

import io.vavr.control.Option;

public interface DownloadTaskDAO
{
	Option<DownloadTask> getNextTask();
	DownloadTask insert(DownloadTask task);
	long update(DownloadTask task);
	long delete(long fileId);
}
