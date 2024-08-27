/*
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
package dev.luin.file.client.core.file;

import dev.luin.file.client.core.file.encryption.EncryptionSecret;
import dev.luin.file.client.core.file.encryption.EncryptionService;
import dev.luin.file.client.core.service.model.InputStreamDataSource;
import io.vavr.CheckedConsumer;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.activation.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class FileSystem
{
	@NonNull
	FSFileDAO fsFileDAO;
	@NonNull
	RandomFileGenerator randomFileGenerator;
	@NonNull
	EncryptionService encryptionService;

	public Option<FSFile> findFile(final FileId id)
	{
		return fsFileDAO.findFile(id);
	}

	public Seq<FSFile> getFiles()
	{
		return fsFileDAO.selectFiles();
	}

	public FSFile createEncryptedFile(@NonNull final NewFSFile newFile) throws IOException
	{
		val algorithm = encryptionService.getDefaultAlgorithm();
		val secret = encryptionService.generateSecret(algorithm);
		return randomFileGenerator.create()
				.flatMap(encryptFile(newFile, secret))
				.andThenTry(validateSha256Checksum(newFile))
				.map(
						tuple -> FSFile.builder()
								.path(tuple._1.getPath())
								.name(newFile.getName())
								.contentType(newFile.getContentType())
								.encryptionAlgorithm(algorithm)
								.encryptionSecret(secret)
								.md5Checksum(new Md5Checksum(tuple._2.digest()))
								.sha256Checksum(new Sha256Checksum(tuple._3.digest()))
								.timestamp(new Timestamp())
								.length(tuple._1.getLength())
								.build())
				.get();
	}

	private CheckedConsumer<Tuple3<RandomFile, MessageDigest, MessageDigest>> validateSha256Checksum(final NewFSFile newFile)
	{
		return tuple ->
		{
			val calculatedSha256Checksum = new Sha256Checksum(tuple._3.digest());
			if (newFile.getSha256Checksum() != null && !calculatedSha256Checksum.validate(newFile.getSha256Checksum()))
				throw new IOException(
						"Checksum error for file "
								+ newFile.getName()
								+ ". Checksum of the file uploaded ("
								+ calculatedSha256Checksum
								+ ") is not equal to the provided checksum ("
								+ newFile.getSha256Checksum()
								+ ")");
		};
	}

	private final Function1<RandomFile, Try<Tuple3<RandomFile, MessageDigest, MessageDigest>>> encryptFile(NewFSFile newFile, EncryptionSecret secret)
	{
		try
		{
			val md5 = Md5Checksum.messageDigest();
			val sha256 = Sha256Checksum.messageDigest();
			return file -> Try.success(file)
					.flatMapTry(
							f -> f.write(encryptionService.encryptionInputStream(new DigestInputStream(new DigestInputStream(newFile.getInputStream(), md5), sha256), secret))
									.map(x -> Tuple.of(file, md5, sha256)));
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public FSFile createEmptyFile(@NonNull final String url) throws IOException
	{
		val randomFile = randomFileGenerator.create().get();
		val result = FSFile.builder().url(new Url(url)).path(randomFile.getPath()).timestamp(new Timestamp()).build();
		return fsFileDAO.insertFile(result);
	}

	public boolean updateFile(FSFile file)
	{
		return fsFileDAO.updateFile(file) > 0;
	}

	public FSFile append(@NonNull FSFile fsFile, @NonNull final InputStream input) throws IOException
	{
		val result = fsFile.append(input);
		if (result.isCompleted())
			fsFileDAO.updateFile(result);
		return result;
	}

	public FSFile append(@NonNull final FSFile fsFile, @NonNull final InputStream input, final long first, final long length) throws IOException
	{
		val result = fsFile.append(input, first, length);
		if (result.isCompleted())
			fsFileDAO.updateFile(result);
		return result;
	}

	public boolean deleteFile(@NonNull final FSFile fsFile, final boolean force)
	{
		val result = Try.of(() -> Files.deleteIfExists(fsFile.getFile().toPath())).onFailure(t -> log.error("", t));
		if (force || result.isSuccess())
			fsFileDAO.deleteFile(fsFile.getId());
		return force || result.getOrElse(false);
	}

	public DataSource toDecryptedDataSource(FSFile f)
	{
		try
		{
			val in = encryptionService.decryptionInputStream(f.getEncryptionAlgorithm(), new FileInputStream(f.getFile()), f.getEncryptionSecret());
			return new InputStreamDataSource(in, f.getName(), f.getContentType());
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public Consumer<FSFile> decryptToFile(Path filename)
	{
		// TODO handle exceptions
		return f -> Try.withResources(() -> decryptionInputStream(f), () -> new FileOutputStream(filename.toFile())).of(InputStream::transferTo);
	}

	private InputStream decryptionInputStream(FSFile f) throws FileNotFoundException
	{
		return encryptionService.decryptionInputStream(f.getEncryptionAlgorithm(), new FileInputStream(f.getFile()), f.getEncryptionSecret());
	}
}
