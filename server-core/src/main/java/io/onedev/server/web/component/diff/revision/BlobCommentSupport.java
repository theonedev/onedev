package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.MarkPos;

public interface BlobCommentSupport extends Serializable {
	
	@Nullable MarkPos getMark();
	
	String getMarkUrl(MarkPos mark);
	
	Collection<CodeComment> getComments();
	
	@Nullable CodeComment getOpenComment();

	void onToggleComment(AjaxRequestTarget target, CodeComment comment);
	
	void onAddComment(AjaxRequestTarget target, MarkPos markPos);
	
	Component getDirtyContainer();
	
}
