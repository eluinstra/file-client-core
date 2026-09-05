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

import java.net.URL;

/**
 * Guard for outbound URLs opened by the client.
 *
 * <p>Download URLs are supplied by the caller (e.g. the REST {@code url} parameter) and were
 * previously trusted blindly, allowing the client to be pointed at arbitrary hosts (a
 * server-side-request-forgery vector). Requiring {@code https} blocks the plaintext request path
 * used by cloud metadata endpoints such as {@code http://169.254.169.254/} and internal HTTP
 * services, and rejects non-HTTP schemes such as {@code file://} and {@code ftp://}. The remaining
 * trust in the target host is enforced by the TLS client certificate and trust store.
 */
public final class UrlGuard
{
	private static final String HTTPS = "https";

	private UrlGuard()
	{
	}

	public static void validate(final URL url)
	{
		if (!HTTPS.equals(url.getProtocol()))
			throw new SecurityException("Only HTTPS URLs may be opened by the client, got: " + url.getProtocol() + "://" + url.getHost());
	}
}
