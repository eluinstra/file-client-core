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

import java.time.Instant;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import dev.luin.file.client.core.file.FSFile;
import dev.luin.file.client.core.jaxb.InstantAdapter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@XmlRootElement(name = "fileInfo")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class FileInfo
{
	@XmlElement
	Long id;
	@XmlElement(required = true)
	@NonNull
	String url;
	@XmlElement(required = true)
	//@NonNull
	String name;
	@XmlElement(required = true)
	//@NonNull
	String contentType;
	@XmlElement(required = true)
	//@NonNull
	String md5Checksum;
	@XmlElement(required = true)
	//@NonNull
	String sha256Checksum;
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	@NonNull
	Instant timestamp;
	@XmlElement
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	Instant startDate;
	@XmlElement
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	Instant endDate;
	@XmlElement(required = true)
	long length;
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	@NonNull
	Instant lastModified;
	@XmlElement(required = true)
	boolean completed;

	public FileInfo(FSFile file)
	{
		this.id = file.getId().getValue();
		this.url = file.getUrl().getValue();
		this.name = file.getName().getValue();
		this.contentType = file.getContentType().getValue();
		this.md5Checksum = file.getMd5Checksum().getValue();
		this.sha256Checksum = file.getSha256Checksum().getValue();
		this.timestamp = file.getTimestamp().getValue();
		this.length = file.getLength().getValue();
		this.lastModified = file.getLastModified();
		this.completed = file.isCompleted();
	}
}
