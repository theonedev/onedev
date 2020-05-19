package io.onedev.server.web.component.diff.blob;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.Mark;

public interface SourceAware {
	
	void onCommentDeleted(AjaxRequestTarget target, CodeComment comment);
	
	void onCommentClosed(AjaxRequestTarget target, CodeComment comment);

	void onCommentAdded(AjaxRequestTarget target, CodeComment comment);
	
	void mark(AjaxRequestTarget target, Mark mark);

	void unmark(AjaxRequestTarget target);
	
	void onUnblame(AjaxRequestTarget target);
}
