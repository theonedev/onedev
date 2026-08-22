package io.onedev.server.plugin.report.unittest;

import static io.onedev.server.codequality.UnitTestReport.ARTIFACTS;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.codequality.UnitTestReport;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.util.MimeUtils;

public class ArtifactResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";

	private static final String PARAM_BUILD = "build";

	private static final String PARAM_REPORT = "report";

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		var params = attributes.getParameters();
		var projectId = params.get(PARAM_PROJECT).toLong();
		var buildNumber = params.get(PARAM_BUILD).toLong();
		String reportName = params.get(PARAM_REPORT).toString();
		if (reportName.contains(".."))
			throw new ExplicitException("Invalid request path");

		if (!SecurityUtils.isSystem()) {
			var project = OneDev.getInstance(ProjectService.class).load(projectId);
			var build = OneDev.getInstance(BuildService.class).find(project, buildNumber);
			if (build == null) {
				throw new EntityNotFoundException(String.format(
						"Unable to find build (project: %s, build number: %d)",
						project.getPath(), buildNumber));
			}
			if (!SecurityUtils.canAccessReport(build, reportName))
				throw new UnauthorizedException();
		}

		List<String> pathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String pathSegment = params.get(i).toString();
			if (pathSegment.contains(".."))
				throw new ExplicitException("Invalid request path");
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}
		String artifactPath = Joiner.on("/").join(pathSegments);
		if (!artifactPath.startsWith(ARTIFACTS + "/"))
			throw new ExplicitException("Invalid artifact request path");

		String fileName = StringUtils.substringAfterLast(artifactPath, "/");
		ResourceResponse response = new ResourceResponse();
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.setContentDisposition(ContentDisposition.ATTACHMENT);
		try {
			response.setContentType(MimeUtils.sanitize(Files.probeContentType(Paths.get(artifactPath))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		response.disableCaching();
		response.setFileName(URLEncoder.encode(fileName, UTF_8));
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				UnitTestReport.downloadArtifact(projectId, buildNumber, reportName, artifactPath,
						attributes.getResponse().getOutputStream());
			}

		});
		return response;
	}

	public static PageParameters paramsOf(Build build, String reportName, String path) {
		return paramsOf(build.getProject().getId(), build.getNumber(), reportName, path);
	}

	public static PageParameters paramsOf(Long projectId, Long buildNumber, String reportName, String path) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PROJECT, projectId);
		params.add(PARAM_BUILD, buildNumber);
		params.add(PARAM_REPORT, reportName);
		int index = 0;
		for (String segment: Splitter.on("/").split(path))
			params.set(index++, segment);
		return params;
	}

}
