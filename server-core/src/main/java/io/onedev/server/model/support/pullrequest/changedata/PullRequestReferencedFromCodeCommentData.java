package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.notification.ActivityDetail;

public class PullRequestReferencedFromCodeCommentData 
		extends PullRequestChangeData implements ReferencedFromAware<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	public PullRequestReferencedFromCodeCommentData(CodeComment comment) {
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public String getActivity() {
		return "referenced from code comment";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public CodeComment getReferencedFrom() {
		return OneDev.getInstance(CodeCommentManager.class).get(commentId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
