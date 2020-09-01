package dev.luin.file.client.core.service.model;

import java.io.File;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileDataSource extends javax.activation.FileDataSource
{
	@NonNull
	String name;
	@NonNull
	String contentType;

	public FileDataSource(File file, @NonNull String name, String contentType)
	{
		super(file);
		this.name = name;
		this.contentType = contentType;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}
}
