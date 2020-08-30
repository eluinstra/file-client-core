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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.querydsl.sql.types.AbstractType;

import lombok.val;

public class UrlType extends AbstractType<URL>
{
	public UrlType(int type)
	{
		super(type);
	}

	@Override
	public Class<URL> getReturnedClass()
	{
		return URL.class;
	}

	@Override
	public URL getValue(ResultSet rs, int startIndex) throws SQLException
	{
		try
		{
			val url = rs.getString(startIndex);
			return url != null ? new URL(url) : null;
		}
		catch (MalformedURLException e)
		{
			throw new SQLException(e);
		}
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, URL value) throws SQLException
	{
		st.setString(startIndex,value != null ? value.toString() : null);
	}
}
