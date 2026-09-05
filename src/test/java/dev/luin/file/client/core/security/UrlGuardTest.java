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
package dev.luin.file.client.core.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlGuardTest
{
	@Test
	void httpsIsAllowed()
	{
		assertThatCode(() -> UrlGuard.validate(url("https://localhost:8443/files/x"))).doesNotThrowAnyException();
		assertThatCode(() -> UrlGuard.validate(url("https://example.com/"))).doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings = {"http://169.254.169.254/latest/meta-data/", "http://localhost:8080", "file:///etc/passwd", "ftp://host/file"})
	void nonHttpsIsRejected(String rawUrl)
	{
		assertThatThrownBy(() -> UrlGuard.validate(url(rawUrl))).isInstanceOf(SecurityException.class);
	}

	private static URL url(final String raw)
	{
		try
		{
			return new URI(raw).toURL();
		}
		catch (URISyntaxException | MalformedURLException e)
		{
			throw new AssertionError(e);
		}
	}
}
