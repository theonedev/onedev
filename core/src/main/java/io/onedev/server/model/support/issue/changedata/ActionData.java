package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.IssueAction;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentSupport;
import io.onedev.server.model.support.DiffSupport;

public interface ActionData extends Serializable {
	
	Component render(String componentId, IssueAction action);
	
	String getDescription();

	@Nullable
	DiffSupport getDiffSupport();
	
	@Nullable
	CommentSupport getCommentSupport();
	
	Map<String, User> getNewUsers(Project project);
	
	Map<String, Team> getNewTeams(Project project);
	
}
