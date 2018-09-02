package io.onedev.server.model.support.issue.changedata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.IssueAction;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
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
	public DiffSupport getDiffSupport() {
		return new DiffSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> getOldLines() {
				return Lists.newArrayList(oldTitle);
			}

			@Override
			public List<String> getNewLines() {
				return Lists.newArrayList(newTitle);
			}

			@Override
			public String getOldFileName() {
				return "a.txt";
			}

			@Override
			public String getNewFileName() {
				return "b.txt";
			}
			
		};
	}

	@Override
	public String getDescription() {
		return "changed title";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}
	
	@Override
	public Map<String, User> getNewUsers(Project project) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Team> getNewTeams(Project project) {
		return new HashMap<>();
	}
	
}
