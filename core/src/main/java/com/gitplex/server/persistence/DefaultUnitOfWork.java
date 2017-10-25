package com.gitplex.server.persistence;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.utils.ObjectReference;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultUnitOfWork implements UnitOfWork {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultUnitOfWork.class);
	
	private final PersistManager persistManager;
	
	private final ExecutorService executorService;
	
	private final ThreadLocal<ObjectReference<Session>> sessionReferenceHolder = new ThreadLocal<ObjectReference<Session>>() {

		@Override
		protected ObjectReference<Session> initialValue() {
			return new ObjectReference<Session>() {

				@Override
				protected Session openObject() {
					Session session = persistManager.getSessionFactory().openSession();
					try {
						// Session is supposed to be able to write only in transactional methods
						session.setHibernateFlushMode(FlushMode.MANUAL);
					} catch (Exception e) {
						session.close();
						throw Throwables.propagate(e);
					}
					
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
	public DefaultUnitOfWork(PersistManager persistManager, ExecutorService executorService) {
		this.persistManager = persistManager;
		this.executorService = executorService;
	}

	@Override
	public void begin() {
		sessionReferenceHolder.get().open();
	}

	@Override
	public void end() {
		sessionReferenceHolder.get().close();
	}

	@Override
	public Session getSession() {
		return sessionReferenceHolder.get().get();
	}
	
	@Override
	public <T> T call(Callable<T> callable) {
		if (persistManager.getSessionFactory() != null) {
			begin();
			try {
				return callable.call();
			} catch (Exception e) {
				throw Throwables.propagate(e);
			} finally {
				end();
			}
		} else {
			try {
				return callable.call();
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public void doAsync(final Runnable runnable) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				if (persistManager.getSessionFactory() != null) {
					begin();
					try {
						runnable.run();
					} catch (Exception e) {
						logger.error("Error executing within unit of work.", e);
					} finally {
						end();
					}
				} else {
					try {
						runnable.run();
					} catch (Exception e) {
						logger.error("Error executing within unit of work.", e);
					}
				}
			}
			
		});
	}

	@Override
	public void run(Runnable runnable) {
		if (persistManager.getSessionFactory() != null) {
			begin();
			try {
				runnable.run();
			} finally {
				end();
			}
		} else {
			runnable.run();
		}
	}

}