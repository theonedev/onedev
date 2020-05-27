package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentAware;

public interface IssueChangeData extends Serializable {
	
	Component render(String componentId, IssueChange change);
	
	String getActivity(@Nullable Issue withIssue);

	@Nullable
	CommentAware getCommentAware();
	
	Map<String, Collection<User>> getNewUsers();
	
	Map<String, Group> getNewGroups();
	
	boolean affectsBoards();
}
