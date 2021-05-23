package dev.luin.file.client.core.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.val;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sha256Checksum
{
	String value;

	public static Sha256Checksum of(String checksum)
	{
		return new Sha256Checksum(checksum);
	}

	public static Sha256Checksum of(final File file) throws IOException
	{
		try (val is = new FileInputStream(file))
		{
			return new Sha256Checksum(DigestUtils.sha256Hex(is));
		}
	}

	public boolean validate(final String checksum)
	{
		return StringUtils.isEmpty(value) || value.equalsIgnoreCase(checksum);
	}
}
