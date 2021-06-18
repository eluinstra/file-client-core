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

import org.apache.commons.lang3.StringUtils;

import dev.luin.file.client.core.ValueObject;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

@Value
public class ContentType implements ValueObject<String>
{
	public static final ContentType TEXT = new ContentType("text/plain");
	@NonNull
	String value;

	public ContentType(@NonNull final String contentType)
	{
		value = Option.of(contentType)
				.flatMap(this::parseValue)
				.get();
	}

	private Option<String> parseValue(final String s)
	{
		return s != null ? Option.of(s.split(";")[0].trim()).filter(StringUtils::isNotEmpty) : Option.none();
	}

	public boolean isBinary()
	{
		return !value.matches("^(text/.*|.*/xml)$");
	}
}
