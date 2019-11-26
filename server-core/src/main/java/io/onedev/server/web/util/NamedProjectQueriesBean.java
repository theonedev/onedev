package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.NamedProjectQuery;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class NamedProjectQueriesBean extends NamedQueriesBean<NamedProjectQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedProjectQuery> queries = new ArrayList<>();

	@NotNull
	@Editable
	@OmitName
	public List<NamedProjectQuery> getQueries() {
		return queries;
	}

	public void setQueries(List<NamedProjectQuery> queries) {
		this.queries = queries;
	}

}