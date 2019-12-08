/**
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onedev.server.persistence;

import java.util.concurrent.Callable;

import org.hibernate.Session;
import org.hibernate.Transaction;

public interface TransactionManager {

	Transaction getTransaction();
	
	Session getSession();
	
	<T> T call(Callable<T> callable);
	
	void run(Runnable runnable);
	
	void runAsync(Runnable runnable);
	
	/**
	 * Do some work after commit if there is an active transaction, or do the work immediately if there is no active 
	 * transaction. Note that the work should not involve any other database operations in current thread 
	 *  
	 * @param runnable
	 */
	void runAfterCommit(Runnable runnable);

	void mustRunAfterTransaction(Runnable runnable);
}
