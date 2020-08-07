package dev.luin.fc.core.upload;

import java.net.MalformedURLException;
import java.net.URL;

import com.querydsl.sql.SQLQueryFactory;

import dev.luin.fc.core.querydsl.model.QFile;
import io.tus.java.client.TusURLStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TusUrlDAO implements TusURLStore
{
	SQLQueryFactory queryFactory;
	QFile table;

	@Override
	public void set(String id, URL url)
	{
		queryFactory.update(table)
				.set(table.url,url.toString())
				.where(table.id.eq(Long.parseLong(id)));
	}

	@Override
	public URL get(String id)
	{
		try
		{
			val result = queryFactory.select(table.url)
					.from(table)
					.where(table.id.eq(Long.parseLong(id)))
					.fetchOne();
			return result != null ? new URL(result) : null;
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	@Override
	public void remove(String id)
	{
		queryFactory.delete(table)
				.where(table.id.eq(Long.parseLong(id)));
	}
}
