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
package dev.luin.file.client.core.download;

import java.time.Instant;

import lombok.Value;
import lombok.val;

@Value
public class TimeFrame
{
	Instant startDate;
	Instant endDate;

	public TimeFrame(final Instant startDate, final Instant endDate)
	{
		if (startDate != null && endDate != null && !startDate.isBefore(endDate))
			throw new IllegalStateException("StartDate not before EndDate");
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean hasTimeFrame()
	{
		return startDate != null || endDate != null;
	}

	public boolean isValid()
	{
		val now = Instant.now();
		return (startDate == null || startDate.compareTo(now) <= 0
				&& endDate == null || endDate.compareTo(now) > 0);
	}
}
