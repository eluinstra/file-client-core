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
package dev.luin.file.client.core.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.luin.file.client.core.transaction.DataSourceTransactionTemplate;
import java.util.Arrays;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataSourceConfig
{
	public static final String BASEPATH = "classpath:/dev/luin/file/client/core/db/migration/";

	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@AllArgsConstructor
	@Getter
	public enum Location
	{
		DB2("jdbc:db2:", BASEPATH + "db2"),
		H2("jdbc:h2:", BASEPATH + "h2"),
		HSQLDB("jdbc:hsqldb:", BASEPATH + "hsqldb"),
		MARIADB("jdbc:mariadb:", BASEPATH + "mysql"),
		MSSQL("jdbc:sqlserver:", BASEPATH + "mssql"),
		MYSQL("jdbc:mysql:", BASEPATH + "mysql"),
		ORACLE("jdbc:oracle:", BASEPATH + "oracle"),
		POSTGRES("jdbc:postgresql:", BASEPATH + "postgresql");

		String jdbcUrl;
		String location;

		public static Optional<String> getLocation(String jdbcUrl)
		{
			return Arrays.stream(values()).filter(l -> jdbcUrl.startsWith(l.jdbcUrl)).map(l -> l.location).findFirst();
		}
	}

	@Value("${jdbc.driverClassName}")
	String driverClassName;
	@Value("${jdbc.url}")
	String jdbcUrl;
	@Value("${jdbc.username}")
	String username;
	@Value("${jdbc.password}")
	String password;
	@Value("${jdbc.pool.autoCommit}")
	boolean isAutoCommit;
	@Value("${jdbc.pool.connectionTimeout}")
	int connectionTimeout;
	@Value("${jdbc.pool.maxIdleTime}")
	int maxIdleTime;
	@Value("${jdbc.pool.maxLifetime}")
	int maxLifetime;
	@Value("${jdbc.pool.testQuery}")
	String testQuery;
	@Value("${jdbc.pool.minPoolSize}")
	int minPoolSize;
	@Value("${jdbc.pool.maxPoolSize}")
	int maxPoolSize;

	@Bean("dataSourceTransactionTemplate")
	public DataSourceTransactionTemplate dataSourceTransactionTemplate()
	{
		return new DataSourceTransactionTemplate();
	}

	@Bean(destroyMethod = "close")
	public DataSource hikariDataSource()
	{
		val config = new HikariConfig();
		config.setDriverClassName(driverClassName);
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		config.setAutoCommit(isAutoCommit);
		config.setConnectionTimeout(connectionTimeout);
		config.setIdleTimeout(maxIdleTime);
		config.setMaxLifetime(maxLifetime);
		config.setConnectionTestQuery(testQuery);
		config.setMinimumIdle(minPoolSize);
		config.setMaximumPoolSize(maxPoolSize);
		return new HikariDataSource(config);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void flyway()
	{
		val locations = Location.getLocation(jdbcUrl);
		locations.ifPresent(l ->
		{
			val config = Flyway.configure().dataSource(jdbcUrl, username, password).locations(l).ignoreMigrationPatterns("*:missing");
			config.load().migrate();
		});
	}
}
