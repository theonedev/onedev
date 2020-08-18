package io.onedev.server.web.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.LogManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

public class BuildLogResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_BUILD = "build";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectName = params.get(PARAM_PROJECT).toString();
		if (StringUtils.isBlank(projectName))
			throw new IllegalArgumentException("project name has to be specified");
		
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project: " + projectName);
		
		Long buildNumber = params.get(PARAM_BUILD).toOptionalLong();
		
		if (buildNumber == null)
			throw new IllegalArgumentException("build number has to be specified");
		
		Build build = OneDev.getInstance(BuildManager.class).find(project, buildNumber);

		if (build == null) {
			String message = String.format("Unable to find build (project: %s, build number: %d)", 
					project.getName(), buildNumber);
			throw new EntityNotFoundException(message);
		}
		
		if (!SecurityUtils.canAccessLog(build))
			throw new UnauthorizedException();
	
		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode("build-log.txt", StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (InputStream is = OneDev.getInstance(LogManager.class).openLogStream(build)) {
					IOUtils.copy(is, attributes.getResponse().getOutputStream());
				}
			}			
			
		});

		return response;
	}

	public static PageParameters paramsOf(Project project, Long buildNumber) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getName());
		params.set(PARAM_BUILD, buildNumber);
		return params;
	}
	
}
