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
package dev.luin.fc.core.datasource;

import java.util.Arrays;
import java.util.Optional;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dev.luin.fc.core.transaction.DataSourceTransactionTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataSourceConfig
{
	public static final String BASEPATH = "classpath:/dev/luin/fc/core/db/migration/";

	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@AllArgsConstructor
	@Getter
	public enum Location
	{
		DB2("jdbc:db2:",BASEPATH + "db2"),
		H2("jdbc:h2:",BASEPATH + "h2"),
		HSQLDB("jdbc:hsqldb:",BASEPATH + "hsqldb"),
		MARIADB("jdbc:mariadb:",BASEPATH + "mysql"),
		MSSQL("jdbc:sqlserver:",BASEPATH + "mssql"),
		MYSQL("jdbc:mysql:",BASEPATH + "mysql"),
		ORACLE("jdbc:oracle:",BASEPATH + "oracle"),
		POSTGRES("jdbc:postgresql:",BASEPATH + "postgresql");
		
		String jdbcUrl;
		String location;
		
		public static Optional<String> getLocation(String jdbcUrl)
		{
			return Arrays.stream(values())
					.filter(l -> jdbcUrl.startsWith(l.jdbcUrl))
					.map(l -> l.location)
					.findFirst();
		}
	}

	@Value("${fc.jdbc.driverClassName}")
	String driverClassName;
	@Value("${fc.jdbc.url}")
	String jdbcUrl;
	@Value("${fc.jdbc.username}")
	String username;
	@Value("${fc.jdbc.password}")
	String password;
	@Value("${fc.pool.autoCommit}")
	boolean isAutoCommit;
	@Value("${fc.pool.connectionTimeout}")
	int connectionTimeout;
	@Value("${fc.pool.maxIdleTime}")
	int maxIdleTime;
	@Value("${fc.pool.maxLifetime}")
	int maxLifetime;
	@Value("${fc.pool.testQuery}")
	String testQuery;
	@Value("${fc.pool.minPoolSize}")
	int minPoolSize;
	@Value("${fc.pool.maxPoolSize}")
	int maxPoolSize;

	@Bean
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

	@Bean
	public void flyway()
	{
		val locations = Location.getLocation(jdbcUrl);
		locations.ifPresent(l ->
		{
			val config = Flyway.configure()
					.dataSource(jdbcUrl,username,password)
					.locations(l)
					.ignoreMissingMigrations(true);
			config.load().migrate();
		});
	}
}
