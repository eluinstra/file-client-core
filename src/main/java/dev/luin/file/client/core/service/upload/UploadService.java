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
package dev.luin.file.client.core.service.upload;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.soap.MTOM;

import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.NewFile;
import dev.luin.file.client.core.service.model.UploadTask;
import dev.luin.file.client.core.upload.UploadStatus;

@MTOM(enabled = true)
@WebService(name = "UploadService", targetNamespace = "http://luin.dev/file/client/1.0", serviceName = "UploadService", endpointInterface = "UploadServiceSoapBinding", portName = "UploadServicePort")
public interface UploadService
{
	@WebResult(name = "uploadTask")
	UploadTask uploadFile(@WebParam(name = "creationUrl") @XmlElement(required = true) String creationUrl, @WebParam(name = "file") @XmlElement(required = true) NewFile file) throws ServiceException;

	@WebResult(name = "uploadTask")
	UploadTask getUploadTask(@WebParam(name = "fileId") @XmlElement(required = true) Long fileId) throws ServiceException;

	@WebResult(name = "uploadTask")
	List<UploadTask> getUploadTasks(@WebParam(name = "status") List<UploadStatus.Status> status) throws ServiceException;

	void deleteUploadTask(@WebParam(name = "fileId") @XmlElement(required = true) Long fileId) throws ServiceException;
}
