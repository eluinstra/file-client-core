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
package dev.luin.file.client.core.service.model;

import dev.luin.file.client.core.file.ContentType;
import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.file.Filename;
import java.io.File;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileDataSource extends jakarta.activation.FileDataSource
{
	@NonNull
	Filename name;
	@NonNull
	ContentType contentType;

	public static FileDataSource of(FSFile file)
	{
		return new FileDataSource(file.getFile(), file.getName(), file.getContentType());
	}

	public FileDataSource(File file, @NonNull Filename name, @NonNull ContentType contentType)
	{
		super(file);
		this.name = name;
		this.contentType = contentType;
	}

	@Override
	public String getName()
	{
		return name.getValue();
	}

	@Override
	public String getContentType()
	{
		return contentType.getValue();
	}
}
