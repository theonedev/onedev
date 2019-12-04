package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> listParams;
	
	private List<NamedBuildQuery> namedQueries;
	
	private transient GlobalBuildSetting setting;
	
	private String buildsToPreserve = "all";
	
	@Editable(description="Specify builds to preserve. OneDev will run every night to remove builds not matching "
			+ "query specified here")
	@BuildQuery(withCurrentUserCriteria = false)
	@NotEmpty
	public String getBuildsToPreserve() {
		return buildsToPreserve;
	}

	public void setBuildsToPreserve(String buildsToPreserve) {
		this.buildsToPreserve = buildsToPreserve;
	}
	
	private GlobalBuildSetting getGlobalSetting() {
		if (setting == null)
			setting = OneDev.getInstance(SettingManager.class).getBuildSetting();
		return setting;
	}

	@Nullable
	public List<String> getListParams(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && listParams == null)
			return new ArrayList<>(getGlobalSetting().getListParams());
		else
			return listParams;
	}
	
	public void setListParams(@Nullable List<String> listParams) {
		this.listParams = listParams;
	}

	@Nullable
	public List<NamedBuildQuery> getNamedQueries(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && namedQueries == null)
			return new ArrayList<>(getGlobalSetting().getNamedQueries());
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
