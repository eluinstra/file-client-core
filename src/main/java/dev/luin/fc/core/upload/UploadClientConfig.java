package dev.luin.fc.core.upload;

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
import io.tus.java.client.TusURLStore;
import lombok.AccessLevel;
import lombok.val;
import lombok.experimental.FieldDefaults;

@Configuration
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadClientConfig
{
	@Autowired
	FileSystem fs;
	@Autowired
	@Qualifier("dataSourceTransactionTemplate")
	TransactionTemplate transactionTemplate;
	@Autowired
	SQLQueryFactory queryFactory;

	@Bean
	public UploadTaskHandler uploadTaskHandler() throws GeneralSecurityException, IOException, Exception
	{
		val sslFactoryManager = SSLFactoryManager.builder()
				.keyStore(KeyStore.of(KeyStoreType.PKCS12,"dev/luin/fc/core/keystore.p12","password","password"))
				.trustStore(TrustStore.of(KeyStoreType.PKCS12,"dev/luin/fc/core/keystore.p12","password"))
				.enabledProtocols(new String[]{"TLSv1.2"})
				.enabledCipherSuites(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384","TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"})
				.verifyHostnames(true)
				.build();
		return new UploadTaskHandler(sslFactoryManager,fs,transactionTemplate,uploadTaskManager());
	}

	@Bean
	public UploadTaskManager uploadTaskManager()
	{
		return new UploadTaskManager(uploadTaskDAO(),tusDAO());
	}

	@Bean
	public UploadTaskDAO uploadTaskDAO()
	{
		return new UploadTaskDAOImpl(queryFactory);
	}

	@Bean
	public TusURLStore tusDAO()
	{
		return new TusUrlDAO(queryFactory);
	}
}
