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
package dev.luin.file.client.core.service.model;

import javax.activation.DataHandler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import dev.luin.file.client.core.file.FSFile;

@Mapper
public interface FileMapper
{
	public FileMapper INSTANCE = Mappers.getMapper(FileMapper.class);

	@Mapping(source = "file.name", target = "name")
	@Mapping(source = "content", target = "content")
	@Mapping(source = "file.contentType", target = "contentType")
	File toFile(FSFile file, DataHandler content);
}
