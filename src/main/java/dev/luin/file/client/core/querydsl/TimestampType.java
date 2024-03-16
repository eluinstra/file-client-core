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
package dev.luin.file.client.core.querydsl;

import com.querydsl.sql.types.AbstractType;
import dev.luin.file.client.core.file.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

class TimestampType extends AbstractType<Timestamp>
{
	public TimestampType(int type)
	{
		super(type);
	}

	@Override
	public Class<Timestamp> getReturnedClass()
	{
		return Timestamp.class;
	}

	@Override
	public Timestamp getValue(ResultSet rs, int startIndex) throws SQLException
	{
		return new Timestamp(rs.getTimestamp(startIndex).toLocalDateTime().toInstant(ZoneOffset.UTC));
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Timestamp value) throws SQLException
	{
		st.setTimestamp(startIndex, java.sql.Timestamp.valueOf(LocalDateTime.ofInstant(value.getValue(), ZoneOffset.UTC)));
	}
}
