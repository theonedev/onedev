package io.onedev.server.model.support.issue.changedata;

import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.codecomment.referencedfrom.ReferencedFromCodeCommentPanel;

public class IssueReferencedFromCodeCommentData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	public IssueReferencedFromCodeCommentData(CodeComment comment) {
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public String getDescription() {
		return "Referenced from code comment";
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new ReferencedFromCodeCommentPanel(componentId, commentId);
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

}
