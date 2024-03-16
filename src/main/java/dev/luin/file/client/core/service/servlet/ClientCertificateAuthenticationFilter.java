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
package dev.luin.file.client.core.service.servlet;

import dev.luin.file.client.core.security.KeyStoreManager;
import dev.luin.file.client.core.security.KeyStoreType;
import dev.luin.file.client.core.server.ClientCertificateManager;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientCertificateAuthenticationFilter implements Filter
{
	@NonNull
	KeyStore trustStore;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException
	{
		try
		{
			val trustStoreType = filterConfig.getInitParameter("trustStoreType");
			val trustStorePath = filterConfig.getInitParameter("trustStorePath");
			val trustStorePassword = filterConfig.getInitParameter("trustStorePassword");
			trustStore = KeyStoreManager.getKeyStore(KeyStoreType.valueOf(trustStoreType), trustStorePath, trustStorePassword);
		}
		catch (GeneralSecurityException | IOException e)
		{
			throw new ServletException(e);
		}
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		try
		{
			val certificate = ClientCertificateManager.getCertificate();
			if (validate(trustStore, certificate))
				chain.doFilter(request, response);
			else
				((HttpServletResponse)response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		catch (KeyStoreException e)
		{
			throw new ServletException(e);
		}
	}

	private boolean validate(final KeyStore trustStore, final X509Certificate x509Certificate) throws KeyStoreException
	{
		return x509Certificate != null && trustStore.getCertificateAlias(x509Certificate) != null;
	}

	@Override
	public void destroy()
	{
		// Do nothing
	}
}
