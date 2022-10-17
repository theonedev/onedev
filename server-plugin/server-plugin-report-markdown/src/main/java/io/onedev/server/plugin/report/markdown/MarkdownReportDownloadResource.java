package io.onedev.server.plugin.report.markdown;

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
import org.apache.tika.mime.MimeTypes;
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
import io.onedev.server.storage.StorageManager;

public class MarkdownReportDownloadResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";

	private static final String PARAM_BUILD = "build";

	private static final String PARAM_REPORT = "report";
	
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
		
		String reportName = params.get(PARAM_REPORT).toOptionalString();
		
		if (reportName == null)
			throw new IllegalArgumentException("Markdown report name has to be specified");
		
		if (!SecurityUtils.canAccessReport(build, reportName))
			throw new UnauthorizedException();
			
		List<String> pathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String pathSegment = params.get(i).toString();
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}
		
		String markdownPath = Joiner.on("/").join(pathSegments);
		
		File buildDir = OneDev.getInstance(StorageManager.class).getBuildDir(project.getId(), build.getNumber());
		File reportDir = new File(buildDir, PublishMarkdownReportStep.CATEGORY + "/" + reportName);
		
		File markdownFile = new File(reportDir, markdownPath);
		if (!markdownFile.exists() || markdownFile.isDirectory()) {
			String message = String.format("Specified markdown path does not exist or is a directory (project: %s, build number: %d, path: %s)", 
					project.getPath(), build.getNumber(), markdownPath);
			throw new ExplicitException(message);
		}
			
		ResourceResponse response = new ResourceResponse();
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.setContentType(MimeTypes.OCTET_STREAM);
		
		response.disableCaching();
		
		try {
			response.setFileName(URLEncoder.encode(markdownFile.getName(), StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				LockUtils.read(PublishMarkdownReportStep.getReportLockName(build), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						try (InputStream is = new FileInputStream(markdownFile)) {
							IOUtils.copy(is, attributes.getResponse().getOutputStream());
						}
						return null;
					}
					
				});
			}			
			
		});

		return response;
	}

}
