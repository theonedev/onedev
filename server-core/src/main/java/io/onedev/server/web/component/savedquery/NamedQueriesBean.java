package io.onedev.server.web.component.savedquery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.support.NamedQuery;

public class NamedQueriesBean<T extends NamedQuery> implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<T> queries = new ArrayList<>();

	public List<T> getQueries() {
		return queries;
	}

	public void setQueries(List<T> queries) {
		this.queries = queries;
	}

}