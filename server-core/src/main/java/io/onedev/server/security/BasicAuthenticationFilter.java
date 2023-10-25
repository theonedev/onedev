package io.onedev.server.security;

import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

@Singleton
public class BasicAuthenticationFilter extends ExceptionHandleFilter {
	
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
	        String authzHeader = httpRequest.getHeader(KubernetesHelper.AUTHORIZATION);
			if (authzHeader == null)
				authzHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
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

}
