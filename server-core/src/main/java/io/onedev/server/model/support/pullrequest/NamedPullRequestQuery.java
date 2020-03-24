package io.onedev.server.model.support.pullrequest;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.PullRequestQuery;

@Editable
public class NamedPullRequestQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedPullRequestQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedPullRequestQuery() {
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

	@Editable
	@PullRequestQuery
	@NameOfEmptyValue("All")
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}