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
package dev.luin.file.client.core.upload;

import com.querydsl.sql.SQLQueryFactory;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.security.KeyStore;
import dev.luin.file.client.core.security.TrustStore;
import io.tus.java.client.TusURLStore;
import java.security.GeneralSecurityException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadClientConfig
{
	@Value("${client.ssl.enabledProtocols}")
	String[] enabledProtocols;
	@Value("${client.ssl.enabledCipherSuites}")
	String[] enabledCipherSuites;
	@Value("${client.ssl.verifyHostnames}")
	boolean verifyHostnames;
	@Autowired
	SQLQueryFactory queryFactory;
	@Value("${uploadTask.retry.maxAttempts}")
	int maxRetries;
	@Value("${uploadTask.retry.interval}")
	int retryInterval;
	@Value("${uploadTask.retry.maxMultiplier}")
	int retryMaxMultiplier;

	@Bean
	public UploadTaskHandler uploadTaskHandler(
			@Autowired @Qualifier("clientKeyStore") KeyStore clientKeyStore,
			@Autowired TrustStore trustStore,
			@Autowired FileSystem fs,
			@Autowired UploadTaskManager uploadTaskManager) throws GeneralSecurityException
	{
		val sslFactoryManager = SSLFactoryManager.builder()
				.keyStore(clientKeyStore)
				.trustStore(trustStore)
				.enabledProtocols(enabledProtocols)
				.enabledCipherSuites(enabledCipherSuites)
				.verifyHostnames(verifyHostnames)
				.build();
		return new UploadTaskHandler(sslFactoryManager, fs, uploadTaskManager, maxRetries);
	}

	@Bean
	public UploadTaskManager uploadTaskManager(@Autowired UploadTaskDAO uploadTaskDAO, @Autowired TusURLStore tusDAO)
	{
		return new UploadTaskManager(uploadTaskDAO, tusDAO, retryInterval, retryMaxMultiplier);
	}

	@Bean
	public UploadTaskDAO uploadTaskDAO()
	{
		return new UploadTaskDAOImpl(queryFactory);
	}

	@Bean
	public TusURLStore tusDAO()
	{
		return new TusUrlDAO(queryFactory);
	}
}
