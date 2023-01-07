package io.onedev.server.model.support.pullrequest;

import io.onedev.server.web.editable.annotation.Editable;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

@Editable
public class ProjectPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries;
	
	private MergeStrategy defaultMergeStrategy;
	
	@Nullable
	public List<NamedPullRequestQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

	@Editable(placeholder = "Inherit from parent", rootPlaceholder = "Create merge commit", 
			description = "Specify default merge strategy of pull requests submitted to this project")
	public MergeStrategy getDefaultMergeStrategy() {
		return defaultMergeStrategy;
	}

	public void setDefaultMergeStrategy(MergeStrategy defaultMergeStrategy) {
		this.defaultMergeStrategy = defaultMergeStrategy;
	}
}
