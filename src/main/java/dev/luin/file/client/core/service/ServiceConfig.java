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
package dev.luin.file.client.core.service;

import dev.luin.file.client.core.download.DownloadTaskManager;
import dev.luin.file.client.core.file.FileSystem;
import dev.luin.file.client.core.service.download.DownloadService;
import dev.luin.file.client.core.service.download.DownloadServiceImpl;
import dev.luin.file.client.core.service.file.AttachmentFactory;
import dev.luin.file.client.core.service.file.FileService;
import dev.luin.file.client.core.service.file.FileServiceImpl;
import dev.luin.file.client.core.service.upload.UploadService;
import dev.luin.file.client.core.service.upload.UploadServiceImpl;
import dev.luin.file.client.core.upload.UploadTaskManager;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceConfig
{
	@Value("${attachment.outputDirectory}")
	String attachmentOutputDirectory;
	@Value("${attachment.memoryTreshold}")
	int attachmentMemoryTreshold;
	@Value("${attachment.cipherTransformation}")
	String attachmentCipherTransformation;
	@Autowired
	FileSystem fs;
	@Value("${file.share.upload.location}")
	String shareUploadLocation;
	@Value("${file.share.download.location}")
	String shareDownloadLocation;

	@Bean
	@Autowired
	public UploadService uploadService(UploadTaskManager uploadTaskManager)
	{
		return new UploadServiceImpl(fs, Paths.get(shareUploadLocation).toAbsolutePath(), uploadTaskManager);
	}

	@Bean
	@Autowired
	public DownloadService downloadService(DownloadTaskManager downloadTaskManager)
	{
		return new DownloadServiceImpl(fs, downloadTaskManager);
	}

	@Bean
	public FileService fileService()
	{
		return new FileServiceImpl(fs, Paths.get(shareDownloadLocation).toAbsolutePath());
	}

	@Bean
	public void ebMSAttachmentFactory()
	{
		AttachmentFactory.init(attachmentOutputDirectory, attachmentMemoryTreshold, attachmentCipherTransformation);
	}

}
