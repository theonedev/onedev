package io.onedev.server.pack;

import static org.apache.shiro.SecurityUtils.getSubject;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.ThreadContext;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.job.JobService;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.ExceptionHandleFilter;

@Singleton
public class PackFilter extends ExceptionHandleFilter {
	
	@Inject
	private AccessTokenService accessTokenService;
	
	@Inject
	private ProjectService projectService;
	
	@Inject
	private JobService jobService;
	
	@Inject
	private SessionService sessionService;

	@Inject
	private Set<PackHandler> packHandlers;
		
	@Sessional
    @Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
		var httpRequest = (HttpServletRequest) request;
		var httpResponse = (HttpServletResponse) response;
		var pathSegments = Splitter.on('/').trimResults().omitEmptyStrings()
				.splitToList(httpRequest.getRequestURI());
		for (var packHandler: packHandlers) {
			var handlerMark = "~" + packHandler.getHandlerId();
			if (pathSegments.contains(handlerMark)) {
				pathSegments = packHandler.normalize(pathSegments);
				var handlerMarkIndex = pathSegments.indexOf(handlerMark);
				request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);
				var projectPath = Joiner.on('/').join(pathSegments.subList(0, handlerMarkIndex));
				var projectId = sessionService.call(() -> {
					var project = projectService.findByPath(projectPath);
					if (project != null)
						return project.getId();
					else
						throw new EntityNotFoundException("No project found with path '" + projectPath + "'");
				});

				Long buildId = null;
				var apiKey = packHandler.getApiKey(httpRequest);
				if (apiKey != null) {
					var colonIndex = apiKey.indexOf(':');
					String jobToken;
					String accessTokenValue;
					if (colonIndex != -1) {
						jobToken = apiKey.substring(0, colonIndex);
						accessTokenValue = apiKey.substring(colonIndex +1);
					} else {
						jobToken = null;
						accessTokenValue = apiKey;
					}
					if (jobToken != null) {
						var jobContext = jobService.getJobContext(jobToken, false);
						if (jobContext != null)
							buildId = jobContext.getBuildId();
					}
					var accessToken = accessTokenService.findByValue(accessTokenValue);
					if (accessToken != null)
						ThreadContext.bind(accessToken.asSubject());
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
							var jobContext = jobService.getJobContext(userName, false);
							if (jobContext != null)
								buildId = jobContext.getBuildId();
							if (password.length() != 0) {
								var accessToken = accessTokenService.findByValue(password);
								if (accessToken != null)
									ThreadContext.bind(accessToken.asSubject());
								else
									getSubject().login(new UsernamePasswordToken(userName, password));
							}
						}
					}
				}
				
				packHandler.handle(httpRequest, httpResponse, projectId, buildId,
						pathSegments.subList(handlerMarkIndex + 1, pathSegments.size()));
				return false;
			}
		}
		return true;
	}

}
