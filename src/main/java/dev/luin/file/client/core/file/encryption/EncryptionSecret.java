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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luin.file.client.core.ValueObject;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.Value;

@Value
public class EncryptionSecret implements ValueObject<String>
{
	@NonNull
	byte[] key;
	@NonNull
	byte[] iv;

	public static EncryptionSecret create(String value)
	{
		return Try.of(() -> new ObjectMapper().readValue(value, EncryptionSecret.class)).get();
	}

	@Override
	@JsonIgnore
	public String getValue()
	{
		return Try.of(() -> new ObjectMapper().writeValueAsString(this)).get();
	}
}
