package io.onedev.server.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

import com.google.common.net.HttpHeaders;

import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.ExceptionUtils;

@Singleton
public class BearerAuthenticationFilter extends PathMatchingFilter {
	
	private final UserManager userManager;
	
	@Inject
	public BearerAuthenticationFilter(UserManager userManager) {
		this.userManager = userManager;
	}
	
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
	        HttpServletRequest httpRequest = WebUtils.toHttp(request);
	        String authzHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
	        if (authzHeader != null && authzHeader.startsWith(KubernetesHelper.BEARER + " ")) {
            	String tokenValue = StringUtils.substringAfter(authzHeader, " ");
            	User user = userManager.findByAccessToken(tokenValue);
            	if (user != null)
            		subject.login(new BearerAuthenticationToken(user));
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
