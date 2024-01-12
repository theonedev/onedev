package io.onedev.server.pack;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.BearerAuthenticationToken;
import io.onedev.server.security.ExceptionHandleFilter;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.util.Set;

@Singleton
public class PackFilter extends ExceptionHandleFilter {
	
	private final UserManager userManager;
	
	private final ProjectManager projectManager;
	
	private final JobManager jobManager;
	
	private final SessionManager sessionManager;

	private final Set<PackService> packServices;
	
	@Inject
	public PackFilter(UserManager userManager, ProjectManager projectManager, 
					  JobManager jobManager, SessionManager sessionManager, 
					  Set<PackService> packServices) {
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.jobManager = jobManager;
		this.sessionManager = sessionManager;
		this.packServices = packServices;
	}
	
	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
		var httpRequest = (HttpServletRequest) request;
		var httpResponse = (HttpServletResponse) response;
		var pathSegments = Splitter.on('/').trimResults().omitEmptyStrings()
				.splitToList(httpRequest.getRequestURI());
		for (var packService: packServices) {
			var serviceMark = "~" + packService.getServiceId();
			var serviceMarkIndex = pathSegments.indexOf(serviceMark);
			if (serviceMarkIndex != -1) {
				request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);
				var projectPath = Joiner.on('/').join(pathSegments.subList(0, serviceMarkIndex));
				var projectId = sessionManager.call(() -> {
					var project = projectManager.findByPath(projectPath);
					if (project != null)
						return project.getId();
					else
						throw new EntityNotFoundException("No project found with path '" + projectPath + "'");
				});

				Long buildId = null;
				var apiKey = packService.getApiKey(httpRequest);
				if (apiKey != null) {
					var colonIndex = apiKey.indexOf(':');
					String jobToken;
					String accessToken;
					if (colonIndex != -1) {
						jobToken = apiKey.substring(0, colonIndex);
						accessToken = apiKey.substring(colonIndex +1);
					} else {
						jobToken = null;
						accessToken = apiKey;
					}
					if (jobToken != null) {
						var jobContext = jobManager.getJobContext(jobToken, false);
						if (jobContext != null)
							buildId = jobContext.getBuildId();
					}
					var user = userManager.findByAccessToken(accessToken);
					var subject = SecurityUtils.getSubject();
					if (user != null)
						subject.login(new BearerAuthenticationToken(user));
					else
						throw new UnauthorizedException();
				} else {
					var authzHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
					if (authzHeader != null && authzHeader.toLowerCase().startsWith("basic ")) {
						String authValue = StringUtils.substringAfter(authzHeader, " ");
						String decoded = Base64.decodeToString(authValue);
						String userName = StringUtils.substringBefore(decoded, ":").trim();
						String password = StringUtils.substringAfter(decoded, ":").trim();
						if (userName.length() != 0) {
							var jobContext = jobManager.getJobContext(userName, false);
							if (jobContext != null)
								buildId = jobContext.getBuildId();
							if (password.length() != 0) {
								User user = userManager.findByAccessToken(password);
								Subject subject = SecurityUtils.getSubject();
								if (user != null)
									subject.login(new BearerAuthenticationToken(user));
								else
									subject.login(new UsernamePasswordToken(userName, password));
							}
						}
					}
				}
				
				packService.service(httpRequest, httpResponse, projectId, buildId,
						pathSegments.subList(serviceMarkIndex + 1, pathSegments.size()));
				return false;
			}
		}
		return true;
	}

}
