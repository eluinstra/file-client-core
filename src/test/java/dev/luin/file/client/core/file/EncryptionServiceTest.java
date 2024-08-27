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

import static org.assertj.core.api.Assertions.assertThat;

import dev.luin.file.client.core.file.encryption.AesGcmService;
import dev.luin.file.client.core.file.encryption.ChaChaService;
import dev.luin.file.client.core.file.encryption.EncryptionAlgorithm;
import dev.luin.file.client.core.file.encryption.EncryptionService;
import io.vavr.control.Try;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.NoSuchPaddingException;
import lombok.val;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@TestInstance(Lifecycle.PER_CLASS)
class EncryptionServiceTest
{
	RandomFileGenerator randomFileGenerator = new RandomFileGenerator("/tmp", 0, 10);

	@ParameterizedTest
	@EnumSource(EncryptionAlgorithm.class)
	void encryptFile(EncryptionAlgorithm defaultAlgorithm) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		val in = createRandomFile();
		val encryptionService =
				new EncryptionService(defaultAlgorithm, Map.of(EncryptionAlgorithm.AES256_GCM, new AesGcmService(), EncryptionAlgorithm.CHACHA20, new ChaChaService()));
		val encrypted = randomFileGenerator.create().get();
		val secret = encryptionService.generateSecret(encryptionService.getDefaultAlgorithm());
		Try.withResources(() -> encryptionService.encryptionInputStream(encryptionService.getDefaultAlgorithm(), new FileInputStream(in.getFile()), secret))
				.of(is -> is.transferTo(new FileOutputStream(encrypted.getFile())));
		val out = randomFileGenerator.create().get();
		Try.withResources(() -> encryptionService.decryptionInputStream(encryptionService.getDefaultAlgorithm(), new FileInputStream(encrypted.getFile()), secret))
				.of(is -> is.transferTo(new FileOutputStream(out.getFile())));
		assertThat(Files.mismatch(in.getPath(), encrypted.getPath())).isNotEqualTo(-1);
		assertThat(Files.mismatch(in.getPath(), out.getPath())).isEqualTo(-1);
	}

	private RandomFile createRandomFile() throws IOException
	{
		val result = randomFileGenerator.create().get();
		val writer = new BufferedWriter(new FileWriter(result.getFile()));
		writer.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi elementum tellus non varius volutpat.");
		writer.close();
		return result;
	}
}
