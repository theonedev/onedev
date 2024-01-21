package io.onedev.server.plugin.pack.container;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.BearerAuthenticationToken;
import io.onedev.server.security.ExceptionHandleFilter;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.core.HttpHeaders;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Singleton
public class ContainerAuthenticationFilter extends ExceptionHandleFilter {
	
	static final String ATTR_BUILD_ID = "buildId";
	
	private final UserManager userManager;
	
	private final JobManager jobManager;
	
	@Inject
	public ContainerAuthenticationFilter(UserManager userManager, JobManager jobManager) {
		this.userManager = userManager;
		this.jobManager = jobManager;
	}
	
	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
		Subject subject = SecurityUtils.getSubject();
		var httpRequest = WebUtils.toHttp(request);
		String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null) {
			if (authHeader.toLowerCase().startsWith("basic ")) {
				String authValue = StringUtils.substringAfter(authHeader, " ");
				String decodedAuthValue = Base64.decodeToString(authValue);
				String userName = StringUtils.substringBefore(decodedAuthValue, ":").trim();
				String password = StringUtils.substringAfter(decodedAuthValue, ":").trim();
				if (userName.length() != 0 && password.length() != 0) {
					User user = userManager.findByAccessToken(password);
					AuthenticationToken token;
					if (user != null)
						token = new BearerAuthenticationToken(user);
					else
						token = new UsernamePasswordToken(userName, password);
					try {
						subject.login(token);
					} catch (UnknownAccountException | IncorrectCredentialsException e) {
						throw new ClientException(SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, 
								"Unknown user name or incorrect credentials");
					}
				}
			} else if (authHeader.toLowerCase().startsWith("bearer ")) {
				var authValue = substringAfter(authHeader, " ");
				var jobContext = jobManager.getJobContext(substringBefore(authValue, ":"), false);
				if (jobContext != null)
					request.setAttribute(ATTR_BUILD_ID, jobContext.getBuildId());
				var accessToken = substringAfter(authValue, ":");
				var user = userManager.findByAccessToken(accessToken);
				if (user != null)
					subject.login(new BearerAuthenticationToken(user));
			} else {
				throw new ClientException(SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, 
						"Unsupported authorization: " + substringBefore(authHeader, " "));
			}
		}
		
		return true;
	}

}
