package io.onedev.server.plugin.report.html;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HtmlReportDownloadResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_BUILD = "build";

	private static final String PARAM_REPORT = "report";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		var params = attributes.getParameters();

		var projectId = params.get(PARAM_PROJECT).toLong();
		var project = OneDev.getInstance(ProjectManager.class).load(projectId);
		
		var buildNumber = params.get(PARAM_BUILD).toLong();
		var build = OneDev.getInstance(BuildManager.class).find(project, buildNumber);
		if (build == null) {
			String message = String.format("Unable to find build (project: %s, build number: %d)",
					project.getPath(), buildNumber);
			throw new EntityNotFoundException(message);
		}
		
		String reportName = params.get(PARAM_REPORT).toString();

		if (!SecurityUtils.canAccessReport(build, reportName))
			throw new UnauthorizedException();

		List<String> pathSegments = new ArrayList<>();

		for (int i = 0; i < params.getIndexedCount(); i++) {
			String pathSegment = params.get(i).toString();
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}

		if (pathSegments.isEmpty())
			throw new ExplicitException("File path has to be specified");

		var filePath = Joiner.on("/").join(pathSegments);
		var fileName = filePath;
		if (fileName.contains("/"))
			fileName = StringUtils.substringAfterLast(fileName, "/");
		
		ResourceResponse response = new ResourceResponse();
		try {
			response.setContentType(Files.probeContentType(Paths.get(filePath)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		response.disableCaching();
		response.setFileName(URLEncoder.encode(fileName, UTF_8));
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				var fileContent = OneDev.getInstance(ProjectManager.class).runOnActiveServer(
						projectId,
						new ReadPublishedFile(projectId, buildNumber, reportName, filePath));
				attributes.getResponse().write(fileContent);
			}			
			
		});

		return response;
	}

	public static PageParameters paramsOf(Build build, String reportName, String path) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PROJECT, build.getProject().getId());
		params.add(PARAM_BUILD, build.getNumber());
		params.add(PARAM_REPORT, reportName);

		int index = 0;
		for (String segment: Splitter.on("/").split(path)) {
			params.set(index, segment);
			index++;
		}
		
		return params;
	}

}
