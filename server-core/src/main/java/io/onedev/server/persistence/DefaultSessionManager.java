package io.onedev.server.persistence;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.util.ObjectReference;

@Singleton
public class DefaultSessionManager implements SessionManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);
	
	private final ExecutorService executorService;
	
	private final TransactionManager transactionManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final ThreadLocal<ObjectReference<Session>> sessionReferenceHolder = new ThreadLocal<ObjectReference<Session>>() {

		@Override
		protected ObjectReference<Session> initialValue() {
			return new ObjectReference<Session>() {

				@Override
				protected Session openObject() {
					SessionFactory sessionFactory = sessionFactoryManager.getSessionFactory();
					if (sessionFactory != null) {
						Session session = sessionFactory.openSession();
						// Session is supposed to be able to write only in transactional methods
						session.setHibernateFlushMode(FlushMode.MANUAL);
						return session;
					} else {
						throw new ServerNotReadyException();
					}
				}

				@Override
				protected void closeObject(Session session) {
					session.close();
				}
				
			};
		}
		
	};
	
	@Inject
	public DefaultSessionManager(ExecutorService executorService, TransactionManager transactionManager, 
			SessionFactoryManager sessionFactoryManager) {
		this.executorService = executorService;
		this.transactionManager = transactionManager;
		this.sessionFactoryManager = sessionFactoryManager;
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
		if (sessionFactoryManager.getSessionFactory() == null)
			throw new ServerNotReadyException();
		else
			return sessionReferenceHolder.get().get();
	}
	
	@Override
	public <T> T call(Callable<T> callable) {
		if (sessionFactoryManager.getSessionFactory() != null) {
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
		call(() -> {
			runnable.run();
			return null;
		});
	}

	@Override
	public void runAsync(Runnable runnable) {
		executorService.execute(() -> {
			try {
				DefaultSessionManager.this.run(runnable);
			} catch (Exception e) {
				logger.error("Error executing in session", e);
			}
		});
	}

	@Override
	public void runAsyncAfterCommit(Runnable runnable) {
		transactionManager.runAfterCommit(() -> runAsync(runnable));
	}

}