package io.onedev.server.persistence;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;

public class SessionInterceptor implements MethodInterceptor {

	@Inject
	private PersistManager persistManager;
	
	@Inject
	private UnitOfWork unitOfWork;

	public Object invoke(MethodInvocation mi) throws Throwable {
		if (persistManager.getSessionFactory() != null) {
			unitOfWork.begin();
			try {
				return mi.proceed();
			} finally {
				unitOfWork.end();
			}
		} else {
			return mi.proceed();
		}
	}
	
}