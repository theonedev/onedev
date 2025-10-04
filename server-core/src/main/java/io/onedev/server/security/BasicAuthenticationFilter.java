package io.onedev.server.security;

import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.util.WebUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

@Singleton
public class BasicAuthenticationFilter extends ExceptionHandleFilter {
	
	private final AccessTokenService accessTokenService;
	
	@Inject
	public BasicAuthenticationFilter(AccessTokenService accessTokenService) {
		this.accessTokenService = accessTokenService;
	}
	
	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);

    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
	        String authzHeader = httpRequest.getHeader(KubernetesHelper.AUTHORIZATION);
			if (authzHeader == null)
				authzHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
	        if (authzHeader != null && authzHeader.toLowerCase().startsWith("basic ")) {
            	String authValue = StringUtils.substringAfter(authzHeader, " ");
                String decoded = Base64.decodeToString(authValue);
                String userName = StringUtils.substringBefore(decoded, ":").trim();
                String password = StringUtils.substringAfter(decoded, ":").trim();
				if (userName.length() != 0) {
					var accessToken = accessTokenService.findByValue(userName);
					if (accessToken != null) {
						ThreadContext.bind(accessToken.asSubject());
						return true;
					}
				}
				if (password.length() != 0) {
					var accessToken = accessTokenService.findByValue(password);
					if (accessToken != null) {
						ThreadContext.bind(accessToken.asSubject());
						return true;
					}
				}
				if (userName.length() != 0 && password.length() != 0) {
					subject.login(new UsernamePasswordToken(userName, password));
					return true;
				}
	        }
		}
		return true;
	}

}
