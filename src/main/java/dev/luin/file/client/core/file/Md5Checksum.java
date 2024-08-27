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

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.matchesPattern;

import dev.luin.file.client.core.ValueObject;
import io.vavr.control.Try;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.NonNull;
import lombok.Value;

@Value
public class Md5Checksum implements ValueObject<String>
{
	@NonNull
	String value;

	public static MessageDigest messageDigest() throws NoSuchAlgorithmException
	{
		return MessageDigest.getInstance("MD5");
	}

	public Md5Checksum(final byte[] checksum)
	{
		this(HexFormat.of().formatHex(checksum));
	}

	public Md5Checksum(@NonNull final String checksum)
	{
		value =
				Try.success(checksum).andThen(v -> inclusiveBetween(32, 32, v.length())).map(String::toUpperCase).andThen(v -> matchesPattern(v, "^[0-9A-F]*$")).get();
	}
}
