/*
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
package dev.luin.file.client.core.file;

import static org.apache.commons.io.IOUtils.copyLarge;

import dev.luin.file.client.core.file.encryption.EncryptionAlgorithm;
import dev.luin.file.client.core.file.encryption.EncryptionSecret;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.val;

@Builder(access = AccessLevel.PACKAGE)
@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FSFile
{
	@With
	FileId id;
	@With
	Url url;
	@NonNull
	@Getter(value = AccessLevel.PACKAGE)
	Path path;
	@With
	Filename name;
	@With
	ContentType contentType;
	@NonNull
	EncryptionAlgorithm encryptionAlgorithm;
	@NonNull
	EncryptionSecret encryptionSecret;
	@With
	Md5Checksum md5Checksum;
	@With
	Sha256Checksum sha256Checksum;
	@NonNull
	Timestamp timestamp;
	@With
	Length length;

	public File getFile()
	{
		return path.toFile();
	}

	public Length getFileLength()
	{
		return new Length(getFile().length());
	}

	public Instant getLastModified()
	{
		return Instant.ofEpochMilli(getFile().lastModified());
	}

	public boolean isCompleted()
	{
		return length.equals(getFileLength());
	}

	FSFile append(@NonNull final InputStream input) throws IOException
	{
		val file = getFile();
		if (!file.exists() || isCompleted())
			throw new FileNotFoundException(url.toString());
		try (val output = new FileOutputStream(file, true))
		{
			input.transferTo(output);
			return isCompleted() ? complete() : this;
		}
	}

	private FSFile complete() throws IOException
	{
		val file = getFile();
		if (!file.exists())// || !fsFile.isCompleted())
			throw new FileNotFoundException(url.toString());
		return this; // TODO validate file???
	}

	FSFile append(@NonNull final InputStream input, final long first, final long length) throws IOException
	{
		val file = getFile();
		if (!file.exists() || isCompleted())
			throw new FileNotFoundException(url.toString());
		try (val output = new FileOutputStream(file, true))
		{
			copyLarge(input, output, first, length);
			return isCompleted() ? complete() : this;
		}
	}
}
