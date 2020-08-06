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
package dev.luin.fc.core.file;

import java.util.List;

import io.vavr.control.Option;
import lombok.NonNull;

interface FSFileDAO
{
	Option<FSFile> findFile(long id);
	List<String> selectFiles();
	long insertFile(@NonNull FSFile fsFile);
	long updateFile(@NonNull FSFile fsFile);
	long deleteFile(long id);
}
