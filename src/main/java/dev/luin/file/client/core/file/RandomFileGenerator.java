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

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class RandomFileGenerator
{

	@NonNull
	String baseDir;
	int directoryDepth;
	int filenameLength;
	@NonNull
	Function<Integer, String> randomFilename;

	public RandomFileGenerator(String baseDir, int directoryDepth, int filenameLength)
	{
		this(baseDir, directoryDepth, filenameLength, RandomStringUtils::randomNumeric);
	}

	public Try<RandomFile> create()
	{
		while (true)
		{
			try
			{
				val file = createRandomFile(baseDir, directoryDepth, () -> randomFilename.apply(directoryDepth + filenameLength));
				if (file.isDefined())
					return Try.success(file.get());
			}
			catch (IOException e)
			{
				return Try.failure(e);
			}
		}
	}

	private Option<RandomFile> createRandomFile(final String baseDir, final int directoryDepth, Supplier<String> randomFilename) throws IOException
	{
		val filename = randomFilename.get();
		val path = createPath(Paths.get(baseDir), filename, directoryDepth);
		return createFile(path);
	}

	private Path createPath(final Path path, final String filename, final int subDirectories) throws IOException
	{
		try
		{
			if (subDirectories > 0)
				return createPath(Files.createDirectory(path.resolve(filename.substring(0, 1))), filename.substring(1), subDirectories - 1);
			else
				return path.resolve(filename);
		}
		catch (FileAlreadyExistsException e)
		{
			return createPath(path.resolve(filename.substring(0, 1)), filename.substring(1), subDirectories - 1);
		}
	}

	private Option<RandomFile> createFile(final Path path) throws IOException
	{
		try
		{
			val file = new RandomFile(path);
			return file.getFile().createNewFile() ? Option.some(file) : Option.none();
		}
		catch (IOException e)
		{
			throw new IOException("Error creating file " + path, e);
		}
	}

}
