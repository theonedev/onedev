package io.onedev.server.persistence;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.google.inject.Inject;

import io.onedev.utils.ExceptionUtils;

public class TransactionInterceptor implements MethodInterceptor {

	@Inject
	private PersistManager persistManager;
	
	@Inject
	private UnitOfWork unitOfWork;
	
	public Object invoke(MethodInvocation mi) throws Throwable {
		if (persistManager.getSessionFactory() != null) {
			unitOfWork.begin();
			try {
				Session session = unitOfWork.getSession();
				if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE) {
					return mi.proceed();
				} else {
					Transaction tx = session.beginTransaction();
					try {
						Object result = mi.proceed();
						session.flush();
						tx.commit();
						return result;
					} catch (Throwable t) {
						tx.rollback();
						throw ExceptionUtils.unchecked(t);
					}
				}
			} finally {
				unitOfWork.end();
			}
		} else {
			return mi.proceed();
		}
	}

}
