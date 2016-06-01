package com.pmease.gitplex.web.component.diff.revision;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.entity.CodeComment;

public interface MarkSupport extends Serializable {
	
	@Nullable DiffMark getMark();
	
	void onMark(AjaxRequestTarget target, DiffMark mark);
	
	String getMarkUrl(DiffMark mark);
	
	String getCommentUrl(CodeComment comment);
	
	@Nullable CodeComment getOpenComment();

	void onCommentOpened(AjaxRequestTarget target, @Nullable CodeComment comment);
	
	void onAddComment(AjaxRequestTarget target, DiffMark mark);

}
