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
package dev.luin.file.client.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class PredicatesTest
{
	@Test
	void testContains()
	{
		Predicate<String> p = Predicates.contains("foo", "bar");
		assertThat(p.test("xfoobar")).isTrue();
		assertThat(p.test("baz foo")).isTrue();
		assertThat(p.test("baz")).isFalse();
	}

	@Test
	void testContainsNoValues()
	{
		assertThat(Predicates.contains().test("anything")).isFalse();
	}

	@Test
	void testStartsWith()
	{
		assertThat(Predicates.startsWith("he").test("hello")).isTrue();
		assertThat(Predicates.startsWith("lo").test("hello")).isFalse();
	}

	@Test
	void testEndsWith()
	{
		assertThat(Predicates.endsWith("lo").test("hello")).isTrue();
		assertThat(Predicates.endsWith("he").test("hello")).isFalse();
	}
}
