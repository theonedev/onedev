package io.onedev.server.model.support.build;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class NamedBuildQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedBuildQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedBuildQuery() {
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
	@BuildQuery(withCurrentUserCriteria=true, withUnfinishedCriteria=true)
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}