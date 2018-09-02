package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.IssueAction;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;

public class MilestoneChangeData implements ActionData {

	private static final long serialVersionUID = 1L;

	private final String oldMilestone;
	
	private final String newMilestone;
	
	public MilestoneChangeData(@Nullable Milestone oldMilestone, @Nullable Milestone newMilestone) {
		this.oldMilestone = oldMilestone!=null?oldMilestone.getName():null;
		this.newMilestone = newMilestone!=null?newMilestone.getName():null;
	}
	
	@Override
	public Component render(String componentId, IssueAction action) {
		return new PlainDiffPanel(componentId, getOldLines(), "a.txt", getNewLines(), "b.txt", true);
	}
	
	private List<String> getOldLines() {
		List<String> oldLines = new ArrayList<>();
		if (oldMilestone != null)
			oldLines.add("Milestone: " + oldMilestone);
		else
			oldLines.add("Milestone: ");
		return oldLines;
	}

	private List<String> getNewLines() {
		List<String> newLines = new ArrayList<>();
		if (newMilestone != null)
			newLines.add("Milestone: " + newMilestone);
		else
			newLines.add("Milestone: ");
		return newLines;
	}
	
	@Override
	public DiffSupport getDiffSupport() {
		return new DiffSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> getOldLines() {
				return getOldLines();
			}

			@Override
			public List<String> getNewLines() {
				return getNewLines();
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
	
	public String getOldMilestone() {
		return oldMilestone;
	}

	public String getNewMilestone() {
		return newMilestone;
	}

	@Override
	public String getDescription() {
		return "changed milestone";
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
