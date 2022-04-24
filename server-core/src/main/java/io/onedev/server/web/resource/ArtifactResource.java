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

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

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

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		Long projectId = params.get(PARAM_PROJECT).toLong();
		Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
		
		Long buildNumber = params.get(PARAM_BUILD).toOptionalLong();
		
		if (buildNumber == null)
			throw new IllegalArgumentException("build number has to be specified");
		
		Build build = OneDev.getInstance(BuildManager.class).find(project, buildNumber);

		if (build == null) {
			String message = String.format("Unable to find build (project: %s, build number: %d)", 
					project.getPath(), buildNumber);
			throw new EntityNotFoundException(message);
		}
		
		if (!SecurityUtils.canAccess(build))
			throw new UnauthorizedException();
		
		List<String> pathSegments = new ArrayList<>();

		for (int i = 0; i < params.getIndexedCount(); i++) {
			String pathSegment = params.get(i).toString();
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}
		
		if (pathSegments.isEmpty())
			throw new ExplicitException("Artifact path has to be specified");
		
		String artifactPath = Joiner.on("/").join(pathSegments);
		
		File artifactsDir = build.getArtifactsDir();
		File artifactFile = new File(artifactsDir, artifactPath);
		if (!artifactFile.exists() || artifactFile.isDirectory()) {
			String message = String.format("Specified artifact path does not exist or is a directory (project: %s, build number: %d, path: %s)", 
					project.getPath(), build.getNumber(), artifactPath);
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
		params.set(PARAM_PROJECT, project.getId());
		params.set(PARAM_BUILD, buildNumber);
		
		int index = 0;
		for (String segment: Splitter.on("/").split(path)) {
			params.set(index, segment);
			index++;
		}
		return params;
	}

}
