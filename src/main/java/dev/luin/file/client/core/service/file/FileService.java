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
package dev.luin.file.client.core.service.file;

import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.File;
import dev.luin.file.client.core.service.model.FileInfo;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.ws.soap.MTOM;
import java.util.List;

@MTOM(enabled = true)
@WebService(
		name = "FileService",
		targetNamespace = "http://luin.dev/file/client/1.0",
		serviceName = "FileService",
		endpointInterface = "FileServiceSoapBinding",
		portName = "FileServicePort")
public interface FileService
{
	@WebResult(name = "file")
	File downloadFile(@WebParam(name = "id") @XmlElement(required = true) Long id) throws ServiceException;

	@WebResult(name = "file")
	FileInfo
			downloadFileToFs(@WebParam(name = "id") @XmlElement(required = true) Long id, @WebParam(name = "filename") @XmlElement(required = true) String filename)
					throws ServiceException;

	@WebResult(name = "fileInfo")
	FileInfo getFileInfo(@WebParam(name = "id") @XmlElement(required = true) Long id) throws ServiceException;

	@WebResult(name = "fileInfo")
	List<FileInfo> getFiles() throws ServiceException;
}
