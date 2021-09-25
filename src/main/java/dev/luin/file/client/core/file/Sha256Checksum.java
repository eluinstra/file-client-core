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

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.matchesPattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import dev.luin.file.client.core.ValueObject;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
public class Sha256Checksum implements ValueObject<String>
{
	@NonNull
	String value;

	public static Sha256Checksum of(@NonNull final File file)
	{
		try (val is = new FileInputStream(file))
		{
			return new Sha256Checksum(DigestUtils.sha256Hex(is));
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public Sha256Checksum(@NonNull final String checksum)
	{
		value = Try.success(checksum)
				.andThen(v -> inclusiveBetween(32,64,v.length()))
				.map(String::toUpperCase)
				.andThen(v -> matchesPattern(v,"^[0-9A-F]*$"))
				.get();
	}
	
	public boolean validate(/*@NonNull*/ final Sha256Checksum checksum)
	{
		return this.equals(checksum);
	}
}
