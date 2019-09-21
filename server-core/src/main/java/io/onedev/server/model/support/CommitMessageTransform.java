package io.onedev.server.model.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.JavaPattern;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class CommitMessageTransform implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String searchFor;
	
	private String replaceWith;

	@Editable(order=100)
	@NotEmpty
	@JavaPattern
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
