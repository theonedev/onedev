package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.support.Mark;

public interface CommentSupport extends Serializable {
	
	@Nullable Mark getMark();
	
	@Nullable
	String getMarkUrl(Mark mark);
	
	void onMark(AjaxRequestTarget target, Mark mark);
	
	void onUnmark(AjaxRequestTarget target);
	
	@Nullable CodeComment getOpenComment();

	void onCommentOpened(AjaxRequestTarget target, CodeComment comment);
	
	void onCommentClosed(AjaxRequestTarget target);
	
	void onAddComment(AjaxRequestTarget target, Mark mark);

	Collection<CodeComment> getComments();
	
	void onSaveComment(CodeComment comment);
	
	void onSaveCommentReply(CodeCommentReply reply);
	
}
