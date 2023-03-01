package io.onedev.server.model.support;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectQuery;

@Editable
public class NamedProjectQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedProjectQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedProjectQuery() {
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
	@ProjectQuery
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}