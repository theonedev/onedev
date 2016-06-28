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
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.google.inject.Inject;

public class TransactionInterceptor implements MethodInterceptor {

	private static ThreadLocal<Integer> transactionRefCounter = new ThreadLocal<Integer>() {

		@Override
		protected Integer initialValue() {
			return 0;
		}
		
	};
	
	@Inject
	private UnitOfWork unitOfWork;
	
	private void increaseTransactionRefCounter() {
		transactionRefCounter.set(transactionRefCounter.get()+1);
	}
	
	private void decreaseTransactionRefCounter() {
		transactionRefCounter.set(transactionRefCounter.get()-1);
	}
	
	public Object invoke(MethodInvocation mi) throws Throwable {
		unitOfWork.begin();
		try {
			Session session = unitOfWork.getSession();
			if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE) {
				increaseTransactionRefCounter();
				try {
					return mi.proceed();
				} finally {
					decreaseTransactionRefCounter();
				}
			} else {
				Transaction tx = session.beginTransaction();
				FlushMode previousMode = session.getFlushMode();
				session.setFlushMode(FlushMode.COMMIT);
				increaseTransactionRefCounter();
				try {
					Object result = mi.proceed();
					tx.commit();
					return result;
				} catch (Throwable t) {
					try {
						tx.rollback();
					} catch (Throwable t2) {
					}
					throw t;
				} finally {
					decreaseTransactionRefCounter();
					session.setFlushMode(previousMode);
				}
			}
			
		} finally {
			unitOfWork.end();
		}
	}

	/**
	 * Checks if current transactional method invocation is the initiating invocation. If a transactional 
	 * method is called from another transactional method, its invocation is not considered to be 
	 * initiating. This is important as we often only want to invoke action listeners on initiating 
	 * invocations when implementation of one action calls other actions
	 */
	public static boolean isInitiating() {
		return transactionRefCounter.get() == 1;
	}
	
}
