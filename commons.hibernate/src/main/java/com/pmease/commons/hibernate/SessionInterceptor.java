/*
 * Copyright PMEase (c) 2005-2008,
 * Date: Feb 24, 2008
 * Time: 4:29:05 PM
 * All rights reserved.
 * 
 * Revision: $Id: TransactionInterceptor.java 1209 2008-07-28 00:16:18Z robin $
 */
package com.pmease.commons.hibernate;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;

class SessionInterceptor implements MethodInterceptor {

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