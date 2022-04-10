package io.onedev.server.model.support.pullrequest;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

public class ProjectPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries;
	
	@Nullable
	public List<NamedPullRequestQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

}
