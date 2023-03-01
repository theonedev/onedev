package io.onedev.server.model.support;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.CommitQuery;
import io.onedev.server.annotation.Editable;

@Editable
public class NamedCommitQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedCommitQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedCommitQuery() {
	}

	@Editable
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(placeholder="All")
	@CommitQuery(withCurrentUserCriteria=true)
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}