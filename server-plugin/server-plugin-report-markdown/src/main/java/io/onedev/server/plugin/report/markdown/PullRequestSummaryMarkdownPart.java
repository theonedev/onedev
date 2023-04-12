package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestSummaryPart;

public class PullRequestSummaryMarkdownPart extends PullRequestSummaryPart {

	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final Long buildNumber;
	
	public PullRequestSummaryMarkdownPart(Long projectId, Long buildNumber, String reportName) {
		super(reportName);
		this.projectId = projectId;
		this.buildNumber = buildNumber;
	}
	
	@Override
	public Component render(String componentId) {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		String markdown = projectManager.runOnActiveServer(projectId, new ClusterTask<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String call() throws Exception {
				File categoryDir = new File(Build.getStorageDir(projectId, buildNumber), PublishPullRequestMarkdownReportStep.CATEGORY);
				File file = new File(new File(categoryDir, getReportName()), PublishPullRequestMarkdownReportStep.CONTENT);
				return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			}
			
		});
		return new MarkdownViewer(componentId, Model.of(markdown), null)
				.add(AttributeAppender.append("class", "mb-n3"));
	}

}
