package io.onedev.server.model.support.pack;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.PackQuery;
import io.onedev.server.model.support.NamedQuery;

import javax.validation.constraints.NotEmpty;

@Editable
public class NamedPackQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedPackQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedPackQuery() {
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
	@PackQuery(withCurrentUserCriteria = true)
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}