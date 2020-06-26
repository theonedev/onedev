package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.Mark;

public interface BlobCommentSupport extends Serializable {
	
	@Nullable Mark getMark();
	
	String getMarkUrl(Mark mark);
	
	Collection<CodeComment> getComments();
	
	@Nullable CodeComment getOpenComment();

	void onOpenComment(AjaxRequestTarget target, CodeComment comment);
	
	void onAddComment(AjaxRequestTarget target, Mark mark);
	
	Component getDirtyContainer();
	
}
