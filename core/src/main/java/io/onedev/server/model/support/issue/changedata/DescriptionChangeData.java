package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.google.common.base.Splitter;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.utils.HtmlUtils;

public class DescriptionChangeData implements ChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public DescriptionChangeData(@Nullable String oldDescription, @Nullable String newDescription) {
		this.oldDescription = oldDescription;
		this.newDescription = newDescription;
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
		return new PlainDiffPanel(componentId, getLines(oldDescription), getLines(newDescription));
	}

	@Override
	public String getTitle(IssueChange change, boolean external) {
		Issue issue = change.getIssue();
		if (external) 
			return String.format("[Description Changed] Issue #%d: %s", issue.getNumber(), issue.getTitle());  
		else 
			return "changed description";
	}

	private List<String> getLines(@Nullable String description) {
		List<String> lines = new ArrayList<>();
		if (description != null)
			lines = Splitter.on("\n").splitToList(description);
		else
			lines = new ArrayList<>();
		return lines;
	}
	
	@Override
	public String describeAsHtml(IssueChange change) {
		String escaped = HtmlUtils.escapeHtml(change.getUser().getDisplayName());
		StringBuilder builder = new StringBuilder(String.format("<b>%s changed description</b>", escaped));
		builder.append("<p style='margin: 16px 0;'>");
		builder.append(DiffUtils.diffAsHtml(getLines(oldDescription), getLines(newDescription)));
		return builder.toString();
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public Map<String, User> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

}
