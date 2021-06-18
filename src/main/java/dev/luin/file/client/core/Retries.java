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
package dev.luin.file.client.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.Validate;

import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

//@Value
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Retries implements ValueObject<Integer>
{
	@NonNull
	AtomicInteger value;

	public Retries()
	{
		this(0);
	}

	public Retries(@NonNull final Integer retries)
	{
		value = Try.success(retries)
				.andThenTry(v -> Validate.isTrue(v.compareTo(0) >= 0))
				.map(AtomicInteger::new)
				.get();
	}

	public Retries increment()
	{
		return new Retries(value.incrementAndGet());
	}

	@Override
	public Integer getValue()
	{
		return value.get();
	}
}
