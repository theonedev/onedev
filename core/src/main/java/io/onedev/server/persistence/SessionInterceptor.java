package io.onedev.server.persistence;

import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;

import io.onedev.commons.utils.ExceptionUtils;

public class SessionInterceptor implements MethodInterceptor {

	@Inject
	private SessionManager sessionManager;

	public Object invoke(MethodInvocation mi) throws Throwable {
		return sessionManager.call(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				try {
					return mi.proceed();
				} catch (Throwable e) {
					throw ExceptionUtils.unchecked(e);
				}
			}
			
		});
	}
	
}