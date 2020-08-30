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
package dev.luin.file.client.core.upload;

import java.net.URL;

import org.springframework.transaction.annotation.Transactional;

import com.querydsl.sql.SQLQueryFactory;

import dev.luin.file.client.core.file.QFile;
import io.tus.java.client.TusURLStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Transactional(transactionManager = "dataSourceTransactionManager")
public class TusUrlDAO implements TusURLStore
{
	SQLQueryFactory queryFactory;
	QFile table = QFile.file;

	@Override
	public void set(String id, URL url)
	{
		queryFactory.update(table)
				.set(table.url,url)
				.where(table.id.eq(Long.parseLong(id)));
	}

	@Override
	public URL get(String id)
	{
		return queryFactory.select(table.url)
				.from(table)
				.where(table.id.eq(Long.parseLong(id)))
				.fetchOne();
	}

	@Override
	public void remove(String id)
	{
		//TODO: replace?
//		queryFactory.delete(table)
//				.where(table.id.eq(Long.parseLong(id)));
	}
}
