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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import dev.luin.file.client.core.ValueObject;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

//@Value
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
@ToString
public class Url implements ValueObject<String>
{
	URL value;

	public Url(String url)
	{
		this(Try.success(url.contains("://0.0.0.0:") ? url.replace("://0.0.0.0:","://localhost:") : url)
				.mapTry(URL::new)
				.getOrElseThrow(() -> new IllegalArgumentException("Url is invalid")));
	}

	public Url(URL url)
	{
		value = url;
	}

	public URLConnection openConnection()
	{
		try
		{
			return value.openConnection();
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public String getValue()
	{
		return value.toString();
	}

	public URL toURL()
	{
		return value;
	}
}
