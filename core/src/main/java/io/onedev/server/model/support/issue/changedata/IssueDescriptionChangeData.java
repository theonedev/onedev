package io.onedev.server.model.support.issue.changedata;

import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.utils.StringUtils;

public class IssueDescriptionChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public IssueDescriptionChangeData(String oldDescription, String newDescription) {
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
		List<String> oldLines = StringUtils.splitAndTrim(oldDescription, "\n");
		List<String> newLines = StringUtils.splitAndTrim(newDescription, "\n");
		return new PlainDiffPanel(componentId, oldLines, "a.txt", newLines, "b.txt", true);
	}
	
	@Override
	public String getDescription() {
		return "changed description";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}
	
	@Override
	public Map<String, User> getNewUsers(Project project) {
		return null;
	}

	@Override
	public Map<String, Group> getNewGroups(Project project) {
		return null;
	}

	@Override
	public boolean affectsBoards() {
		return false;
	}
	
}
