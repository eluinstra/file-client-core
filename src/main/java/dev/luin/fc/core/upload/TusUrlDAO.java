package dev.luin.fc.core.upload;

import java.net.URL;

import com.querydsl.sql.SQLQueryFactory;

import io.tus.java.client.TusURLStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TusUrlDAO implements TusURLStore
{
	SQLQueryFactory queryFactory;

	@Override
	public void set(String fingerprint, URL url)
	{
		
	}

	@Override
	public URL get(String fingerprint)
	{
		return null;
	}

	@Override
	public void remove(String fingerprint)
	{
		
	}
}
