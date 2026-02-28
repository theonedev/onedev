package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.workspace.NamedWorkspaceQuery;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;

@Editable
public class NamedWorkspaceQueriesBean extends NamedQueriesBean<NamedWorkspaceQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedWorkspaceQuery> queries = new ArrayList<>();

	@NotNull
	@Editable
	@OmitName
	public List<NamedWorkspaceQuery> getQueries() {
		return queries;
	}

	public void setQueries(List<NamedWorkspaceQuery> queries) {
		this.queries = queries;
	}

}
