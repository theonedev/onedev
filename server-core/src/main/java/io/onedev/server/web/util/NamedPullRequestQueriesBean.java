package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class NamedPullRequestQueriesBean extends NamedQueriesBean<NamedPullRequestQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedPullRequestQuery> queries = new ArrayList<>();

	@Override
	@NotNull
	@Editable
	@OmitName
	public List<NamedPullRequestQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(List<NamedPullRequestQuery> queries) {
		this.queries = queries;
	}

}