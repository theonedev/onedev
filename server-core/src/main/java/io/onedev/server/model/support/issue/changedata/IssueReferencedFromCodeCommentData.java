package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.rest.annotation.EntityId;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.codecomment.referencedfrom.ReferencedFromCodeCommentPanel;

public class IssueReferencedFromCodeCommentData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	@EntityId(CodeComment.class)
	private final Long commentId;
	
	public IssueReferencedFromCodeCommentData(CodeComment comment) {
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public String getActivity() {
		return "Referenced from code comment";
	}

	@Override
	public Component render(String componentId, IssueChange change) {
		return new ReferencedFromCodeCommentPanel(componentId, commentId);
	}

	@Override
	public CommentAware getCommentAware() {
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
		return false;
	}

}
