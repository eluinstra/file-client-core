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

import com.querydsl.sql.types.AbstractType;

import dev.luin.file.client.core.file.Url;
import lombok.val;

public class UrlType extends AbstractType<Url>
{
	public UrlType(int type)
	{
		super(type);
	}

	@Override
	public Class<Url> getReturnedClass()
	{
		return Url.class;
	}

	@Override
	public Url getValue(ResultSet rs, int startIndex) throws SQLException
	{
		val url = rs.getString(startIndex);
		return url != null ? new Url(url) : null;
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Url value) throws SQLException
	{
		st.setString(startIndex,value != null ? value.getValue() : null);
	}
}
