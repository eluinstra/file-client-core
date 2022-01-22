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

import com.querydsl.sql.SQLQueryFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileSystemConfig
{
	@Value("${file.baseDir}")
	String baseDir;
	@Value("${file.filenameLength}")
	int filenameLength;

	@Bean
	public FileSystem fileSystem(@Autowired FSFileDAO fsFileDAO)
	{
		return FileSystem.builder()
				.fsFileDAO(fsFileDAO)
				.baseDir(baseDir)
				.filenameLength(filenameLength)
				.build();
	}

	@Bean
	public FSFileDAO fsFileDAO(@Autowired SQLQueryFactory queryFactory)
	{
		return new FSFileDAOImpl(queryFactory);
	}
}
