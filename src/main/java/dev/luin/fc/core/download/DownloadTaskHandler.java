package dev.luin.fc.core.download;

import dev.luin.fc.core.upload.SSLFactoryManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class DownloadTaskHandler
{
	@NonNull
	SSLFactoryManager sslFactoryManager;
}
