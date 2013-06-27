package com.pmease.commons.security;

import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

@Singleton
public class DefaultRememberMeManager extends CookieRememberMeManager {

	/**
	 * This method is overriden to provide a default principal for not remembered and unauthenticated 
	 * users in order to make various subject role/permission check methods works in that case.  
	 */
	@Override
	public PrincipalCollection getRememberedPrincipals(SubjectContext subjectContext) {
		PrincipalCollection principals = super.getRememberedPrincipals(subjectContext);
		if (principals == null)
			principals = new SimplePrincipalCollection(0L, "");
		return principals;
	}

}
