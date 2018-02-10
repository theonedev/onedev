package com.turbodev.server.web.component.diff.revision;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.support.MarkPos;

public interface CommentSupport extends Serializable {
	
	@Nullable MarkPos getMark();
	
	String getMarkUrl(MarkPos mark);
	
	void onMark(AjaxRequestTarget target, MarkPos mark);
	
	String getCommentUrl(CodeComment comment);
	
	@Nullable CodeComment getOpenComment();

	void onCommentOpened(AjaxRequestTarget target, @Nullable CodeComment comment);
	
	void onAddComment(AjaxRequestTarget target, MarkPos markPos);

}
