package io.onedev.server.persistence;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.util.ObjectReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultSessionService implements SessionService {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultSessionService.class);

	@Inject
	private ExecutorService executorService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private SessionFactoryService sessionFactoryService;
	
	private final ThreadLocal<ObjectReference<Session>> sessionReferenceHolder = new ThreadLocal<ObjectReference<Session>>() {

		@Override
		protected ObjectReference<Session> initialValue() {
			return new ObjectReference<Session>() {

				@Override
				protected Session openObject() {
					SessionFactory sessionFactory = sessionFactoryService.getSessionFactory();
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
		if (sessionFactoryService.getSessionFactory() == null)
			throw new ServerNotReadyException();
		else
			return sessionReferenceHolder.get().get();
	}
	
	@Override
	public <T> T call(Callable<T> callable) {
		if (sessionFactoryService.getSessionFactory() != null) {
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
				DefaultSessionService.this.run(runnable);
			} catch (Exception e) {
				logger.error("Error executing in session", e);
			}
		});
	}

	@Override
	public void runAsyncAfterCommit(Runnable runnable) {
		transactionService.runAfterCommit(() -> runAsync(runnable));
	}

}