package com.gitplex.core.entity.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable
public class CommitMessageTransformSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String searchFor;
	
	private String replaceWith;

	@Editable(order=100, description="Specify <a href='https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html'>Java pattern</a> "
			+ "to match in the commit message, for instance, you may match JIRA issue key with <tt>([A-Z]{2,}-\\d+)</tt>")
	@NotEmpty
	public String getSearchFor() {
		return searchFor;
	}

	public void setSearchFor(String searchFor) {
		this.searchFor = searchFor;
	}

	@Editable(order=200, description="Specify replacement for above matches, for the example "
			+ "issue key pattern above, you may use <tt>&lt;a href='http://track.example.com/browse/$1'&gt;$1&lt;/a&gt;</tt> to "
			+ "replace issue keys with issue links. Here <tt>$n</tt> represents nth capturing group in <tt>search for</tt> "
			+ "pattern")
	@NotEmpty
	public String getReplaceWith() {
		return replaceWith;
	}

	public void setReplaceWith(String replaceWith) {
		this.replaceWith = replaceWith;
	}
	
}
