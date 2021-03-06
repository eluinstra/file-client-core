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
package dev.luin.file.client.core.querydsl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import com.querydsl.sql.types.AbstractType;

import io.vavr.control.Option;

public class InstantType extends AbstractType<Instant>
{
	public InstantType(int type)
	{
		super(type);
	}

	@Override
	public Class<Instant> getReturnedClass()
	{
		return Instant.class;
	}

	@Override
	public Instant getValue(ResultSet rs, int startIndex) throws SQLException
	{
		return toInstant(rs.getTimestamp(startIndex));
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Instant value) throws SQLException
	{
		st.setTimestamp(startIndex,toTimestamp(value));
	}

	public static Instant toInstant(Timestamp timestamp)
	{
		return timestamp != null ? timestamp.toInstant() : null;
	}

	public static Option<Instant> toInstantOption(Timestamp timestamp)
	{
		return timestamp != null ? Option.of(timestamp.toInstant()) : Option.none();
	}

	public static Timestamp toTimestamp(Instant instant)
	{
		return instant != null ? Timestamp.from(instant) : null;
	}
}
