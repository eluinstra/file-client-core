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

import io.vavr.control.Try;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.io.IOUtils;

@Value
public class RandomFile
{
	@NonNull
	Path path;
	@NonNull
	File file;

	public RandomFile(final Path path)
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
				.of(o -> IOUtils.copyLarge(input, o))
				.getOrElseThrow(t -> new IllegalStateException("Error writing to file " + path, t));
	}
}
