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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.querydsl.sql.types.AbstractType;

import dev.luin.file.client.core.file.ContentType;

class ContentTypeType extends AbstractType<ContentType>
{
	public ContentTypeType(int type)
	{
		super(type);
	}

	@Override
	public Class<ContentType> getReturnedClass()
	{
		return ContentType.class;
	}

	@Override
	public ContentType getValue(ResultSet rs, int startIndex) throws SQLException
	{
		return toContentType(rs.getString(startIndex));
	}

	private ContentType toContentType(String value)
	{
		return value == null ? null : new ContentType(value);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, ContentType value) throws SQLException
	{
		st.setString(startIndex,value.getValue());
	}
}
