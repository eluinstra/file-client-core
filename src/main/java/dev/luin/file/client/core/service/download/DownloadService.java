package dev.luin.file.client.core.service.download;

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
import dev.luin.file.client.core.service.ServiceException;
import dev.luin.file.client.core.service.model.DownloadTask;

@MTOM(enabled = true)
@WebService(name = "DownloadService", targetNamespace = "http://luin.dev/file/client/1.0", serviceName = "DownloadService", endpointInterface = "DownloadServiceSoapBinding", portName = "DownloadServicePort")
public interface DownloadService
{
	@WebResult(name = "downloadTask")
	DownloadTask downloadFile(@WebParam(name = "url") @XmlElement(required = true) String url,
			@WebParam(name = "startDate") @XmlElement @XmlJavaTypeAdapter(InstantAdapter.class) Instant startDate,
			@WebParam(name = "endDate") @XmlElement @XmlJavaTypeAdapter(InstantAdapter.class) Instant endDate) throws ServiceException;

	@WebResult(name = "downloadTask")
	DownloadTask getDownloadTask(@WebParam(name = "fileId") @XmlElement(required = true) Long fileId) throws ServiceException;

	@WebResult(name = "downloadTask")
	List<DownloadTask> getDownloadTasks(@WebParam(name = "status") List<DownloadStatus.Status> status) throws ServiceException;

	void deleteDownloadTask(@WebParam(name = "fileId") @XmlElement(required = true) Long fileId) throws ServiceException;
}
