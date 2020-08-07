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
package dev.luin.fc.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import dev.luin.fc.core.datasource.DataSourceConfig;
import dev.luin.fc.core.download.DownloadClientConfig;
import dev.luin.fc.core.file.FileSystemConfig;
import dev.luin.fc.core.querydsl.QueryDSLConfig;
import dev.luin.fc.core.service.ServiceConfig;
import dev.luin.fc.core.transaction.TransactionManagerConfig;
import dev.luin.fc.core.upload.UploadClientConfig;

@Configuration
@Import({
	DataSourceConfig.class,
	DownloadClientConfig.class,
	FileSystemConfig.class,
	QueryDSLConfig.class,
	ServiceConfig.class,
	TransactionManagerConfig.class,
	UploadClientConfig.class
})
@PropertySource(value = {"classpath:dev/luin/fc/core/default.properties"}, ignoreResourceNotFound = true)
public class MainConfig
{
	public static void main(String[] args)
	{
		try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MainConfig.class))
		{
			
		}
	}
}
