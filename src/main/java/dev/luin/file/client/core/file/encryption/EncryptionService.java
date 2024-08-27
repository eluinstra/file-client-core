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
package dev.luin.file.client.core.file.encryption;

import java.io.InputStream;
import java.util.Map;
import javax.crypto.CipherInputStream;
import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EncryptionService
{
	EncryptionAlgorithm defaultAlgorithm;
	Map<EncryptionAlgorithm, EncryptionEngine> engines;

	public EncryptionSecret generateSecret(EncryptionAlgorithm algorithm)
	{
		return engines.get(algorithm).generateSecret();
	}

	public CipherInputStream encryptionInputStream(InputStream in, EncryptionSecret secret)
	{
		return encryptionInputStream(defaultAlgorithm, in, secret);
	}

	public CipherInputStream encryptionInputStream(EncryptionAlgorithm algorithm, InputStream in, EncryptionSecret secret)
	{
		return engines.get(algorithm).encryptionInputStream(in, secret);
	}

	public CipherInputStream decryptionInputStream(EncryptionAlgorithm algorithm, InputStream in, EncryptionSecret secret)
	{
		return engines.get(algorithm).decryptionInputStream(in, secret);
	}
}
