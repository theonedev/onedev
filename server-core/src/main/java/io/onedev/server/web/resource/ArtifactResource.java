package io.onedev.server.web.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.base.Joiner;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ContentDetector;

public class ArtifactResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";

	private static final String PARAM_BUILD = "build";

	private static final String PARAM_PATH = "path";
	
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
		
		if (!SecurityUtils.canAccess(build))
			throw new UnauthorizedException();
		
		List<String> pathSegments = new ArrayList<>();
		String pathSegment = params.get(PARAM_PATH).toString();
		if (pathSegment.length() != 0)
			pathSegments.add(pathSegment);
		else
			throw new ExplicitException("Artifact path has to be specified");

		for (int i = 0; i < params.getIndexedCount(); i++) {
			pathSegment = params.get(i).toString();
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}
		
		String artifactPath = Joiner.on("/").join(pathSegments);
		
		File artifactsDir = build.getArtifactsDir();
		File artifactFile = new File(artifactsDir, artifactPath);
		if (!artifactFile.exists() || artifactFile.isDirectory()) {
			String message = String.format("Specified artifact path does not exist or is a directory (project: %s, build number: %d, path: %s)", 
					project.getName(), build.getNumber(), artifactPath);
			throw new ExplicitException(message);
		}
			
		ResourceResponse response = new ResourceResponse();
		try (InputStream is = new BufferedInputStream(new FileInputStream(artifactFile))) {
			response.setContentType(ContentDetector.detectMediaType(is, artifactPath).toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode(artifactFile.getName(), StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		response.setContentLength(artifactFile.length());
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				LockUtils.read(build.getArtifactsLockKey(), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						try (InputStream is = new FileInputStream(artifactFile)) {
							IOUtils.copy(is, attributes.getResponse().getOutputStream());
						}
						return null;
					}
					
				});
			}			
			
		});

		return response;
	}

	public static PageParameters paramsOf(Project project, Long buildNumber, String path) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getName());
		params.set(PARAM_BUILD, buildNumber);
		params.set(PARAM_PATH, path);
		return params;
	}

}
