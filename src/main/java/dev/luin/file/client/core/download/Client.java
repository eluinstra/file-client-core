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
package dev.luin.file.client.core.download;

import dev.luin.file.client.core.file.RandomFileGenerator;
import dev.luin.file.client.core.security.KeyStore;
import dev.luin.file.client.core.security.KeyStoreType;
import dev.luin.file.client.core.security.TrustStore;
import dev.luin.file.client.core.upload.SSLFactoryManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class Client
{
	int chunkSize = 256;
	@NonNull
	RandomFileGenerator randomFileGenerator;
	@NonNull
	SSLSocketFactory sslSocketFactory;

	public static void main(String[] args) throws Exception
	{
		if (args.length == 0)
			System.out.println("Usage: Client <url>");
		val randomFileGenerator = new RandomFileGenerator("", 0, 10);
		val sslFactoryManager = SSLFactoryManager.builder()
				.keyStore(KeyStore.of(KeyStoreType.PKCS12, "dev/luin/file/client/core/keystore.p12", "password", "password"))
				.trustStore(TrustStore.of(KeyStoreType.PKCS12, "dev/luin/file/client/core/truststore.p12", "password"))
				.enabledProtocols(new String[]{"TLSv1.3,TLSv1.2"})
				.enabledCipherSuites(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384" ,"TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256"})
				.verifyHostnames(true)
				.build();
		val client = new Client(randomFileGenerator, sslFactoryManager.getSslSocketFactory());
		client.download(args[0]);
	}

	private void download(String s) throws IOException
	{
		val url = new URL(s);
		var connection = createConnection(url);
		connection.setRequestMethod("HEAD");
		val contentLength = connection.getContentLengthLong();
		val file = randomFileGenerator.create().map(f -> f.getPath().toFile()).get();
		long fileLength = 0;
		while (fileLength < contentLength)
		{
			fileLength += chunkSize;
			connection = createConnection(url);
			connection.setRequestProperty("Range", "bytes=" + file.length() + "-" + fileLength);
			try (val output = new FileOutputStream(file, true))
			{
				connection.getInputStream().transferTo(output);
			}
		}
		System.out.println(file.getAbsolutePath());
	}

	private HttpURLConnection createConnection(final URL url) throws IOException
	{
		val connection = (HttpURLConnection)url.openConnection();
		if (connection instanceof HttpsURLConnection)
		{
			HttpsURLConnection secureConnection = (HttpsURLConnection)connection;
			secureConnection.setSSLSocketFactory(sslSocketFactory);
		}
		return connection;
	}
}
