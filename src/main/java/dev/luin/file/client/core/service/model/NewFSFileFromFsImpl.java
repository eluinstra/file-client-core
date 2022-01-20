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
package dev.luin.file.client.core.service.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.core.MediaType;

import dev.luin.file.client.core.file.ContentType;
import dev.luin.file.client.core.file.Filename;
import dev.luin.file.client.core.file.NewFSFile;
import dev.luin.file.client.core.file.Sha256Checksum;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(staticName = "of")
public class NewFSFileFromFsImpl implements NewFSFile
{
	@NonNull
	NewFileFromFs file;
	@NonNull
	Path sharedFs;

	@Override
	public Filename getName()
	{
		return file.getName();
	}

	@Override
	public ContentType getContentType()
	{
		val f = sharedFs.resolve(file.getName().getValue());
		val contentType = Try.of(() -> Files.probeContentType(f))
			.getOrElse(MediaType.APPLICATION_OCTET_STREAM);
		return new ContentType(contentType);
	}

	@Override
	public Sha256Checksum getSha256Checksum()
	{
		return new Sha256Checksum(file.getSha256Checksum());
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		val f = validateFilename(file.getName().getValue(), sharedFs);
		return new FileInputStream(f.toFile());
	}

	public static Path validateFilename(String filename, Path sharedFs) throws IOException
	{
		val f = sharedFs.resolve(filename);
		if (f.toAbsolutePath().startsWith(sharedFs))
			return f;
		else
			throw new IOException("Illegal file access");
	}

}
