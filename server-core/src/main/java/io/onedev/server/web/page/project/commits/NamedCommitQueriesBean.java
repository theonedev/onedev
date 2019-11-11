package io.onedev.server.web.page.project.commits;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.NamedCommitQuery;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class NamedCommitQueriesBean extends NamedQueriesBean<NamedCommitQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedCommitQuery> queries = new ArrayList<>();

	@Override
	@NotNull
	@Editable
	@OmitName
	public List<NamedCommitQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(List<NamedCommitQuery> queries) {
		this.queries = queries;
	}

}