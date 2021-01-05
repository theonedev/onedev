package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;

public class GlobalPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries = new ArrayList<>();
	
	public GlobalPullRequestSetting() {
		namedQueries.add(new NamedPullRequestQuery("Open", "open"));
		namedQueries.add(new NamedPullRequestQuery("To be reviewed by me", "to be reviewed by me"));
		namedQueries.add(new NamedPullRequestQuery("To be changed by me", "submitted by me and someone requested for changes"));
		namedQueries.add(new NamedPullRequestQuery("Requested for changes by me", "requested for changes by me"));
		namedQueries.add(new NamedPullRequestQuery("Approved by me", "approved by me"));
		namedQueries.add(new NamedPullRequestQuery("Submitted by me", "submitted by me"));
		namedQueries.add(new NamedPullRequestQuery("Submitted recently", "\"Submit Date\" is since \"last week\""));
		namedQueries.add(new NamedPullRequestQuery("Updated recently", "\"Update Date\" is since \"last week\""));
		namedQueries.add(new NamedPullRequestQuery("Closed", "merged or discarded"));
		namedQueries.add(new NamedPullRequestQuery("All", null));
	}
	
	public List<NamedPullRequestQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedPullRequestQuery getNamedQuery(String name) {
		for (NamedPullRequestQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
