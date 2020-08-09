package dev.luin.fc.core.download;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.querydsl.sql.SQLQueryFactory;

import dev.luin.fc.core.file.FileSystem;
import dev.luin.fc.core.security.KeyStore;
import dev.luin.fc.core.security.KeyStoreType;
import dev.luin.fc.core.security.TrustStore;
import dev.luin.fc.core.transaction.TransactionTemplate;
import dev.luin.fc.core.upload.SSLFactoryManager;
import lombok.AccessLevel;
import lombok.val;
import lombok.experimental.FieldDefaults;

@Configuration
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DownloadClientConfig
{
	@Autowired
	FileSystem fs;
	@Autowired
	@Qualifier("dataSourceTransactionTemplate")
	TransactionTemplate transactionTemplate;
	@Autowired
	SQLQueryFactory queryFactory;

	@Bean
	public DownloadTaskHandler downloadTaskHandler() throws GeneralSecurityException, IOException, Exception
	{
		val sslFactoryManager = SSLFactoryManager.builder()
				.keyStore(KeyStore.of(KeyStoreType.PKCS12,"dev/luin/fc/core/keystore.p12","password","password"))
				.trustStore(TrustStore.of(KeyStoreType.PKCS12,"dev/luin/fc/core/keystore.p12","password"))
				.enabledProtocols(new String[]{"TLSv1.2"})
				.enabledCipherSuites(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384","TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"})
				.verifyHostnames(true)
				.build();
		return new DownloadTaskHandler(sslFactoryManager,fs,transactionTemplate,downloadTaskManager());
	}

	@Bean
	public DownloadTaskManager downloadTaskManager()
	{
		return new DownloadTaskManager(downloadTaskDAO());
	}

	@Bean
	public DownloadTaskDAO downloadTaskDAO()
	{
		return new DownloadTaskDAOImpl(queryFactory);
	}
}
