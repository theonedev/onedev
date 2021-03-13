package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestSummaryPart;

public class PullRequestSummaryMarkdownPart extends PullRequestSummaryPart {

	private final File reportDir;
	
	public PullRequestSummaryMarkdownPart(File reportDir) {
		super(reportDir.getName());
		this.reportDir = reportDir;
	}

	@Override
	public Component render(String componentId) {
		try {
			File file = new File(reportDir, JobPullRequestMarkdownReport.FILE);
			String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			return new MarkdownViewer(componentId, Model.of(markdown), null)
					.add(AttributeAppender.append("class", "mb-n3"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
