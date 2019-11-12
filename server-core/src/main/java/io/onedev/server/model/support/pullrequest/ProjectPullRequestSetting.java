package io.onedev.server.model.support.pullrequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.PullRequestSetting;

public class ProjectPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries;
	
	private transient PullRequestSetting setting;
	
	private PullRequestSetting getSetting() {
		if (setting == null)
			setting = OneDev.getInstance(SettingManager.class).getPullRequestSetting();
		return setting;
	}

	@Nullable
	public List<NamedPullRequestQuery> getNamedQueries(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && namedQueries == null)
			return new ArrayList<>(getSetting().getNamedQueries());
		else
			return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

	@Nullable
	public NamedPullRequestQuery getNamedQuery(String name) {
		for (NamedPullRequestQuery namedQuery: getNamedQueries(true)) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

}
