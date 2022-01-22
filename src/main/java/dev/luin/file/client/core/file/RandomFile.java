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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomFile
{
	@NonNull
	Path path;
	@NonNull
	File file;

	public static Try<RandomFile> create(@NonNull final String baseDir, final int filenameLength)
	{
		while (true)
		{
			val path = createRandomPath(baseDir, filenameLength);
			try
			{
				val file = createFile(path);
				if (file.isSingleValued())
					return Try.success(file.get());
			}
			catch (IOException e)
			{
				return Try.failure(new IOException("Error creating file " + path,e));
			}
		}
	}
	
	private static Path createRandomPath(final String baseDir, final int filenameLength)
	{
		val filename = RandomStringUtils.randomNumeric(filenameLength);
		return Paths.get(baseDir,filename);
	}
	
	private static Option<RandomFile> createFile(final Path path) throws IOException
	{
		val file = path.toFile();
		return file.createNewFile() ? Option.some(new RandomFile(path,file)) : Option.none();
	}

	private RandomFile(final Path path)
	{
		this.path = path;
		file = path.toFile();
	}

	Length getLength()
	{
		return new Length(file.length());
	}

	public long write(@NonNull final InputStream input)
	{
		return Try.withResources(() -> new FileOutputStream(file))
				.of(o -> IOUtils.copyLarge(input,o))
				.getOrElseThrow(t -> new IllegalStateException("Error writing to file " + path,t));
	}
}
