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
package dev.luin.file.client.core.download;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.CharSeq;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderValue
{
	@NonNull
	String value;
	@NonNull
	Map<String, String> params;

	public static Option<HeaderValue> of(String value)
	{
		val parts = value != null ? CharSeq.of(value).split(";") : List.<CharSeq>empty();
		return parts.headOption().map(v -> new HeaderValue(v.trim().mkString(), getParams(parts.tail())));
	}

	private static Map<String, String> getParams(Seq<CharSeq> stream)
	{
		return stream.flatMap(s -> toTuple2(s, "=")).foldLeft(HashMap.empty(), (m, t) -> m.put(t));
	}

	private static Option<Tuple2<String, String>> toTuple2(@NonNull CharSeq s, String splitRegEx)
	{
		val parts = s.split(splitRegEx, 2);
		return parts.headOption()
				.map(k -> Tuple.of(k.trim().mkString(), parts.tail().headOption().map(v -> v.trim().mkString().replaceAll("^\"|\"$", "")).getOrNull()));
	}

	@Override
	public String toString()
	{
		return value + params.toStream().map(e -> "; " + e._1 + " = " + Option.of(e._2).getOrElse("")).mkString();
	}

	public static void main(String[] args)
	{
		HeaderValue.of("test;a=\"b\";b=c;c").stdout();
	}
}
