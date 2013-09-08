package com.pmease.commons.hibernate;

import java.util.Stack;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class UnitOfWorkImpl implements UnitOfWork, Provider<Session>, SessionProvider {
	
	private final Provider<SessionFactory> sessionFactoryProvider;
	
	private final ThreadLocal<Stack<Session>> sessionStack = new ThreadLocal<Stack<Session>>() {

		@Override
		protected Stack<Session> initialValue() {
			return new Stack<Session>();
		}
		
	};
	
	@Inject
	public UnitOfWorkImpl(Provider<SessionFactory> sessionFactoryProvider) {
		this.sessionFactoryProvider = sessionFactoryProvider;
	}

	public void begin() {
		Session session;
		if (sessionStack.get().isEmpty())
			session = sessionFactoryProvider.get().openSession();
		else
			session = sessionStack.get().peek();
		sessionStack.get().push(session);
	}

	public void end() {
		Preconditions.checkState(!sessionStack.get().isEmpty(), 
				"Not balanced calls to UnitOfWork.begin() and UnitOfWork.end()");
		Session session = sessionStack.get().pop();
		if (sessionStack.get().isEmpty())
			session.close();
	}

	public Session get() {
		return getSession();
	}

	public Session getSession() {
		Preconditions.checkState(!sessionStack.get().isEmpty(), 
				"Make sure to begin a work first by calling UnitOfWork.begin().");
		return sessionStack.get().peek();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactoryProvider.get();
	}

}