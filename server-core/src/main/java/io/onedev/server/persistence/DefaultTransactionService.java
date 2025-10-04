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

import com.google.common.base.Preconditions;
import io.onedev.commons.utils.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.FlushModeType;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Singleton
public class DefaultTransactionService implements TransactionService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTransactionService.class);

	@Inject
	private SessionService sessionService;

	@Inject
	private ExecutorService executorService;
	
	private final Map<Transaction, Collection<Runnable>> completionRunnables = new ConcurrentHashMap<>();

	@Override
	public <T> T call(Callable<T> callable) {
		return sessionService.call(() -> {
			if (getTransaction().isActive()) {
				return callable.call();
			} else {
				Session session = sessionService.getSession();
				FlushModeType prevFlushModeType = session.getFlushMode();
				Transaction tx = session.beginTransaction();
				try {
					session.setFlushMode(FlushModeType.AUTO);
					T result = callable.call();
					tx.commit();
					return result;
				} catch (Throwable t) {
					tx.rollback();
					throw ExceptionUtils.unchecked(t);
				} finally {
					Collection<Runnable> runnables = completionRunnables.remove(tx);
					if (runnables != null) {
						for (Runnable runnable: runnables) {
							try {
								runnable.run();
							} catch (Exception e) {
								logger.error("Error running completion callback", e);
							}
						}
					}
					session.setFlushMode(prevFlushModeType);
				}
			}
		});
	}

	@Override
	public void run(Runnable runnable) {
		call(() -> {
			runnable.run();
			return null;
		});
	}

	@Override
	public void runAsync(Runnable runnable) {
		executorService.execute(() -> {
			try {
				DefaultTransactionService.this.run(runnable);
			} catch (Exception e) {
				logger.error("Error executing in transaction", e);
			}
		});
	}
	
	@Override
	public void runAfterCommit(Runnable runnable) {
		if (getTransaction() != null && getTransaction().isActive()) {
			getTransaction().registerSynchronization(new Synchronization() {
				
				@Override
				public void beforeCompletion() {
				}
				
				@Override
				public void afterCompletion(int status) {
					if (status == Status.STATUS_COMMITTED) {
						try {
							runnable.run();
						} catch (Exception e) {
							logger.error("Error executing after commit", e);
						}
					}
				}
				
			});
		} else {
			runnable.run();
		}
	}
	
	@Override
	public Transaction getTransaction() {
		return getSession() != null? getSession().getTransaction(): null;
	}

	@Override
	public Session getSession() {
		return sessionService.getSession();
	}

	@Override
	public void mustRunAfterTransaction(Runnable runnable) {
		Transaction transaction = getTransaction();
		Preconditions.checkState(transaction.isActive());
		Collection<Runnable> runnables = completionRunnables.get(transaction);
		if (runnables == null) {
			runnables = new ArrayList<>();
			completionRunnables.put(transaction, runnables);
		}
		runnables.add(runnable);
	}

}
