package io.onedev.server.security;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.AgentTokenService;
import io.onedev.server.service.UserService;
import io.onedev.server.workspace.WorkspaceService;
import io.onedev.server.job.JobService;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class BearerAuthenticationFilter extends ExceptionHandleFilter {
	
	@Inject
	private UserService userService;
	
	@Inject
	private AccessTokenService accessTokenService;
	
	@Inject
	private AgentTokenService agentTokenService;
	
	@Inject
	private JobService jobService;

	@Inject
	private WorkspaceService workspaceService;
	
	@Inject
	private ClusterService clusterService;
	
	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
			String bearerToken = SecurityUtils.getBearerToken((HttpServletRequest)request);
			if (bearerToken != null) {
				if (clusterService.getCredential().equals(bearerToken)) {
					ThreadContext.bind(userService.getSystem().asSubject());
				} else {
					var accessToken = accessTokenService.findByValue(bearerToken);
					if (accessToken != null) {
						ThreadContext.bind(accessToken.asSubject());
					} else {
						var workspaceContext = workspaceService.getWorkspaceContext(bearerToken, false);
						if (workspaceContext != null) {
							var workspace = workspaceService.load(workspaceContext.getWorkspaceId());
							ThreadContext.bind(workspace.getUser().asSubject());
						} else if (agentTokenService.find(bearerToken) == null 
								&& jobService.getJobContext(bearerToken, false) == null) {
							throw new IncorrectCredentialsException("Invalid or expired access token");
						}
					}
				}
	        } 
		}
		return true;
	}

}
