package io.onedev.server.model.support.issue;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;

@Editable
public class NamedQuery implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedQuery() {
	}

	@Editable
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable
	@IssueQuery
	@NotEmpty
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
}