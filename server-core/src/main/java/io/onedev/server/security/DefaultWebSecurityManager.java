package io.onedev.server.security;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.apache.shiro.web.subject.WebSubjectContext;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;

@Singleton
public class DefaultWebSecurityManager extends org.apache.shiro.web.mgt.DefaultWebSecurityManager {

	@Inject
	public DefaultWebSecurityManager(Set<Realm> realms, RememberMeManager rememberMeManager) {
		setSubjectFactory(new DefaultWebSubjectFactory() {

			@Override
			public Subject createSubject(SubjectContext context) {
		        if (!(context instanceof WebSubjectContext)) {
		            return super.createSubject(context);
		        }
		        WebSubjectContext wsc = (WebSubjectContext) context;
		        SecurityManager securityManager = wsc.resolveSecurityManager();
		        Session session = wsc.resolveSession();
		        boolean sessionEnabled = wsc.isSessionCreationEnabled();
		        PrincipalCollection principals = wsc.resolvePrincipals();
		        
		    	/*
		    	 * Provide a default principal for not remembered and unauthenticated users in order to make 
		    	 * various subject role/permission check methods works in that case.  
		    	 */
		        if (principals == null)
		        	principals = new SimplePrincipalCollection(0L, "");
		        
		        boolean authenticated = wsc.resolveAuthenticated();
		        String host = wsc.resolveHost();
		        ServletRequest request = wsc.resolveServletRequest();
		        ServletResponse response = wsc.resolveServletResponse();

		        return new WebDelegatingSubject(principals, authenticated, host, session, sessionEnabled,
		                request, response, securityManager) {

							@Override
							public void logout() {
								super.logout();

								/*
						    	 * Provide a default principal for not remembered and unauthenticated users in order to make 
						    	 * various subject role/permission check methods works in that case.  
						    	 */
								principals = new SimplePrincipalCollection(0L, "");
							}
		        	
		        };
			}
			
		});
		
		setAuthenticator(new ModularRealmAuthenticator() {

			@Override
			protected AuthenticationInfo doMultiRealmAuthentication(Collection<Realm> realms, AuthenticationToken token) {
		        for (Realm realm : realms) {
		            if (realm.supports(token)) 
		                return realm.getAuthenticationInfo(token);
		        }
		        return null;
			}
			
		});
		
		setRealms(realms);
		setRememberMeManager(rememberMeManager);
	}

	/**
	 * This method is overriden to make sure that anonymous user (the user with id 0) is not saved 
	 * to session. This is important as otherwise requests to RESTful services will trigger creation 
	 * of sessions.   
	 * 
	 * @see org.apache.shiro.mgt.DefaultSecurityManager#save(org.apache.shiro.subject.Subject)
	 */
	@Override
	protected void save(Subject subject) {
		Long guestId = 0L;
		if (!guestId.equals(subject.getPrincipal()))
			super.save(subject);
	}

}
