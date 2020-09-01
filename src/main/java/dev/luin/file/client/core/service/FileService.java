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
package dev.luin.file.client.core.service;

import java.time.Instant;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.ws.soap.MTOM;

import dev.luin.file.client.core.download.DownloadStatus;
import dev.luin.file.client.core.jaxb.InstantAdapter;
import dev.luin.file.client.core.service.model.DownloadTask;
import dev.luin.file.client.core.service.model.File;
import dev.luin.file.client.core.service.model.FileInfo;
import dev.luin.file.client.core.service.model.UploadTask;
import dev.luin.file.client.core.upload.UploadStatus;

@MTOM(enabled=true)
@WebService(name = "FileService", targetNamespace="http://luin.dev/file/client/1.0", serviceName = "FileService", endpointInterface = "FileServiceSoapBinding", portName = "FileServicePort")
public interface FileService
{
	@WebResult(name="file")
	UploadTask uploadFile(@WebParam(name="creationUrl") @XmlElement(required=true) String creationUrl, @WebParam(name="file") @XmlElement(required=true) File file) throws ServiceException;
	@WebResult(name="uploadTask")
	UploadTask getUploadTask(@WebParam(name="fileId") @XmlElement(required=true) Long fileId) throws ServiceException;
	@WebResult(name="uploadTask")
	List<UploadTask> getUploadTasks(@WebParam(name="status") List<UploadStatus> status) throws ServiceException;
	void deleteUploadTask(@WebParam(name="fileId") @XmlElement(required=true) Long fileId) throws ServiceException;
	@WebResult(name="file")
	DownloadTask downloadFile(@WebParam(name="url") @XmlElement(required=true) String url,
			@WebParam(name="startDate") @XmlElement @XmlJavaTypeAdapter(InstantAdapter.class) Instant startDate,
			@WebParam(name="endDate") @XmlElement @XmlJavaTypeAdapter(InstantAdapter.class) Instant endDate) throws ServiceException;
	@WebResult(name="downloadTask")
	DownloadTask getDownloadTask(@WebParam(name="fileId") @XmlElement(required=true) Long fileId) throws ServiceException;
	@WebResult(name="downloadTask")
	List<DownloadTask> getDownloadTasks(@WebParam(name="status") List<DownloadStatus> status) throws ServiceException;
	void deleteDownloadTask(@WebParam(name="fileId") @XmlElement(required=true) Long fileId) throws ServiceException;
	@WebResult(name="file")
	File getFile(@WebParam(name="id") @XmlElement(required=true) Long id) throws ServiceException;
	@WebResult(name="file")
	FileInfo getFileInfo(@WebParam(name="id") @XmlElement(required=true) Long id) throws ServiceException;
	@WebResult(name="fileInfo")
	List<FileInfo> getFiles() throws ServiceException;
}
