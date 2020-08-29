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
package dev.luin.fc.core.querydsl;

import static dev.luin.fc.core.Predicates.contains;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.sql.DB2Templates;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLServer2012Templates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AccessLevel;
import lombok.val;
import lombok.experimental.FieldDefaults;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryDSLConfig
{
	@Autowired
	DataSource dataSource;
	
	@Bean
	public SQLQueryFactory queryFactory()
	{
		val provider = new SpringConnectionProvider(dataSource);
		return new SQLQueryFactory(querydslConfiguration(),provider);
	}

	@Bean
	public com.querydsl.sql.Configuration querydslConfiguration()
	{
		val templates = getSQLTemplates();
		val result = new com.querydsl.sql.Configuration(templates);
		result.register(new InstantType(Types.TIMESTAMP));
		result.register(new UrlType(Types.VARCHAR));
		result.register(new UploadStatusType(Types.TINYINT));
		result.register(new DownloadStatusType(Types.TINYINT));
		result.setExceptionTranslator(new SpringExceptionTranslator());
		return result;
	}

	private SQLTemplates getSQLTemplates()
	{
		return createSQLTemplates(dataSource);
	}

	private SQLTemplates createSQLTemplates(DataSource dataSource)
	{
		val driverClassName = ((HikariDataSource)dataSource).getDriverClassName();
		return Match(driverClassName).of(
				Case($(contains("db2")),o -> DB2Templates.builder().build()),
				Case($(contains("h2")),o -> H2Templates.builder().build()),
				Case($(contains("hsqldb")),o -> HSQLDBTemplates.builder().build()),
				Case($(contains("mariadb","mysql")),o -> MySQLTemplates.builder().build()),
				Case($(contains("oracle")),o -> OracleTemplates.builder().build()),
				Case($(contains("postgresql")),o -> PostgreSQLTemplates.builder().build()),
				Case($(contains("sqlserver")),o -> SQLServer2012Templates.builder().build()),
				Case($(),o -> {
					throw new RuntimeException("Driver class name " + driverClassName + " not recognized!");
				}));
	}
}
