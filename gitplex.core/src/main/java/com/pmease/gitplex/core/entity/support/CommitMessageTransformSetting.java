package com.pmease.gitplex.core.entity.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;

@Editable
public class CommitMessageTransformSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String searchFor;
	
	private String replaceWith;

	@Editable(order=100)
	@NotEmpty
	public String getSearchFor() {
		return searchFor;
	}

	public void setSearchFor(String searchFor) {
		this.searchFor = searchFor;
	}

	@Editable(order=200)
	@NotEmpty
	public String getReplaceWith() {
		return replaceWith;
	}

	public void setReplaceWith(String replaceWith) {
		this.replaceWith = replaceWith;
	}
	
}
