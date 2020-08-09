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
package dev.luin.fc.core.service;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.soap.MTOM;

import dev.luin.fc.core.service.model.File;
import dev.luin.fc.core.service.model.FileInfo;

@MTOM(enabled=true)
@WebService(targetNamespace="http://luin.dev/fc/core/1.0")
public interface FileService
{
	@WebResult(name="id")
	FileInfo uploadFile(@WebParam(name="file") @XmlElement(required=true) File file, @WebParam(name="uploadUrl") @XmlElement(required=true) String uploadUrl) throws ServiceException;
	@WebResult(name="file")
	FileInfo downloadFile(@WebParam(name="url") @XmlElement(required=true) String url) throws ServiceException;
	@WebResult(name="file")
	File getFile(@WebParam(name="id") @XmlElement(required=true) Long id) throws ServiceException;
	@WebResult(name="fileInfo")
	List<FileInfo> getFileInfo() throws ServiceException;
	void deleteFile(@WebParam(name="id") @XmlElement(required=true) Long id, @WebParam(name="force") Boolean force) throws ServiceException;
}
