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

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import java.util.function.Function;

import javax.xml.ws.WebFault;

import org.springframework.dao.DataAccessException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebFault(targetNamespace="http://luin.dev/file/client/1.0")
@NoArgsConstructor
public class ServiceException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	public static Function<? super Throwable,ServiceException> defaultExceptionProvider = e -> 
	{
		log.error("",e);
		return Match(e).of(
				Case($(instanceOf(ServiceException.class)),o -> {
					return o;
				}),
				Case($(instanceOf(DataAccessException.class)),o -> {
					return new ServiceException("A unexpected error occurred!");
				}),
				Case($(),o -> {
					return new ServiceException(o);
				}));
	};

	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message,cause,enableSuppression,writableStackTrace);
	}

	public ServiceException(String message, Throwable cause)
	{
		super(message,cause);
	}

	public ServiceException(String message)
	{
		super(message);
	}

	public ServiceException(Throwable cause)
	{
		super(cause);
	}

}
