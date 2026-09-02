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

import io.vavr.collection.Stream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
public class VirtualPathTest
{
	@Test
	void testValue()
	{
		val path = new VirtualPath("/ABC123");
		assertThat(path.getValue()).isEqualTo("/ABC123");
	}

	@ParameterizedTest
	@ValueSource(strings = { "/a", "/A", "/0", "/ABCdef123" })
	void testValidPaths(String path)
	{
		assertDoesNotThrow(() -> new VirtualPath(path));
	}

	@ParameterizedTest
	@MethodSource
	void testInvalidPaths(String path)
	{
		assertThrows(IllegalArgumentException.class, () -> new VirtualPath(path));
	}

	private static Stream<String> testInvalidPaths()
	{
		// too short, no leading slash, invalid characters, too long
		val max = "/".concat(IntStream.rangeClosed(1, 256).mapToObj(i -> "a").collect(Collectors.joining()));
		return Stream.of("a", "ABC", "/a b", "/a/b", "/a-", max);
	}

	@Test
	void testEquality()
	{
		assertThat(new VirtualPath("/ABC")).isEqualTo(new VirtualPath("/ABC"));
		assertThat(new VirtualPath("/ABC")).isNotEqualTo(new VirtualPath("/ABD"));
	}
}
