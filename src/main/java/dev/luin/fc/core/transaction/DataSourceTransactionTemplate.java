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
package dev.luin.fc.core.transaction;

import org.springframework.transaction.annotation.Transactional;

import io.vavr.CheckedFunction0;
import io.vavr.CheckedRunnable;

public class DataSourceTransactionTemplate implements TransactionTemplate
{
	@Override
	@Transactional(transactionManager = "dataSourceTransactionManager")
	public void executeTransaction(CheckedRunnable runnable)
	{
		runnable.unchecked().run();
	}

	@Override
	@Transactional(transactionManager = "dataSourceTransactionManager")
	public <T> T executeTransactionWithResult(CheckedFunction0<T> producer)
	{
		return (T)producer.unchecked().get();
	}
}
