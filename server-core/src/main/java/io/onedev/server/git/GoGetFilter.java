package io.onedev.server.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;

@Singleton
public class GoGetFilter implements Filter {
	
	private final ProjectManager projectManager;
	
	private final UrlManager urlManager;
	
	private final SessionManager sessionManager;
	
	@Inject
	public GoGetFilter(ProjectManager projectManager, UrlManager urlManager, SessionManager sessionManager) {
		this.projectManager = projectManager;
		this.urlManager = urlManager;
		this.sessionManager = sessionManager;
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (httpRequest.getQueryString() != null && httpRequest.getQueryString().contains("go-get=1")) {
			sessionManager.openSession();
			try {
				String hostAndPath = StringUtils.substringAfter(httpRequest.getRequestURL().toString(), "://");
				String host = StringUtils.substringBefore(hostAndPath, "/");
				String path = StringUtils.substringAfter(hostAndPath, "/");
				
				Project parent = null;
				for (String pathSegment: Splitter.on("/").trimResults().omitEmptyStrings().split(path)) {
					Project project = projectManager.find(parent, pathSegment);
					if (project != null) 
						parent = project;
					else 
						break;
				}
				if (parent != null) {
					StringBuilder builder = new StringBuilder();
					builder.append(String.format(""
							+ "<!doctype html>\n"
							+ "<html lang=\"en\">\n"
							+ "<head>\n"
							+ "<meta charset=\"utf-8\">\n"
							+ "<meta name=\"go-import\" content=\"%s git %s\">\n"
							+ "</head>\n"
							+ "</html>", host + "/" + parent.getPath(), urlManager.cloneUrlFor(parent, false)));
					byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
					httpResponse.getOutputStream().write(bytes);
					httpResponse.setStatus(HttpServletResponse.SC_OK);
				} else {
					httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			} finally {
				sessionManager.closeSession();
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	@Override
	public void destroy() {
	}
	
}
 