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

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import dev.luin.file.client.core.file.ContentType;
import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.file.Filename;
import dev.luin.file.client.core.file.Length;
import dev.luin.file.client.core.file.Url;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(staticName = "createClient")
public class HttpClient
{
	@NonNull
	SSLSocketFactory sslSocketFactory;
	@NonNull
	FileSystem fs;

	public void download(final FSFile file, final Url url) throws IOException
	{
		log.info("Downloading {}",file);
		val connection = createHttpConnection(url);
		connection.setRequestMethod(DownloadMethod.FILE_INFO.getHttpMethod());
		boolean isResponseSuccessful = connection.getResponseCode() / 100 == 2;
		if (isResponseSuccessful)
		{
			val f = downloadFile(getFile(file,connection), url);
			if (f.isCompleted())
				log.info("Downloaded {}",f);
		}
		else
			throw new IllegalStateException("Unexpected response: " + connection.getResponseCode());
	}

	private HttpURLConnection createHttpConnection(final Url url)
	{
		val connection = (HttpURLConnection)url.openConnection();
		if (connection instanceof HttpsURLConnection)
		{
			HttpsURLConnection secureConnection = (HttpsURLConnection)connection;
			secureConnection.setSSLSocketFactory(sslSocketFactory);
	  }
		return connection;
	}

	private FSFile getFile(final FSFile file, final HttpURLConnection connection)
	{
		val contentLength = getContentLength(connection).getOrElseThrow(() -> new IllegalStateException("No Content-Length found"));
		val contentType = connection.getContentType();
		val filename = HeaderValue.of(connection.getHeaderField("Content-Disposition"))
				.flatMap(h -> h.getParams().get("filename"))
				.getOrNull();
		return file.withLength(new Length(contentLength))
				.withContentType(new ContentType(contentType))
				.withName(new Filename(filename));
	}

	private Option<Long> getContentLength(HttpURLConnection connection)
	{
		val result = connection.getContentLengthLong();
		return result != -1 ? Option.of(result) : Option.none();
	}

	private FSFile downloadFile(FSFile file, final Url url) throws IOException
	{
		while (!file.isCompleted())
		{
			val connection = createHttpConnection(url);
		  connection.setRequestProperty("Range","bytes=" + file.getFileLength().getStringValue() + "-" + file.getLength().getStringValue());
		  file = fs.append(file,connection.getInputStream());
		}
		return file;
	}
}