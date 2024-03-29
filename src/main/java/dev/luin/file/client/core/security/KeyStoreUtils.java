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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class KeyStoreUtils
{
	public static KeyStore loadKeyStore(KeyStoreType type, String location, String password)
	{
		try (val in = getInputStream(location))
		{
			val keyStore = KeyStore.getInstance(type.name());
			keyStore.load(in,password.toCharArray());
			return keyStore;
		}
		catch (GeneralSecurityException | IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public static InputStream getInputStream(String location) throws FileNotFoundException
	{
		try
		{
			return new FileInputStream(location);
		}
		catch (FileNotFoundException e)
		{
			var result = KeyStoreUtils.class.getResourceAsStream(location);
			if (result == null)
				result = KeyStoreUtils.class.getResourceAsStream("/" + location);
			if (result == null)
				throw e;
			return result;
		}
	}
}
