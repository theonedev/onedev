package io.onedev.server.model.support.pullrequest.changedata;

import java.util.List;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.utils.StringUtils;

public class PullRequestDescriptionChangeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public PullRequestDescriptionChangeData(String oldDescription, String newDescription) {
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}
	
	@Override
	public String getDescription() {
		return "changed description";
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
