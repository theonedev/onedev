package com.turbodev.server.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

@Singleton
public class SessionProvider implements Provider<Session> {

	private final UnitOfWork unitOfWork;
	
	@Inject
	public SessionProvider(UnitOfWork unitOfWork) {
		this.unitOfWork = unitOfWork;
	}
	
	@Override
	public Session get() {
		return unitOfWork.getSession();
	}

}
