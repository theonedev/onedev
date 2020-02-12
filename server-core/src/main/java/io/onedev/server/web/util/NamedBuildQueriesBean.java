package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.build.NamedBuildQuery;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class NamedBuildQueriesBean extends NamedQueriesBean<NamedBuildQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedBuildQuery> queries = new ArrayList<>();

	@NotNull
	@Editable
	@OmitName
	public List<NamedBuildQuery> getQueries() {
		return queries;
	}

	public void setQueries(List<NamedBuildQuery> queries) {
		this.queries = queries;
	}

}