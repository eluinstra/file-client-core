package dev.luin.file.client.core.service.model;

import java.io.IOException;
import java.io.InputStream;

import dev.luin.file.client.core.file.NewFSFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(staticName = "of")
public class NewFSFileImpl implements NewFSFile
{
	File file;

	@Override
	public String getName()
	{
		return file.getContent().getName();
	}

	@Override
	public String getContentType()
	{
		return file.getContent().getContentType();
	}

	@Override
	public String getSha256Checksum()
	{
		return file.getSha256Checksum();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return file.getContent().getInputStream();
	}
}
