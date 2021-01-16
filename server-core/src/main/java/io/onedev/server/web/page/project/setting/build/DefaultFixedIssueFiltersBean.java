package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.support.build.DefaultFixedIssueFilter;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class DefaultFixedIssueFiltersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<DefaultFixedIssueFilter> defaultFixedIssueFilters = new ArrayList<>();

	@Editable
	public List<DefaultFixedIssueFilter> getDefaultFixedIssueFilters() {
		return defaultFixedIssueFilters;
	}

	public void setDefaultFixedIssueFilters(List<DefaultFixedIssueFilter> defaultFixedIssueFilters) {
		this.defaultFixedIssueFilters = defaultFixedIssueFilters;
	}
	
}
