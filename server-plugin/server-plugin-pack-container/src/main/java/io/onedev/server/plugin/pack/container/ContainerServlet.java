package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.exception.ChallengeAwareUnauthenticatedException;
import io.onedev.server.job.JobManager;
import io.onedev.server.pack.PackServlet;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@Singleton
public class ContainerServlet extends PackServlet {
	
	public static final String PATH = "/v2";

	private final SettingManager settingManager;
	
	private final SessionManager sessionManager;
	
	private final UserManager userManager;

	@Inject
	public ContainerServlet(SettingManager settingManager, JobManager jobManager, 
							BuildManager buildManager, ObjectMapper objectMapper, 
							SessionManager sessionManager, UserManager userManager) {
		super(jobManager, buildManager, objectMapper);
		this.settingManager = settingManager;
		this.sessionManager = sessionManager;
		this.userManager = userManager;
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		var pathInfo = request.getPathInfo();
		if (pathInfo == null)
			pathInfo = "";
		else 
			pathInfo = StringUtils.strip(pathInfo, "/");

		String possibleJobToken;
		Long userId = null;
		var auth = request.getHeader("Authorization");
		if (auth != null && auth.startsWith("Bearer ")) {
			var bearerAuth = auth.substring("Bearer ".length());
			possibleJobToken = StringUtils.substringBefore(bearerAuth, ":");
			var accessToken = StringUtils.substringAfter(bearerAuth, ":");
			var user = userManager.findByAccessToken(accessToken);
			if (user != null)
				userId = user.getId();
		}

		if (userId != null)
			SecurityUtils.getSubject().runAs(SecurityUtils.asPrincipal(userId));
		try {
			if (pathInfo.equals("")) {
				if (SecurityUtils.getUserId().equals(0L))
					throw new ChallengeAwareUnauthenticatedException(getChallenge(), "Please login");
				else
					response.setStatus(SC_OK);
			} else if (pathInfo.equals("token")) {
				var jsonObj = new HashMap<String, String>();
				String accessToken;
				var user = SecurityUtils.getUser();
				if (user != null)
					accessToken = userManager.createTemporalAccessToken(user.getId(), 3600);
				else
					accessToken = CryptoUtils.generateSecret();
				jsonObj.put("token", getPossibleJobToken(request) + ":" + accessToken);
				sendResponse(response, SC_OK, jsonObj);
			} else {
				response.setStatus(SC_NOT_IMPLEMENTED);
			}
		} finally {
			if (userId != null)
				SecurityUtils.getSubject().releaseRunAs();
		}
	}

	private String getChallenge() {
		return "Bearer realm=\"" + settingManager.getSystemSetting().getServerUrl() + "/v2/token\",service=\"container_registry\",scope=\"*\"";
	}
	
}
