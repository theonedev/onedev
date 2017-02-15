package com.gitplex.server.persistence;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;

public class SessionInterceptor implements MethodInterceptor {

	@Inject
	private UnitOfWork unitOfWork;

	public Object invoke(MethodInvocation mi) throws Throwable {
		unitOfWork.begin();
		try {
			return mi.proceed();
		} finally {
			unitOfWork.end();
		}
	}
	
}