package dev.luin.fc.core.service.model;

import java.time.Instant;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class DownloadTask
{
	@XmlElement(required=true)
	@NonNull
	String url;
	@XmlElement
	Instant startDate;
	@XmlElement
	Instant endDate;
	@XmlElement(required=true)
	long fileId;
	@XmlElement(required=true)
	@NonNull
	Instant scheduleTime;
	@XmlElement(required=true)
	int retries;
}
