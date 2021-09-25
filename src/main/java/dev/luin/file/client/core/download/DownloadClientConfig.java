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
package dev.luin.file.client.core.download;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import com.querydsl.sql.SQLQueryFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.security.KeyStore;
import dev.luin.file.client.core.security.TrustStore;
import dev.luin.file.client.core.upload.SSLFactoryManager;
import lombok.AccessLevel;
import lombok.val;
import lombok.experimental.FieldDefaults;

@Configuration
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DownloadClientConfig
{
	@Value("${https.enabledProtocols}")
	String[] enabledProtocols;
	@Value("${https.enabledCipherSuites}")
	String[] enabledCipherSuites;
	@Value("${https.verifyHostnames}")
	boolean verifyHostnames;
	@Value("${downloadTask.retry.maxAttempts}")
	int maxRetries;
	@Value("${downloadTask.retry.interval}")
	int retryInterval;
	@Value("${downloadTask.retry.maxMultiplier}")
	int retryMaxMultiplier;

	@Bean
	public DownloadTaskHandler downloadTaskHandler(
			@Autowired @Qualifier("clientKeyStore") KeyStore clientKeyStore,
			@Autowired TrustStore trustStore,
			@Autowired FileSystem fs,
			@Autowired DownloadTaskManager downloadTaskManager) throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException
	{
		val sslFactoryManager = SSLFactoryManager.builder()
				.keyStore(clientKeyStore)
				.trustStore(trustStore)
				.enabledProtocols(enabledProtocols)
				.enabledCipherSuites(enabledCipherSuites)
				.verifyHostnames(verifyHostnames)
				.build();
		return new DownloadTaskHandler(fs,HttpClient.createClient(sslFactoryManager.getSslSocketFactory(),fs),downloadTaskManager,maxRetries);
	}

	@Bean
	public DownloadTaskManager downloadTaskManager(@Autowired DownloadTaskDAO downloadTaskDAO)
	{
		return new DownloadTaskManager(downloadTaskDAO,retryInterval,retryMaxMultiplier);
	}

	@Bean
	public DownloadTaskDAO downloadTaskDAO(@Autowired SQLQueryFactory queryFactory)
	{
		return new DownloadTaskDAOImpl(queryFactory);
	}
}
