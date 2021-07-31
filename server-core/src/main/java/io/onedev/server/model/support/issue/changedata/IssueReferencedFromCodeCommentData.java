package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.rest.annotation.EntityId;
import io.onedev.server.util.CommentAware;

public class IssueReferencedFromCodeCommentData extends IssueChangeData implements ReferencedFromAware<CodeComment> {

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

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}

	@Override
	public CodeComment getReferencedFrom() {
		return OneDev.getInstance(CodeCommentManager.class).get(commentId);
	}

}
