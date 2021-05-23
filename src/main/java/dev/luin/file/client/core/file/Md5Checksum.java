package dev.luin.file.client.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.val;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Md5Checksum
{
	String value;

	public static Md5Checksum of(String checksum)
	{
		return new Md5Checksum(checksum);
	}

	public static Md5Checksum of(File file) throws IOException
	{
		try (val is = new FileInputStream(file))
		{
			return new Md5Checksum(DigestUtils.md5Hex(is));
		}
	}
}
