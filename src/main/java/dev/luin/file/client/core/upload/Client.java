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
package dev.luin.file.client.core.upload;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import dev.luin.file.client.core.security.KeyStore;
import dev.luin.file.client.core.security.KeyStoreType;
import dev.luin.file.client.core.security.TrustStore;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusURLMemoryStore;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class Client extends TusClient
{
	SSLSocketFactory sslSocketFactory;

	@Override
	public void prepareConnection(HttpURLConnection connection)
	{
		super.prepareConnection(connection);
		if (connection instanceof HttpsURLConnection)
		{
			HttpsURLConnection secureConnection = (HttpsURLConnection)connection;
			secureConnection.setSSLSocketFactory(sslSocketFactory);
	  }
	}

	public static void main(String[] args) throws GeneralSecurityException, ProtocolException, IOException
	{
		if (args.length == 0)
			System.out.println("Usage: Client <file>");
		val sslFactoryManager = SSLFactoryManager.builder()
				.keyStore(KeyStore.of(KeyStoreType.PKCS12,"dev/luin/file/client/core/keystore.p12","password","password"))
				.trustStore(TrustStore.of(KeyStoreType.PKCS12,"dev/luin/file/client/core/truststore.p12","password"))
				.enabledProtocols(new String[]{"TLSv1.2"})
				.enabledCipherSuites(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384","TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"})
				.verifyHostnames(true)
				.build();
		val client = new Client(sslFactoryManager.getSslSocketFactory());
		client.setUploadCreationURL(new URL("https://localhost:8443/files/upload"));
		client.enableResuming(new TusURLMemoryStore());
		val file = new File(args[0]);
		val upload = new TusUpload(file);
		upload.setFingerprint("test");
		System.out.println("Starting uploading " + args[0] + "...");
		TusExecutor executor = new TusExecutor()
		{
			@Override
			protected void makeAttempt() throws ProtocolException, IOException
			{
				TusUploader uploader = client.resumeOrCreateUpload(upload);
				uploader.setChunkSize(1024);
				do
				{
					long totalBytes = upload.getSize();
					long bytesUploaded = uploader.getOffset();
					double progress = (double)bytesUploaded / totalBytes * 100;
					System.out.printf("Upload at %06.2f%%.\n",progress);
				} while (uploader.uploadChunk() > -1);
				uploader.finish();
				System.out.println("Upload finished.");
				System.out.format("Upload available at: %s",uploader.getUploadURL().toString());
			}
		};
		executor.makeAttempts();
	}
}
