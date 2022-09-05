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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.persistence.FlushModeType;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import io.onedev.commons.utils.ExceptionUtils;

@Singleton
public class DefaultTransactionManager implements TransactionManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTransactionManager.class);
	
	private final SessionManager sessionManager;
	
	private final ExecutorService executorService;
	
	private final Map<Transaction, Collection<Runnable>> completionRunnables = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultTransactionManager(SessionManager sessionManager, ExecutorService executorService) {
		this.sessionManager = sessionManager;
		this.executorService = executorService;
	}
	
	@Override
	public <T> T call(Callable<T> callable) {
		return sessionManager.call(new Callable<T>() {

			@Override
			public T call() throws Exception {
				if (getTransaction().isActive()) {
					return callable.call();
				} else {
					Session session = sessionManager.getSession();
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
			}
			
		});
	}

	@Override
	public void run(Runnable runnable) {
		call(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				runnable.run();
				return null;
			}
			
		});
	}

	@Override
	public void runAsync(Runnable runnable, @Nullable Lock lock) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					if (lock != null) {
						try {
							lock.lockInterruptibly();
							DefaultTransactionManager.this.run(runnable);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						} finally {
							lock.unlock();
						}
					} else {
						DefaultTransactionManager.this.run(runnable);
					}
				} catch (Exception e) {
					logger.error("Error executing in transaction", e);
				}
			}
			
		});
	}
	
	@Override
	public void runAsyncAfterCommit(Runnable runnable, @Nullable Lock lock) {
		runAfterCommit(new Runnable() {

			@Override
			public void run() {
				runAsync(runnable, lock);
			}
			
		});
	}
	
	@Override
	public void runAfterCommit(Runnable runnable) {
		if (getTransaction().isActive()) {
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
		return getSession().getTransaction();
	}

	@Override
	public Session getSession() {
		return sessionManager.getSession();
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

	@Override
	public void runAsync(Runnable runnable) {
		runAsync(runnable, null);
	}

	@Override
	public void runAsyncAfterCommit(Runnable runnable) {
		runAsyncAfterCommit(runnable, null);
	}
	
}
