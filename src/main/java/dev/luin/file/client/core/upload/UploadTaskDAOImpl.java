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

import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQueryFactory;

import dev.luin.file.client.core.file.FileId;
import dev.luin.file.client.core.upload.UploadStatus.Status;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Transactional(transactionManager = "dataSourceTransactionManager")
class UploadTaskDAOImpl implements UploadTaskDAO
{
	@NonNull
	SQLQueryFactory queryFactory;
	QUploadTask table = QUploadTask.uploadTask;
	Expression<?>[] uploadTaskColumns = {table.fileId,table.creationUrl,table.timestamp,table.status,table.statusTime,table.scheduleTime,table.retries};
	ConstructorExpression<UploadTask> uploadTaskProjection = Projections.constructor(UploadTask.class,uploadTaskColumns);
	DateTimePath<Instant> scheduleTime = Expressions.dateTimePath(Instant.class,"schedule_time");

	@Override
	public Option<UploadTask> getTask(FileId fileId)
	{
		return Option.of(queryFactory.select(uploadTaskProjection)
				.from(table)
				.where(table.fileId.eq(fileId))
				.fetchOne());
	}

	@Override
	public Option<UploadTask> getNextTask()
	{
		return Option.of(queryFactory.select(uploadTaskProjection)
				.from(table)
				.where(scheduleTime.before(Instant.now())
						.and(table.status.eq(Status.CREATED)))
				.orderBy(scheduleTime.asc())
				.fetchFirst());
	}

	@Override
	public Seq<UploadTask> getTasks()
	{
		return List.ofAll(queryFactory.select(uploadTaskProjection)
				.from(table)
				.orderBy(scheduleTime.desc())
				.fetch());
	}

	@Override
	public Seq<UploadTask> getTasks(List<Status> statuses)
	{
		return List.ofAll(queryFactory.select(uploadTaskProjection)
				.from(table)
				.where(table.status.in(statuses.asJava()))
				.orderBy(scheduleTime.asc())
				.fetch());
	}

	@Override
	public UploadTask insert(UploadTask task)
	{
		queryFactory.insert(table)
				.set(table.fileId,task.getFileId())
				.set(table.creationUrl,task.getCreationUrl())
				.set(table.timestamp,task.getTimestamp())
				.set(table.status,task.getStatus().getValue())
				.set(table.statusTime,task.getStatus().getTime())
				.set(table.scheduleTime,task.getScheduleTime())
				.set(table.retries,task.getRetries())
				.execute();
		return task;
	}

	@Override
	public long update(UploadTask task)
	{
		return queryFactory.update(table)
				.set(table.status,task.getStatus().getValue())
				.set(table.statusTime,task.getStatus().getTime())
				.set(table.scheduleTime,task.getScheduleTime())
				.set(table.retries,task.getRetries())
				.where(table.fileId.eq(task.getFileId()))
				.execute();
	}

	@Override
	public long delete(FileId fileId)
	{
		return queryFactory.delete(table)
				.where(table.fileId.eq(fileId))
				.execute();
	}
}
