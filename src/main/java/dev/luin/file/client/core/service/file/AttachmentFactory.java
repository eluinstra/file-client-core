package dev.luin.file.client.core.service.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.io.CachedOutputStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentFactory
{
	public static void init(String attachmentOutputDirectory, int attachmentMemoryTreshold, String attachmentCipherTransformation)
	{
		if (StringUtils.isNotEmpty(attachmentOutputDirectory))
			System.setProperty("org.apache.cxf.io.CachedOutputStream.OutputDirectory",attachmentOutputDirectory);
		CachedOutputStream.setDefaultThreshold(attachmentMemoryTreshold);
		if (StringUtils.isNotEmpty(attachmentCipherTransformation))
			CachedOutputStream.setDefaultCipherTransformation(attachmentCipherTransformation);
	}

}
