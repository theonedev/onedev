package io.onedev.server.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.apache.shiro.web.subject.WebSubjectContext;
import org.apache.shiro.web.subject.support.WebDelegatingSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Collection;
import java.util.Set;

@Singleton
public class DefaultWebSecurityManager extends org.apache.shiro.web.mgt.DefaultWebSecurityManager {

	@Inject
	public DefaultWebSecurityManager(Set<Realm> realms, RememberMeManager rememberMeManager) {
		setSubjectFactory(new DefaultWebSubjectFactory() {

			@Override
			public Subject createSubject(SubjectContext context) {
				var securityManager = context.resolveSecurityManager();
				var session = context.resolveSession();
				var sessionEnabled = context.isSessionCreationEnabled();
				var principals = context.resolvePrincipals();
				if (principals == null)
					principals = SecurityUtils.PRINCIPALS_ANONYMOUS;
				var authenticated = context.resolveAuthenticated();
				var host = context.resolveHost();
		        if (context instanceof WebSubjectContext) {
					WebSubjectContext wsc = (WebSubjectContext) context;
					ServletRequest request = wsc.resolveServletRequest();
					ServletResponse response = wsc.resolveServletResponse();

					return new WebDelegatingSubject(principals, authenticated, host, session, sessionEnabled,
							request, response, securityManager) {

						@Override
						public void logout() {
							super.logout();
							principals = SecurityUtils.PRINCIPALS_ANONYMOUS;
						}

					};
				} else {
					return new DelegatingSubject(principals, authenticated, host, session, sessionEnabled, securityManager);
				}
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
	 * This method is overriden to make sure that anonymous principal is not saved 
	 * to session. This is important as otherwise requests to RESTful services will trigger creation 
	 * of sessions.   
	 * 
	 * @see org.apache.shiro.mgt.DefaultSecurityManager#save(org.apache.shiro.subject.Subject)
	 */
	@Override
	protected void save(Subject subject) {
		if (!SecurityUtils.isAnonymous((String) subject.getPrincipal()))
			super.save(subject);
	}

}
