package io.onedev.server.model.support.pullrequest.changedata;

import java.util.List;

import org.apache.wicket.Component;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;

public class PullRequestDescriptionChangeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public PullRequestDescriptionChangeData(String oldDescription, String newDescription) {
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "changed description";
		if (withRequest != null)
			activity += " of pull request " + withRequest.describe();
		return activity;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		List<String> oldLines = StringUtils.splitAndTrim(oldDescription, "\n");
		List<String> newLines = StringUtils.splitAndTrim(newDescription, "\n");
		return new PlainDiffPanel(componentId, oldLines, "a.txt", newLines, "b.txt", true);
	}
	
	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

}
