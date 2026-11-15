package io.onedev.server.security;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.lang.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.service.AccessTokenService;

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
				if (!userName.isEmpty()) {
					var accessToken = accessTokenService.findByValue(userName);
					if (accessToken != null) {
						SecurityUtils.bindAccessToken(httpRequest, accessToken, userName);
						return true;
					}
				}
				if (!password.isEmpty()) {
					var accessToken = accessTokenService.findByValue(password);
					if (accessToken != null) {
						SecurityUtils.bindAccessToken(httpRequest, accessToken, password);
						return true;
					}
				}
				if (!userName.isEmpty() && !password.isEmpty()) {
					subject.login(new UsernamePasswordToken(userName, password));
					return true;
				}
	        }
		}
		return true;
	}

}
