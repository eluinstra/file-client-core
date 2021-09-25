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

import java.time.Instant;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import dev.luin.file.client.core.jaxb.InstantAdapter;
import dev.luin.file.client.core.upload.UploadStatus.Status;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class UploadTask
{
	@XmlElement(required = true)
	long fileId;
	@XmlElement(required = true)
	@NonNull
	String creationUrl;
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	@NonNull
	Instant timestamp;
	@XmlElement(required = true)
	@NonNull
	Status status;
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	@NonNull
	Instant statusTime;
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(InstantAdapter.class)
	@XmlSchemaType(name = "dateTime")
	@NonNull
	Instant scheduleTime;
	@XmlElement(required = true)
	int retries;

	public UploadTask(dev.luin.file.client.core.upload.UploadTask task)
	{
		this.fileId = task.getFileId().getValue();
		this.creationUrl = task.getCreationUrl().getValue();
		this.timestamp = task.getTimestamp().getValue();
		this.status = task.getStatus().getValue();
		this.statusTime = task.getStatus().getTime();
		this.scheduleTime = task.getScheduleTime().getValue();
		this.retries = task.getRetries().getValue();
	}
}
