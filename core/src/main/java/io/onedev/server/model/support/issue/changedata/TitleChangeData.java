package io.onedev.server.model.support.issue.changedata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.utils.HtmlUtils;
import jersey.repackaged.com.google.common.collect.Lists;

public class TitleChangeData implements ActionData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public TitleChangeData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public Component render(String componentId, IssueAction action) {
		return new PlainDiffPanel(componentId, Lists.newArrayList(oldTitle), "a.txt", Lists.newArrayList(newTitle), "b.txt", true);
	}
	
	@Override
	public List<String> getOldLines() {
		return Lists.newArrayList(oldTitle);
	}

	@Override
	public List<String> getNewLines() {
		return Lists.newArrayList(newTitle);
	}
	
	@Override
	public String getTitle(IssueAction action, boolean external) {
		Issue issue = action.getIssue();
		if (external) 
			return String.format("[Title Changed] Issue #%d: %s", issue.getNumber(), issue.getTitle());  
		else 
			return "changed title";
	}

	@Override
	public String describeAsHtml(IssueAction action) {
		String escaped = HtmlUtils.escapeHtml(action.getUser().getDisplayName());
		StringBuilder builder = new StringBuilder(String.format("<b>%s changed title</b>", escaped));
		builder.append("<p style='margin: 16px 0;'>");
		builder.append(DiffUtils.diffAsHtml(Lists.newArrayList(oldTitle), "a.txt", Lists.newArrayList(newTitle), "b.txt", true));
		return builder.toString();
	}

	@Override
	public CommentSupport getCommentSupport() {
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
