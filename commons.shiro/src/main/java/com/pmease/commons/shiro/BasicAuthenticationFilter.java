package com.pmease.commons.shiro;

import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.WebUtils;

import com.pmease.commons.loader.AppName;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.util.StringUtils;

@Singleton
public class BasicAuthenticationFilter extends PathMatchingFilter {
	
    /**
     * HTTP Authorization header, equal to <code>Authorization</code>
     */
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * HTTP Authentication header, equal to <code>WWW-Authenticate</code>
     */
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

    private final String appName;
	
	@Inject
	public BasicAuthenticationFilter(@AppName String appName) {
		this.appName = appName;
	}

    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
	        HttpServletRequest httpRequest = WebUtils.toHttp(request);
	        String authzHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
	        if (authzHeader != null) {
	            String authzScheme = HttpServletRequest.BASIC_AUTH.toLowerCase(Locale.ENGLISH);
	            
	            if (authzHeader.toLowerCase(Locale.ENGLISH).startsWith(authzScheme)) {
	            	String authToken = StringUtils.substringAfter(authzHeader, " ");
	                String decoded = Base64.decodeToString(authToken);
	                String userName = StringUtils.substringBefore(decoded, ":").trim();
	                String password = StringUtils.substringAfter(decoded, ":").trim();
	                if (userName.length() != 0 && password.length() != 0) {
		                UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
	                    subject.login(token);
	                }
	            }
	        } 
		} 
		
		return true;
	}

	@Override
	protected void cleanup(ServletRequest request, ServletResponse response, Exception existing) 
			throws ServletException, IOException {

        HttpServletResponse httpResponse = WebUtils.toHttp(response);
		if (existing != null) {
			if (ExceptionUtils.find(existing, UnauthenticatedException.class) != null) {
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		        String authcHeader = HttpServletRequest.BASIC_AUTH + " realm=\"" + appName + "\"";
		        httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
				existing = null;
			} else if (ExceptionUtils.find(existing, UnauthorizedException.class) != null) {
				if (!SecurityUtils.getSubject().isAuthenticated()) {
					httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			        String authcHeader = HttpServletRequest.BASIC_AUTH + " realm=\"" + appName + "\"";
			        httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
					existing = null;
				} else {
					httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
					existing = null;
				}
			} else if (ExceptionUtils.find(existing, IncorrectCredentialsException.class) != null) {
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Incorrect credentials.");
				existing = null;
			} else if (ExceptionUtils.find(existing, UnknownAccountException.class) != null) {
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown user name.");
				existing = null;
			}
		}
		
		super.cleanup(request, response, existing);
	}

}
