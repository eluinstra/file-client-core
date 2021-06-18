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

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;

import dev.luin.file.client.core.file.FSFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class File
{
	@XmlElement(required=true)
	Long id;
	@XmlElement
	String sha256Checksum;
	@XmlMimeType("application/octet-stream")
	@XmlElement(required=true)
	@NonNull
	@ToString.Exclude
	DataHandler content;

	public File(FSFile file, DataHandler content)
	{
		this.id = file.getId().getValue();
		this.sha256Checksum = file.getSha256Checksum().getValue();
		this.content = content;
	}

	public File(NewFile newFile)
	{
		this.sha256Checksum = newFile.getSha256Checksum();
		this.content = newFile.getContent();
	}
}
