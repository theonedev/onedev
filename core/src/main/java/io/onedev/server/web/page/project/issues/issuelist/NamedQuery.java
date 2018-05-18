package io.onedev.server.web.page.project.issues.issuelist;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.Editable;

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
	@NotEmpty
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
}