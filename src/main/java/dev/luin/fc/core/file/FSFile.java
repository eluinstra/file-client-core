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
package dev.luin.fc.core.file;

import java.io.File;
import java.net.URL;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Builder(access = AccessLevel.PACKAGE)
@Value
@AllArgsConstructor
public class FSFile
{
	Long id;
	@With
	URL url;
	@NonNull
	@Getter(value=AccessLevel.PACKAGE)
	String path;
	String name;
	@NonNull
	String contentType;
	@With
	String md5Checksum;
	@With
	String sha256Checksum;
	@With
	Long length;

	public File getFile()
	{
		return FileSystem.getFile.apply(path);
	}

	public long getFileLength()
	{
		return getFile().length();
	}

	public Instant getLastModified()
	{
		return Instant.ofEpochMilli(getFile().lastModified());
	}

	public boolean isCompleted()
	{
		return length != null && length == getFileLength();
	}
}
