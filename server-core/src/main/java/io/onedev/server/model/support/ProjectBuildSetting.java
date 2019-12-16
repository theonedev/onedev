package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> listParams;
	
	private List<NamedBuildQuery> namedQueries;
	
	private List<JobSecret> secrets = new ArrayList<>();
	
	private List<BuildPreservation> buildPreservations = new ArrayList<>();
	
	private transient GlobalBuildSetting setting;
	
	public List<JobSecret> getSecrets() {
		return secrets;
	}

	public void setSecrets(List<JobSecret> secrets) {
		this.secrets = secrets;
	}
	
	public List<BuildPreservation> getBuildPreservations() {
		return buildPreservations;
	}

	public void setBuildPreservations(List<BuildPreservation> buildPreservations) {
		this.buildPreservations = buildPreservations;
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
	
	public List<JobSecret> getInheritedSecrets(Project project) {
		Map<String, JobSecret> inheritedSecrets = new LinkedHashMap<>();
		for (JobSecret secret: project.getOwner().getBuildSetting().getSecrets())
			inheritedSecrets.put(secret.getName(), secret);
		inheritedSecrets.keySet().removeAll(secrets.stream().map(it->it.getName()).collect(Collectors.toSet()));
		return new ArrayList<>(inheritedSecrets.values());
	}
	
	public List<JobSecret> getHierarchySecrets(Project project) {
		List<JobSecret> hierarchySecrets = new ArrayList<>(getSecrets());
		hierarchySecrets.addAll(getInheritedSecrets(project));
		return hierarchySecrets;
	}
	
	public String getSecretValue(Project project, String secretName, ObjectId commitId) {
		for (JobSecret secret: getHierarchySecrets(project)) {
			if (secret.getName().equals(secretName)) {
				if (secret.isAuthorized(project, commitId))				
					return secret.getValue();
				else
					throw new OneException("Job secret not authorized: " + secretName);
			}
		}
		throw new OneException("No job secret found: " + secretName);
	}
	
	public List<BuildPreservation> getHierarchyBuildPreservations(Project project) {
		List<BuildPreservation> hierarchyBuildPreservations = new ArrayList<>(getBuildPreservations());
		hierarchyBuildPreservations.addAll(project.getOwner().getBuildSetting().getBuildPreservations());
		return hierarchyBuildPreservations;
	}
	
}
