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

import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyStore
{
	private static Map<String,KeyStore> keyStores = new ConcurrentHashMap<>();
	@NonNull
	String path;
	@NonNull
	java.security.KeyStore keyStore;
	@NonNull
	String keyPassword;
	String defaultAlias;

	public static KeyStore of(@NonNull KeyStoreType type, @NonNull String path, @NonNull String password, @NonNull String keyPassword)
	{
		return keyStores.computeIfAbsent(path,k -> new KeyStore(type,k,password,keyPassword,null));
	}

	public static KeyStore of(@NonNull KeyStoreType type, @NonNull String path, @NonNull String password, @NonNull String keyPassword, @NonNull String defaultAlias)
	{
		String key = path + defaultAlias;
		return keyStores.computeIfAbsent(key,k -> new KeyStore(type,path,password,keyPassword,defaultAlias));
	}

	private KeyStore(@NonNull KeyStoreType type, @NonNull String path, @NonNull String password, @NonNull String keyPassword, String defaultAlias)
	{
		this.path = path;
		this.keyPassword = keyPassword;
		this.defaultAlias = defaultAlias;
		this.keyStore = KeyStoreUtils.loadKeyStore(type,path,password);
	}

	public Certificate getCertificate(String alias) throws KeyStoreException
	{
		return keyStore.getCertificate(alias);
	}

	public String getCertificateAlias(X509Certificate cert) throws KeyStoreException
	{
		return keyStore.getCertificateAlias(cert);
	}

	public Certificate[] getCertificateChain(String alias) throws KeyStoreException
	{
		return keyStore.getCertificateChain(alias);
	}

	public Key getKey(String alias, char[] password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException
	{
		return keyStore.getKey(alias,password);
	}

}
