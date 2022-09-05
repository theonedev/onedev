package io.onedev.server.persistence;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.util.ObjectReference;

@Singleton
public class DefaultSessionManager implements SessionManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);
	
	private final Provider<PersistManager> persistManagerProvider;
	
	private final ExecutorService executorService;
	
	private final TransactionManager transactionManager;
	
	private final ThreadLocal<ObjectReference<Session>> sessionReferenceHolder = new ThreadLocal<ObjectReference<Session>>() {

		@Override
		protected ObjectReference<Session> initialValue() {
			return new ObjectReference<Session>() {

				@Override
				protected Session openObject() {
					Session session = persistManagerProvider.get().getSessionFactory().openSession();
					// Session is supposed to be able to write only in transactional methods
					session.setHibernateFlushMode(FlushMode.MANUAL);
					
					return session;
				}

				@Override
				protected void closeObject(Session session) {
					session.close();
				}
				
			};
		}
		
	};
	
	@Inject
	public DefaultSessionManager(Provider<PersistManager> persistManagerProvider, ExecutorService executorService, 
			TransactionManager transactionManager) {
		this.persistManagerProvider = persistManagerProvider;
		this.executorService = executorService;
		this.transactionManager = transactionManager;
	}

	@Override
	public void openSession() {
		sessionReferenceHolder.get().open();
	}

	@Override
	public void closeSession() {
		sessionReferenceHolder.get().close();
	}

	@Override
	public Session getSession() {
		return sessionReferenceHolder.get().get();
	}
	
	@Override
	public <T> T call(Callable<T> callable) {
		if (persistManagerProvider.get().getSessionFactory() != null) {
			openSession();
			try {
				return callable.call();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			} finally {
				closeSession();
			}
		} else {
			try {
				return callable.call();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
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
							DefaultSessionManager.this.run(runnable);
						} finally {
							lock.unlock();
						}
					} else {
						DefaultSessionManager.this.run(runnable);
					}
				} catch (Exception e) {
					logger.error("Error executing in session", e);
				}
			}
			
		});
	}

	@Override
	public void runAsync(Runnable runnable) {
		runAsync(runnable, null);
	}

	@Override
	public void runAsyncAfterCommit(Runnable runnable, Lock lock) {
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				runAsync(runnable, lock);
			}
			
		});
	}

	@Override
	public void runAsyncAfterCommit(Runnable runnable) {
		runAsyncAfterCommit(runnable, null);
	}

}