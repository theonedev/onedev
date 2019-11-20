package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.BuildSetting;
import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<NamedBuildQuery> namedQueries;
	
	private transient BuildSetting setting;
	
	private String buildsToPreserve = "all";
	
	@Editable(description="Specify builds to preserve. OneDev will run every night to remove builds not matching "
			+ "query specified here")
	@BuildQuery
	@NotEmpty
	public String getBuildsToPreserve() {
		return buildsToPreserve;
	}

	public void setBuildsToPreserve(String buildsToPreserve) {
		this.buildsToPreserve = buildsToPreserve;
	}
	
	private BuildSetting getSetting() {
		if (setting == null)
			setting = OneDev.getInstance(SettingManager.class).getBuildSetting();
		return setting;
	}

	@Nullable
	public List<NamedBuildQuery> getNamedQueries(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && namedQueries == null)
			return new ArrayList<>(getSetting().getNamedQueries());
		else
			return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedBuildQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

	@Nullable
	public NamedBuildQuery getNamedQuery(String name) {
		for (NamedBuildQuery namedQuery: getNamedQueries(true)) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
