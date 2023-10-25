package io.onedev.server.security;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class BearerAuthenticationFilter extends ExceptionHandleFilter {
	
	private final UserManager userManager;
	
	private final ClusterManager clusterManager;
	
	@Inject
	public BearerAuthenticationFilter(UserManager userManager, ClusterManager clusterManager) {
		this.userManager = userManager;
		this.clusterManager = clusterManager;
	}

	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
			String bearerToken = SecurityUtils.getBearerToken((HttpServletRequest)request);
			if (bearerToken != null) {
				if (clusterManager.getCredential().equals(bearerToken)) { 
					subject.login(new BearerAuthenticationToken(userManager.getSystem()));
				} else {
	            	User user = userManager.findByAccessToken(bearerToken);
	            	if (user != null)
	            		subject.login(new BearerAuthenticationToken(user));
				}
	        } 
		} 
		
		return true;
	}

}
