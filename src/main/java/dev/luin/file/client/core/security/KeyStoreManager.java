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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyStoreManager
{
	private static Map<String, KeyStore> keystores = new ConcurrentHashMap<>();

	public static KeyStore getKeyStore(@NonNull final KeyStoreType type, @NonNull final String path, @NonNull final String password)
			throws GeneralSecurityException, IOException
	{
		if (!keystores.containsKey(path))
			keystores.put(path, loadKeyStore(type, path, password));
		return keystores.get(path);
	}

	private static KeyStore loadKeyStore(final KeyStoreType type, final String location, final String password) throws GeneralSecurityException, IOException
	{
		// location = ResourceUtils.getURL(SystemPropertyUtils.resolvePlaceholders(location)).getFile();
		try (val in = getInputStream(location))
		{
			val keyStore = KeyStore.getInstance(type.name());
			keyStore.load(in, password.toCharArray());
			return keyStore;
		}
	}

	private static InputStream getInputStream(final String location) throws FileNotFoundException
	{
		try
		{
			return new FileInputStream(location);
		}
		catch (FileNotFoundException e)
		{
			var result = KeyStoreManager.class.getResourceAsStream(location);
			if (result == null)
				result = KeyStoreManager.class.getResourceAsStream("/" + location);
			if (result == null)
				throw e;
			return result;
		}
	}

}
