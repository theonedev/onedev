package io.onedev.server.model.support.issue;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;

@Editable
public class NamedIssueQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedIssueQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedIssueQuery() {
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
	@IssueQuery(withCurrentUserCriteria = true, withCurrentProjectCriteria = true)
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}