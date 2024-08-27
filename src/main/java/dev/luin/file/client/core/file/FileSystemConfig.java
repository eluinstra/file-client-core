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

import com.querydsl.sql.SQLQueryFactory;
import dev.luin.file.client.core.file.encryption.AesGcmService;
import dev.luin.file.client.core.file.encryption.ChaChaService;
import dev.luin.file.client.core.file.encryption.EncryptionAlgorithm;
import dev.luin.file.client.core.file.encryption.EncryptionService;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.NoSuchPaddingException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileSystemConfig
{
	@Value("${file.baseDir}")
	String baseDir;
	@Value("${file.directoryDepth}")
	int directoryDepth;
	@Value("${file.filenameLength}")
	int filenameLength;
	@Value("${file.defaultEncryptionAlgorithm}")
	EncryptionAlgorithm defaultEncryptionAlgorithm;

	@Bean
	public FileSystem fileSystem(@Autowired FSFileDAO fsFileDAO) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		return FileSystem.builder()
				.fsFileDAO(fsFileDAO)
				.randomFileGenerator(new RandomFileGenerator(baseDir, directoryDepth, filenameLength))
				.encryptionService(
						new EncryptionService(
								defaultEncryptionAlgorithm,
								Map.of(EncryptionAlgorithm.AES256_GCM, new AesGcmService(), EncryptionAlgorithm.CHACHA20, new ChaChaService())))
				.build();
	}

	@Bean
	public FSFileDAO fsFileDAO(@Autowired SQLQueryFactory queryFactory)
	{
		return new FSFileDAOImpl(queryFactory);
	}
}
