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
package dev.luin.file.client.core.upload;

import dev.luin.file.client.core.ValueObject;
import jakarta.xml.bind.annotation.XmlType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class UploadStatus implements ValueObject<UploadStatus.Status>
{
	@XmlType(name = "UploadStatusStatus")
	public enum Status
	{
		CREATED, SUCCEEDED, FAILED;
	}

	@NonNull
	Status value;
	@NonNull
	Instant time;

	public UploadStatus(@NonNull Status status)
	{
		this(status, Instant.now());
	}
}
