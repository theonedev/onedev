package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.web.component.codecomment.referencedfrom.ReferencedFromCodeCommentPanel;

public class PullRequestReferencedFromCodeCommentData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	public PullRequestReferencedFromCodeCommentData(CodeComment comment) {
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return new ReferencedFromCodeCommentPanel(componentId, commentId);
	}
	
	@Override
	public String getDescription() {
		return "referenced from code comment";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

}
