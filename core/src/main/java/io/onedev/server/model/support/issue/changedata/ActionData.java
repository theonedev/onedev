package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentSupport;

public interface ActionData extends Serializable {
	
	Component render(String componentId, IssueAction action);
	
	String getTitle(IssueAction action, boolean external);
	
	String describeAsHtml(IssueAction action);
	
	List<String> getOldLines();
	
	List<String> getNewLines();
	
	@Nullable
	CommentSupport getCommentSupport();
	
	Map<String, User> getNewUsers();
	
	Map<String, Group> getNewGroups();
	
}
