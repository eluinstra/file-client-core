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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
public class RetriesTest
{
	@Test
	void testDefault()
	{
		assertThat(new Retries().getValue()).isZero();
	}

	@Test
	void testValue()
	{
		assertThat(new Retries(3).getValue()).isEqualTo(3);
	}

	@Test
	void testIncrement()
	{
		val retries = new Retries(2);
		assertThat(retries.getValue()).isEqualTo(2);
		assertThat(retries.increment().getValue()).isEqualTo(3);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 100 })
	void testValid(int value)
	{
		assertDoesNotThrow(() -> new Retries(value));
	}

	@ParameterizedTest
	@ValueSource(ints = { -1, -100 })
	void testNegativeRejected(int value)
	{
		assertThrows(IllegalArgumentException.class, () -> new Retries(value));
	}

	@Test
	void testEquality()
	{
		assertThat(new Retries(4)).isEqualTo(new Retries(4));
		assertThat(new Retries(4)).isNotEqualTo(new Retries(5));
	}
}
