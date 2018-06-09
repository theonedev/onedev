package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;

public interface ChangeData extends Serializable {
	
	Component render(String componentId, IssueChange change);
	
	String getTitle(IssueChange change, boolean external);
	
	String describeAsHtml(IssueChange change);
	
	List<String> getOldLines();
	
	List<String> getNewLines();
	
	@Nullable
	CommentSupport getCommentSupport();
	
	Map<String, User> getNewUsers();
	
	Map<String, Group> getNewGroups();
	
}
