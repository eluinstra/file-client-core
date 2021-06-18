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

import dev.luin.file.client.core.file.FileId;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

public interface DownloadTaskDAO
{
	Option<DownloadTask> getTask(FileId fileId);
	Option<DownloadTask> getNextTask();
	Seq<DownloadTask> getTasks();
	Seq<DownloadTask> getTasks(List<DownloadStatus> statuses);
	DownloadTask insert(DownloadTask task);
	long update(DownloadTask task);
	long delete(FileId fileId);
}
