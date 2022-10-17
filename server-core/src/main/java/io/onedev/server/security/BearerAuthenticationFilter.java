package io.onedev.server.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.ExceptionUtils;

@Singleton
public class BearerAuthenticationFilter extends PathMatchingFilter {
	
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
				if (clusterManager.getCredentialValue().equals(bearerToken)) { 
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

	@Override
	protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) 
			throws ServletException, IOException {

        HttpServletResponse httpResponse = WebUtils.toHttp(response);
		if (existing != null && !httpResponse.isCommitted()) { 
			ExceptionUtils.handle(httpResponse, existing);
			existing = null;
		}
		
		super.cleanup(request, response, existing);
	}

}
