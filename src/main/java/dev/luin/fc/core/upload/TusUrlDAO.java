package dev.luin.fc.core.upload;

import java.net.URL;

import com.querydsl.sql.SQLQueryFactory;

import dev.luin.fc.core.querydsl.model.QFile;
import io.tus.java.client.TusURLStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
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
		queryFactory.delete(table)
				.where(table.id.eq(Long.parseLong(id)));
	}
}
