package com.turbodev.server.web.component.diff.blob;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.support.MarkPos;

public interface SourceAware {
	
	void onCommentDeleted(AjaxRequestTarget target, CodeComment comment);
	
	void onCommentClosed(AjaxRequestTarget target, CodeComment comment);

	void onCommentAdded(AjaxRequestTarget target, CodeComment comment);
	
	void mark(AjaxRequestTarget target, @Nullable MarkPos mark);

	void onUnblame(AjaxRequestTarget target);
}
