package dev.luin.file.client.core.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.Value;
import lombok.val;

@Value
public class RandomFile
{
	String path;
	File file;

	public static Try<RandomFile> create(String baseDir, int filenameLength)
	{
		while (true)
		{
			val path = createRandomPath(baseDir, filenameLength);
			try
			{
				val file = createFile(path);
				if (file.isSingleValued())
					return Try.success(file.get());
			}
			catch (IOException e)
			{
				return Try.failure(new IOException("Error creating file " + path,e));
			}
		}
	}
	
	private static Path createRandomPath(String baseDir, int filenameLength)
	{
		val filename = RandomStringUtils.randomNumeric(filenameLength);
		return Paths.get(baseDir,filename);
	}
	
	private static Option<RandomFile> createFile(Path path) throws IOException
	{
		if (path.toFile().createNewFile())
			return Option.some(new RandomFile(path.toString()));
		else
			return Option.none();
	}

	private RandomFile(String path)
	{
		this.path = path;
		file = FSFile.getFile.apply(path);
	}

	public long write(final InputStream input) throws IOException
	{
		try (val output = new FileOutputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
		catch(IOException e)
		{
			throw new IOException("Error writing to file " + path,e);
		}
	}
}
