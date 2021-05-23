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
package dev.luin.file.client.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;

import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;

import dev.luin.file.client.core.service.model.FileDataSource;
import io.vavr.Function1;
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
	public static final Function1<String,File> getFile = path -> Paths.get(path).toFile();
	@With
	Long id;
	@With
	URL url;
	@NonNull
	@Getter(value=AccessLevel.PACKAGE)
	String path;
	@With
	String name;
	@With
	String contentType;
	@With
	Md5Checksum md5Checksum;
	@With
	Sha256Checksum sha256Checksum;
	@NonNull
	Instant timestamp;
	@With
	Long length;

	public File getFile()
	{
		return getFile.apply(path);
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

	public DataSource toDataSource()
	{
		return new FileDataSource(getFile(),name,contentType);
	}

	public FSFile append(@NonNull final InputStream input) throws IOException
	{
		val file = getFile();
		if (!file.exists() || isCompleted())
			throw new FileNotFoundException(url.toString());
		try (val output = new FileOutputStream(file,true))
		{
			IOUtils.copyLarge(input,output);
			if (isCompleted())
				return complete();
			else
				return this;
		}
	}

	private FSFile complete() throws IOException
	{
		val file = getFile();
		if (!file.exists())// || !fsFile.isCompleted())
			throw new FileNotFoundException(url.toString());
		val result = this
				.withSha256Checksum(getSha256Checksum())
				.withMd5Checksum(getMd5Checksum());
		return result;
	}

	public FSFile append(@NonNull final InputStream input, final long first, final long length) throws IOException
	{
		val file = getFile();
		if (!file.exists() || isCompleted())
			throw new FileNotFoundException(url.toString());
		try (val output = new FileOutputStream(file,true))
		{
			IOUtils.copyLarge(input,output,first,length);
			if (isCompleted())
				return complete();
			else
				return this;
		}
	}

	public long write(@NonNull final OutputStream output) throws IOException
	{
		val file = getFile();
		if (!file.exists() || !isCompleted())
			throw new FileNotFoundException(getUrl().toString());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	public long write(@NonNull final OutputStream output, final long first, final long length) throws IOException
	{
		val file = getFile();
		if (!file.exists() || !isCompleted())
			throw new FileNotFoundException(getUrl().toString());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output,first,length);
		}
	}

}
