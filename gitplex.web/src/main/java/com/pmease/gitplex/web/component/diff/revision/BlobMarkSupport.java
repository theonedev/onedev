package com.pmease.gitplex.web.component.diff.revision;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.entity.CodeComment;

public interface BlobMarkSupport extends Serializable {
	
	Collection<CodeComment> getComments();
	
	@Nullable DiffMark getMark();
	
	String getMarkUrl(DiffMark mark);
	
	@Nullable CodeComment getOpenComment();

	void onOpenComment(AjaxRequestTarget target, CodeComment comment);
	
	void onAddComment(AjaxRequestTarget target, DiffMark mark);
	
	Component getDirtyContainer();
	
}
