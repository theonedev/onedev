package io.onedev.server.model.support.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> listParams;
	
	private List<NamedBuildQuery> namedQueries;
	
	private List<JobSecret> jobSecrets = new ArrayList<>();
	
	private List<BuildPreservation> buildPreservations = new ArrayList<>();
	
	private List<ActionAuthorization> actionAuthorizations = new ArrayList<>();
	
	private transient GlobalBuildSetting globalSetting;
	
	public List<JobSecret> getJobSecrets() {
		return jobSecrets;
	}

	public void setJobSecrets(List<JobSecret> jobSecrets) {
		this.jobSecrets = jobSecrets;
	}
	
	public List<BuildPreservation> getBuildPreservations() {
		return buildPreservations;
	}

	public void setBuildPreservations(List<BuildPreservation> buildPreservations) {
		this.buildPreservations = buildPreservations;
	}

	public List<ActionAuthorization> getActionAuthorizations() {
		return actionAuthorizations;
	}

	public void setActionAuthorizations(List<ActionAuthorization> actionAuthorizations) {
		this.actionAuthorizations = actionAuthorizations;
	}

	private GlobalBuildSetting getGlobalSetting() {
		if (globalSetting == null)
			globalSetting = OneDev.getInstance(SettingManager.class).getBuildSetting();
		return globalSetting;
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
	
	public String getSecretValue(Project project, String secretName, ObjectId commitId) {
		for (JobSecret secret: getJobSecrets()) {
			if (secret.getName().equals(secretName)) {
				if (secret.isAuthorized(project, commitId))				
					return secret.getValue();
				else
					throw new OneException("Job secret not authorized: " + secretName);
			}
		}
		throw new OneException("No job secret found: " + secretName);
	}
	
	public boolean isActionAuthorized(Build build, PostBuildAction action) {
		List<ActionAuthorization> authorizations = getActionAuthorizations();
		if (!authorizations.isEmpty()) {
			for (ActionAuthorization authorization: authorizations) {
				if (authorization.isAuthorized(build, action)) 
					return true;
			}
			return false;
		} else {
			return true;
		}
	}
	
}
