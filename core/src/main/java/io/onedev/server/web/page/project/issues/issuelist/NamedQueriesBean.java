package io.onedev.server.web.page.project.issues.issuelist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.issue.NamedQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class NamedQueriesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<NamedQuery> queries = new ArrayList<>();

	@NotNull
	@Editable
	@OmitName
	public List<NamedQuery> getQueries() {
		return queries;
	}

	public void setQueries(List<NamedQuery> queries) {
		this.queries = queries;
	}

}