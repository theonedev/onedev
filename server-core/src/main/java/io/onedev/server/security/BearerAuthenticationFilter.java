package io.onedev.server.security;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.job.JobManager;
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
	
	private final UserManager userManager;
	
	private final AccessTokenManager accessTokenManager;
	
	private final AgentTokenManager agentTokenManager;
	
	private final JobManager jobManager;
	
	private final ClusterManager clusterManager;
	
	@Inject
	public BearerAuthenticationFilter(AccessTokenManager accessTokenManager, AgentTokenManager agentTokenManager, 
									  JobManager jobManager, UserManager userManager, ClusterManager clusterManager) {
		this.accessTokenManager = accessTokenManager;
		this.agentTokenManager = agentTokenManager;
		this.jobManager = jobManager;
		this.userManager = userManager;
		this.clusterManager = clusterManager;
	}

	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
    	Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
			String bearerToken = SecurityUtils.getBearerToken((HttpServletRequest)request);
			if (bearerToken != null) {
				if (clusterManager.getCredential().equals(bearerToken)) {
					ThreadContext.bind(userManager.getSystem().asSubject());
				} else {
					var accessToken = accessTokenManager.findByValue(bearerToken);
					if (accessToken != null) {
						ThreadContext.bind(accessToken.asSubject());
					} else if (agentTokenManager.find(bearerToken) == null 
							&& jobManager.getJobContext(bearerToken, false) == null) {
						throw new IncorrectCredentialsException("Invalid or expired access token");
					}
				}
	        } 
		}
		return true;
	}

}
