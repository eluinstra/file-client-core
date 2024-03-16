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
package dev.luin.file.client.core;

import java.time.Duration;
import java.time.Instant;
import lombok.NonNull;
import lombok.Value;

@Value
public class ScheduleTime implements ValueObject<Instant>
{
	@NonNull
	Instant value;

	public ScheduleTime()
	{
		this(Instant.now());
	}

	public ScheduleTime(@NonNull Instant timestamp)
	{
		value = timestamp;
	}

	public boolean isAfter(Instant instant)
	{
		return value.isAfter(instant);
	}

	public ScheduleTime plus(Duration duration)
	{
		return new ScheduleTime(value.plus(duration));
	}
}
