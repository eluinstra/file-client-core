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
package dev.luin.file.client.core.service.model;

import java.io.IOException;
import java.io.InputStream;

import dev.luin.file.client.core.file.ContentType;
import dev.luin.file.client.core.file.Filename;
import dev.luin.file.client.core.file.NewFSFile;
import dev.luin.file.client.core.file.Sha256Checksum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(staticName = "of")
public class NewFSFileImpl implements NewFSFile
{
	File file;

	@Override
	public Filename getName()
	{
		return new Filename(file.getContent().getName());
	}

	@Override
	public ContentType getContentType()
	{
		return new ContentType(file.getContent().getContentType());
	}

	@Override
	public Sha256Checksum getSha256Checksum()
	{
		return new Sha256Checksum(file.getSha256Checksum());
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return file.getContent().getInputStream();
	}
}
