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
package dev.luin.fc.core.file;

import java.util.List;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQueryFactory;

import dev.luin.fc.core.querydsl.model.QFile;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class FSFileDAOImpl implements FSFileDAO
{
	@NonNull
	SQLQueryFactory queryFactory;
	QFile table = QFile.file;
	Expression<?>[] fsFileColumns = {table.id,table.url,table.realPath,table.filename,table.contentType,table.md5Checksum,table.sha256Checksum,table.startDate,table.endDate,table.fileLength,table.fileType};
	ConstructorExpression<FSFile> fsFileProjection = Projections.constructor(FSFile.class,fsFileColumns);

	@Override
	public Option<FSFile> findFile(final long id)
	{
		return Option.of(queryFactory.select(fsFileProjection)
				.from(table)
				.where(table.id.eq(id))
				.fetchOne());
	}

	@Override
	public List<String> selectFiles()
	{
		return queryFactory.select(table.url)
				.from(table)
				.fetch();
	}

	@Override
	public long insertFile(@NonNull final FSFile fsFile)
	{
		return queryFactory.insert(table)
				.set(table.url,fsFile.getUrl())
				.set(table.realPath,fsFile.getRealPath())
				.set(table.filename,fsFile.getName())
				.set(table.contentType,fsFile.getContentType())
				.set(table.md5Checksum,fsFile.getMd5Checksum())
				.set(table.sha256Checksum,fsFile.getSha256Checksum())
				.set(table.startDate,fsFile.getStartDate())
				.set(table.endDate,fsFile.getEndDate())
				.set(table.fileLength,fsFile.getFileLength())
				.set(table.fileType,fsFile.getFileType())
				.executeWithKey(Long.class);
	}

	@Override
	public long updateFile(@NonNull FSFile fsFile)
	{
		return queryFactory.update(table)
				.set(table.url,fsFile.getUrl())
				.set(table.md5Checksum,fsFile.getMd5Checksum())
				.set(table.sha256Checksum,fsFile.getSha256Checksum())
				.set(table.fileLength,fsFile.getFileLength())
				.where(table.id.eq(fsFile.getId()))
				.execute();
	}

	@Override
	public long deleteFile(final long id)
	{
		return queryFactory.delete(table)
				.where(table.id.eq(id))
				.execute();
	}
}
