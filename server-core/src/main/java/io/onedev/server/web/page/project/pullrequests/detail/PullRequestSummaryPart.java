package io.onedev.server.web.page.project.pullrequests.detail;

import org.apache.wicket.Component;

public abstract class PullRequestSummaryPart {

	private final String title;
	
	public PullRequestSummaryPart(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public abstract Component render(String componentId);
	
}
