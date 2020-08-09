package dev.luin.fc.core.upload;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQueryFactory;

import dev.luin.fc.core.querydsl.model.QUploadTask;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UploadTaskDAOImpl implements UploadTaskDAO
{
	@NonNull
	SQLQueryFactory queryFactory;
	QUploadTask table = QUploadTask.uploadTask;
	Expression<?>[] uploadTaskColumns = {table.fileId,table.creationUrl,table.scheduleTime,table.retries};
	ConstructorExpression<UploadTask> uploadTaskProjection = Projections.constructor(UploadTask.class,uploadTaskColumns);

	@Override
	public Option<UploadTask> getNextTask()
	{
		return Option.of(queryFactory.select(uploadTaskProjection)
				.from(table)
				.fetchFirst());
	}

	@Override
	public UploadTask insert(UploadTask task)
	{
		val id = queryFactory.insert(table)
				.set(table.fileId,task.getFileId())
				.set(table.creationUrl,task.getCreationUrl())
				.set(table.scheduleTime,task.getScheduleTime())
				.set(table.retries,task.getRetries())
				.executeWithKey(Long.class);
		return task.withFileId(id);
	}

	@Override
	public long update(UploadTask task)
	{
		return queryFactory.update(table)
				.set(table.creationUrl,task.getCreationUrl())
				.set(table.scheduleTime,task.getScheduleTime())
				.set(table.retries,task.getRetries())
				.where(table.fileId.eq(task.getFileId()))
				.execute();
	}

	@Override
	public long delete(long fileId)
	{
		return queryFactory.delete(table)
				.where(table.fileId.eq(fileId))
				.execute();
	}
}
