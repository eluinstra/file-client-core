/**
 * Copyright 2020 E.Luinstra
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.luin.fc.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import io.vavr.Function1;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@AllArgsConstructor
public class FileSystem
{
	public static final Function1<String,File> getFile = path -> Paths.get(path).toFile();
	@NonNull
	FSFileDAO fsFileDAO;
	@NonNull
	String baseDir;
	int filenameLength;

	public Option<FSFile> findFile(final long id)
	{
		return fsFileDAO.findFile(id);
	}

	public DataSource createDataSource(FSFile fsFile)
	{
		return new FileDataSource(fsFile.getFile());
	}

	public List<String> getFiles()
	{
		return fsFileDAO.selectFiles();
	}

	public FSFile createFile(
			@NonNull final String url,
			final String filename,
			@NonNull final String contentType,
			final String sha256checksum,
			final Instant startDate,
			final Instant endDate,
			@NonNull final Long userId,
			@NonNull final InputStream content) throws IOException
	{
		val realPath = createRandomFile().get();
		val file = getFile.apply(realPath);
		Try.of(() -> write(content,file)).getOrElseThrow(e -> new IOException("Error writing to file " + realPath,e));
		val calculatedSha256Checksum = calculateSha256Checksum(file);
		if (validateChecksum(sha256checksum,calculatedSha256Checksum))
		{
			val md5Checksum = calculateMd5Checksum(file);
			val result = FSFile.builder()
					.url(url)
					.realPath(realPath)
					.name(filename)
					.contentType(contentType)
					.md5Checksum(md5Checksum)
					.sha256Checksum(calculatedSha256Checksum)
					.startDate(startDate)
					.endDate(endDate)
					.fileLength(file.length())
					.build();
			fsFileDAO.insertFile(result);
			return result;
		}
		else
			throw new IOException("Checksum error for file " + filename + ". Checksum of the file uploaded (" + calculatedSha256Checksum + ") is not equal to the provided checksum (" + sha256checksum + ")");
	}
	
	public FSFile createEmptyFile(
			@NonNull final String url,
			final String filename,
			@NonNull final String contentType,
			final FileType fileType,
			final Long fileLength,
			@NonNull final Long userId) throws IOException
	{
		val realPath = createRandomFile().get();
		val result = FSFile.builder()
				.url(url)
				.realPath(realPath)
				.name(filename)
				.contentType(contentType)
				.fileLength(fileLength)
				.fileType(fileType)
				.build();
		fsFileDAO.insertFile(result);
		return result;
	}

	public FSFile append(@NonNull final FSFile fsFile, @NonNull final InputStream input, final Long length) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists() || fsFile.isCompleted())
			throw new FileNotFoundException(fsFile.getUrl());
		try (val output = new FileOutputStream(file,true))
		{
			if (length != null)
				IOUtils.copyLarge(input,output,0,length);
			else
				IOUtils.copyLarge(input,output);
			if (fsFile.isCompleted())
				completeFile(fsFile);
			fsFileDAO.updateFile(fsFile);
			return fsFile;
		}
	}

	public long write(@NonNull final FSFile fsFile, @NonNull final OutputStream output) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists() || !fsFile.isCompleted())
			throw new FileNotFoundException(fsFile.getUrl());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	public long write(@NonNull final FSFile fsFile, @NonNull final OutputStream output, final long first, final long length) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists() || !fsFile.isCompleted())
			throw new FileNotFoundException(fsFile.getUrl());
		try (val input = new FileInputStream(file))
		{
			return IOUtils.copyLarge(input,output,first,length);
		}
	}

	public boolean deleteFile(@NonNull final FSFile fsFile, final boolean force)
	{
		val result = Try.of(() -> fsFile.getFile().delete()).onFailure(t -> log.error("",t));
		if (force || result.isSuccess())
			fsFileDAO.deleteFile(fsFile.getId());
		return force || result.getOrElse(false);
	}

	private Try<String> createRandomFile()
	{
		var result = (Path)null;
		try
		{
			while (true)
			{
				val filename = RandomStringUtils.randomNumeric(filenameLength);
				result = Paths.get(baseDir,filename);
				if (result.toFile().createNewFile())
					return Try.success(result.toString());
			}
		}
		catch (IOException e)
		{
			return Try.failure(new IOException("Error creating file " + result,e));
		}
	}

	private long write(final InputStream input, final File file) throws IOException
	{
		try (val output = new FileOutputStream(file))
		{
			return IOUtils.copyLarge(input,output);
		}
	}

	private String calculateMd5Checksum(File file) throws IOException
	{
		try (val is = new FileInputStream(file))
		{
			return DigestUtils.md5Hex(is);
		}
	}

	private boolean validateChecksum(final String checksum, final String calculatedChecksum)
	{
		return StringUtils.isEmpty(checksum) || checksum.equalsIgnoreCase(calculatedChecksum);
	}

	private String calculateSha256Checksum(final File file) throws IOException
	{
		try (val is = new FileInputStream(file))
		{
			return DigestUtils.sha256Hex(is);
		}
	}

	private FSFile completeFile(@NonNull final FSFile fsFile) throws IOException
	{
		val file = fsFile.getFile();
		if (!file.exists())// || !fsFile.isCompleted())
			throw new FileNotFoundException(fsFile.getUrl());
		val result = fsFile
				.withSha256Checksum(calculateSha256Checksum(file))
				.withMd5Checksum(calculateMd5Checksum(file));
		fsFileDAO.updateFile(result);
		return result;
	}
}
