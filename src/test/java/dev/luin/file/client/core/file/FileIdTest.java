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
package dev.luin.file.client.core.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.vavr.collection.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
public class FileIdTest
{
	@Test
	void testFromString()
	{
		val id = new FileId("42");
		assertThat(id.getValue()).isEqualTo(42L);
		assertThat(id.getStringValue()).isEqualTo("42");
	}

	@Test
	void testFromLong()
	{
		val id = new FileId(7L);
		assertThat(id.getValue()).isEqualTo(7L);
		assertThat(id.getStringValue()).isEqualTo("7");
	}

	@ParameterizedTest
	@ValueSource(longs = { 0L, 1L, 1000000000000000000L, 9223372036854775807L })
	void testValidIds(long value)
	{
		assertDoesNotThrow(() -> new FileId(value));
	}

	@ParameterizedTest
	@ValueSource(longs = { -1L, -1000000000000000000L, -9223372036854775808L })
	void testNegativeIdsRejected(long value)
	{
		assertThrows(IllegalArgumentException.class, () -> new FileId(value));
	}

	@ParameterizedTest
	@MethodSource
	void testInvalidStringsRejected(String input)
	{
		assertThrows(NumberFormatException.class, () -> new FileId(input));
	}

	private static Stream<Arguments> testInvalidStringsRejected()
	{
		return Stream.of(arguments("42x"), arguments(""), arguments(" "), arguments("1.5"));
	}

	@Test
	void testEquality()
	{
		assertThat(new FileId(5L)).isEqualTo(new FileId(5L));
		assertThat(new FileId("5")).isEqualTo(new FileId(5L));
		assertThat(new FileId(5L)).isNotEqualTo(new FileId(6L));
		assertThat(new FileId(5L).hashCode()).isEqualTo(new FileId(5L).hashCode());
	}
}
