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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.val;

public class ChaChaService implements EncryptionEngine
{
	private static final String ALGORITHM = "ChaCha20";
	private static final String TRANSFORMATION = "ChaCha20-Poly1305/None/NoPadding";
	private static final int KEY_LENGTH = 256;
	private static final int IV_LENGTH = 12;
	SecureRandom secureRandom = new SecureRandom();
	KeyGenerator keyGenerator;

	public ChaChaService() throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(KEY_LENGTH, SecureRandom.getInstanceStrong());
	}

	@Override
	public EncryptionSecret generateSecret()
	{
		return new EncryptionSecret(keyGenerator.generateKey().getEncoded(), generateIv());
	}

	private byte[] generateIv()
	{
		val result = new byte[IV_LENGTH];
		secureRandom.nextBytes(result);
		return result;
	}

	@Override
	public CipherInputStream encryptionInputStream(InputStream in, EncryptionSecret secret)
	{
		try
		{
			val cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret.getKey(), ALGORITHM), new IvParameterSpec(secret.getIv()));
			return new CipherInputStream(in, cipher);
		}
		catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			throw new IllegalStateException(e);
		}
	}

	@Override
	public CipherInputStream decryptionInputStream(InputStream in, EncryptionSecret secret)
	{
		try
		{
			val cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret.getKey(), ALGORITHM), new IvParameterSpec(secret.getIv()));
			return new CipherInputStream(in, cipher);
		}
		catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
