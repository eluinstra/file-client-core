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

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeyStoreConfig
{
	@Value("${truststore.type}")
	KeyStoreType trustStoretype;
	@Value("${truststore.path}")
	String trustStorepath;
	@Value("${truststore.password}")
	String trustStorepassword;
	@Value("${client.keystore.type}")
	KeyStoreType clientKeyStoreType;
	@Value("${client.keystore.path}")
	String clientKeyStorePath;
	@Value("${client.keystore.password}")
	String clientKeyStorePassword;
	@Value("${client.keystore.keyPassword}")
	String clientKeyStoreKeyPassword;
	@Value("${client.keystore.defaultAlias}")
	String clientKeyStoreDefaultAlias;

	@Bean
	public TrustStore trustStore()
	{
		return TrustStore.of(trustStoretype, trustStorepath, trustStorepassword);
	}

	@Bean("clientKeyStore")
	public KeyStore clientKeyStore()
	{
		return KeyStore.of(clientKeyStoreType, clientKeyStorePath, clientKeyStorePassword, clientKeyStoreKeyPassword, clientKeyStoreDefaultAlias);
	}
}
