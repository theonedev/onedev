package com.pmease.commons.hibernate;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pmease.commons.util.ObjectReference;

@Singleton
public class DefaultUnitOfWork implements UnitOfWork, Provider<Session> {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultUnitOfWork.class);
	
	private final Provider<SessionFactory> sessionFactoryProvider;
	
	private final ExecutorService executorService;
	
	private final ThreadLocal<ObjectReference<Session>> sessionReferenceHolder = new ThreadLocal<ObjectReference<Session>>() {

		@Override
		protected ObjectReference<Session> initialValue() {
			return new ObjectReference<Session>() {

				@Override
				protected Session openObject() {
					Session session = sessionFactoryProvider.get().openSession();
					
					// Session is supposed to be able to write only in transactional methods
					session.setFlushMode(FlushMode.MANUAL);
					
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
	public DefaultUnitOfWork(Provider<SessionFactory> sessionFactoryProvider, ExecutorService executorService) {
		this.sessionFactoryProvider = sessionFactoryProvider;
		this.executorService = executorService;
	}

	public void begin() {
		sessionReferenceHolder.get().open();
	}

	public void end() {
		sessionReferenceHolder.get().close();
	}

	public Session get() {
		return getSession();
	}

	public Session getSession() {
		return sessionReferenceHolder.get().get();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactoryProvider.get();
	}

	@Override
	public <T> T call(Callable<T> callable) {
		begin();
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			end();
		}
	}

	@Override
	public void asyncCall(final Runnable runnable) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				begin();
				try {
					runnable.run();
				} catch (Exception e) {
					logger.error("Error executing within unit of work.", e);
				} finally {
					end();
				}
			}
			
		});
	}

}