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
package dev.luin.file.client.core.file;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQueryFactory;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Transactional(transactionManager = "dataSourceTransactionManager")
class FSFileDAOImpl implements FSFileDAO
{
	@NonNull
	SQLQueryFactory queryFactory;
	QFile table = QFile.file;
	Expression<?>[] fsFileColumns =
			{table.id, table.url, table.path, table.name, table.contentType, table.md5Checksum, table.sha256Checksum, table.timestamp, table.length};
	ConstructorExpression<FSFile> fsFileProjection = Projections.constructor(FSFile.class, fsFileColumns);

	@Override
	public Option<FSFile> findFile(final FileId id)
	{
		return Option.of(queryFactory.select(fsFileProjection).from(table).where(table.id.eq(id)).fetchOne());
	}

	@Override
	public Option<FSFile> findFile(final Url url)
	{
		return Option.of(queryFactory.select(fsFileProjection).from(table).where(table.url.eq(url)).fetchOne());
	}

	@Override
	public Seq<FSFile> selectFiles()
	{
		val namePath = Expressions.comparablePath(String.class, "name");
		return List.ofAll(queryFactory.select(fsFileProjection).from(table).orderBy(namePath.asc()).fetch());
	}

	@Override
	public FSFile insertFile(@NonNull final FSFile fsFile)
	{
		val id = queryFactory.insert(table)
				.set(table.url, fsFile.getUrl())
				.set(table.path, fsFile.getPath())
				.set(table.name, fsFile.getName())
				.set(table.contentType, fsFile.getContentType())
				.set(table.md5Checksum, fsFile.getMd5Checksum())
				.set(table.sha256Checksum, fsFile.getSha256Checksum())
				.set(table.timestamp, fsFile.getTimestamp())
				.set(table.length, fsFile.getLength())
				.executeWithKey(FileId.class);
		return fsFile.withId(id);
	}

	@Override
	public long updateFile(@NonNull FSFile fsFile)
	{
		return queryFactory.update(table)
				.set(table.url, fsFile.getUrl())
				.set(table.name, fsFile.getName())
				.set(table.contentType, fsFile.getContentType())
				.set(table.md5Checksum, fsFile.getMd5Checksum())
				.set(table.sha256Checksum, fsFile.getSha256Checksum())
				.set(table.length, fsFile.getLength())
				.where(table.id.eq(fsFile.getId()))
				.execute();
	}

	@Override
	public long deleteFile(final FileId id)
	{
		return queryFactory.delete(table).where(table.id.eq(id)).execute();
	}
}
