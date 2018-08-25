package io.onedev.server.model.support.codecomment;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.web.editable.annotation.CodeCommentQuery;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class NamedCodeCommentQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedCodeCommentQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedCodeCommentQuery() {
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
	@CodeCommentQuery
	@NotEmpty
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
}