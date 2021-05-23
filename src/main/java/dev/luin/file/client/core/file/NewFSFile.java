package dev.luin.file.client.core.file;

import java.io.IOException;
import java.io.InputStream;

public interface NewFSFile
{
	String getName();
	String getContentType();
	String getSha256Checksum();
	InputStream getInputStream() throws IOException;
}
