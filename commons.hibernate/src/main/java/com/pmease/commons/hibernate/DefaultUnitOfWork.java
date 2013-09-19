package com.pmease.commons.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pmease.commons.util.ObjectReference;

@Singleton
public class DefaultUnitOfWork implements UnitOfWork, Provider<Session> {
	
	private final Provider<SessionFactory> sessionFactoryProvider;
	
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
	public DefaultUnitOfWork(Provider<SessionFactory> sessionFactoryProvider) {
		this.sessionFactoryProvider = sessionFactoryProvider;
	}

	public void begin() {
		sessionReferenceHolder.get().increase();
	}

	public void end() {
		sessionReferenceHolder.get().decrease();
	}

	public Session get() {
		return getSession();
	}

	public Session getSession() {
		return sessionReferenceHolder.get().getObject();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactoryProvider.get();
	}

}