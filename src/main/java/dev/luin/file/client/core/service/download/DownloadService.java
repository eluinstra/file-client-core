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
package dev.luin.file.client.core.service.download;

import dev.luin.file.client.core.download.DownloadStatus;
import dev.luin.file.client.core.jaxb.InstantAdapter;
import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.DownloadTask;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.ws.soap.MTOM;
import java.time.Instant;
import java.util.List;

@MTOM(enabled = true)
@WebService(
		name = "DownloadService",
		targetNamespace = "http://luin.dev/file/client/1.0",
		serviceName = "DownloadService",
		endpointInterface = "DownloadServiceSoapBinding",
		portName = "DownloadServicePort")
public interface DownloadService
{
	@WebResult(name = "downloadTask")
	DownloadTask downloadFile(
			@WebParam(name = "url") @XmlElement(required = true) String url,
			@WebParam(name = "startDate") @XmlElement @XmlJavaTypeAdapter(InstantAdapter.class) Instant startDate,
			@WebParam(name = "endDate") @XmlElement @XmlJavaTypeAdapter(InstantAdapter.class) Instant endDate) throws ServiceException;

	@WebResult(name = "downloadTask")
	DownloadTask getDownloadTask(@WebParam(name = "fileId") @XmlElement(required = true) Long fileId) throws ServiceException;

	@WebResult(name = "downloadTask")
	List<DownloadTask> getDownloadTasks(@WebParam(name = "status") List<DownloadStatus.Status> status) throws ServiceException;

	void deleteDownloadTask(@WebParam(name = "fileId") @XmlElement(required = true) Long fileId) throws ServiceException;
}
