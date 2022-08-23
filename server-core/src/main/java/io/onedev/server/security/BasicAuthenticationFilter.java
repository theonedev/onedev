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
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

import com.google.common.net.HttpHeaders;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.ExceptionUtils;

@Singleton
public class BasicAuthenticationFilter extends PathMatchingFilter {
	
	private final UserManager userManager;

	@Inject
	public BasicAuthenticationFilter(UserManager userManager) {
		this.userManager = userManager;
	}
	
	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
	        HttpServletRequest httpRequest = WebUtils.toHttp(request);
	        String authzHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
	        if (authzHeader != null && authzHeader.toLowerCase().startsWith("basic ")) {
            	String authValue = StringUtils.substringAfter(authzHeader, " ");
                String decoded = Base64.decodeToString(authValue);
                String userName = StringUtils.substringBefore(decoded, ":").trim();
                String password = StringUtils.substringAfter(decoded, ":").trim();
                if (userName.length() != 0 && password.length() != 0) {
                	User user = userManager.findByAccessToken(password);
                	AuthenticationToken token;
                	if (user != null)
                		token = new BearerAuthenticationToken(user);
                	else
                		token = new UsernamePasswordToken(userName, password);
                    subject.login(token);
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
