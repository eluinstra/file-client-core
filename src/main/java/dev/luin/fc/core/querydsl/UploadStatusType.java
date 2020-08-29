package dev.luin.fc.core.querydsl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.querydsl.sql.types.AbstractType;

import dev.luin.fc.core.upload.UploadStatus;

public class UploadStatusType extends AbstractType<UploadStatus>
{
	public UploadStatusType(int type)
	{
		super(type);
	}

	@Override
	public Class<UploadStatus> getReturnedClass()
	{
		return UploadStatus.class;
	}

	@Override
	public UploadStatus getValue(ResultSet rs, int startIndex) throws SQLException
	{
		return UploadStatus.values()[rs.getInt(startIndex)];
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, UploadStatus value) throws SQLException
	{
		st.setInt(startIndex,value.ordinal());
	}
}
