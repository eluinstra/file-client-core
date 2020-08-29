package dev.luin.fc.core.querydsl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.querydsl.sql.types.AbstractType;

import dev.luin.fc.core.download.DownloadStatus;

public class DownloadStatusType extends AbstractType<DownloadStatus>
{
	public DownloadStatusType(int type)
	{
		super(type);
	}

	@Override
	public Class<DownloadStatus> getReturnedClass()
	{
		return DownloadStatus.class;
	}

	@Override
	public DownloadStatus getValue(ResultSet rs, int startIndex) throws SQLException
	{
		return DownloadStatus.values()[rs.getInt(startIndex)];
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, DownloadStatus value) throws SQLException
	{
		st.setInt(startIndex,value.ordinal());
	}
}
