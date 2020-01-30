package io.onedev.server.model.support.issue.changedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;

public class IssueMilestoneChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldMilestone;
	
	private final String newMilestone;
	
	public IssueMilestoneChangeData(@Nullable Milestone oldMilestone, @Nullable Milestone newMilestone) {
		this.oldMilestone = oldMilestone!=null?oldMilestone.getName():null;
		this.newMilestone = newMilestone!=null?newMilestone.getName():null;
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
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
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

	@Override
	public boolean affectsBoards() {
		return true;
	}
	
}
