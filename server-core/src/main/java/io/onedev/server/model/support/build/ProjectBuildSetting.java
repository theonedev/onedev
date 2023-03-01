package io.onedev.server.model.support.build;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.annotation.Editable;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> listParams;
	
	private List<NamedBuildQuery> namedQueries;

	private List<JobProperty> jobProperties = new ArrayList<>();
	
	private List<JobSecret> jobSecrets = new ArrayList<>();
	
	private List<BuildPreservation> buildPreservations = new ArrayList<>();
	
	private List<DefaultFixedIssueFilter> defaultFixedIssueFilters = new ArrayList<>();
	
	private transient GlobalBuildSetting globalSetting;
	
	public List<JobProperty> getJobProperties() {
		return jobProperties;
	}

	public void setJobProperties(List<JobProperty> jobProperties) {
		this.jobProperties = jobProperties;
	}

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

	public List<DefaultFixedIssueFilter> getDefaultFixedIssueFilters() {
		return defaultFixedIssueFilters;
	}

	public void setDefaultFixedIssueFilters(List<DefaultFixedIssueFilter> defaultFixedIssueFilters) {
		this.defaultFixedIssueFilters = defaultFixedIssueFilters;
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
	public List<NamedBuildQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedBuildQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

	private IssueQueryUpdater getQueryUpdater(DefaultFixedIssueFilter filter, int index) {
		return new IssueQueryUpdater() {

			@Override
			protected Usage getUsage() {
				return new Usage().add("issue query").prefix("default fixed issue filter #" + index);
			}

			@Override
			protected boolean isAllowEmpty() {
				return false;
			}

			@Override
			protected String getIssueQuery() {
				return filter.getIssueQuery();
			}

			@Override
			protected void setIssueQuery(String issueQuery) {
				filter.setIssueQuery(issueQuery);
			}
			
		};
	}
	
	public Collection<String> getUndefinedStates() {
		Set<String> undefinedStates = new HashSet<>();
		
		int index =1;
		for (DefaultFixedIssueFilter filter: defaultFixedIssueFilters)
			undefinedStates.addAll(getQueryUpdater(filter, index++).getUndefinedStates());
		
		return undefinedStates;
	}

	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		int index = 1;
		for (DefaultFixedIssueFilter filter: defaultFixedIssueFilters)
			undefinedFields.addAll(getQueryUpdater(filter, index++).getUndefinedFields());
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		int index = 1;
		for (DefaultFixedIssueFilter filter: defaultFixedIssueFilters) 
			undefinedFieldValues.addAll(getQueryUpdater(filter, index++).getUndefinedFieldValues());
		return undefinedFieldValues;
	}
	
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		int index = 1;
		for (Iterator<DefaultFixedIssueFilter> it = defaultFixedIssueFilters.iterator(); it.hasNext();) {
			if (!getQueryUpdater(it.next(), index++).fixUndefinedStates(resolutions))
				it.remove();
		}
	}
	
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		int index = 1;
		for (Iterator<DefaultFixedIssueFilter> it = defaultFixedIssueFilters.iterator(); it.hasNext();) {
			if (!getQueryUpdater(it.next(), index++).fixUndefinedFields(resolutions))
				it.remove();
		}
	}	
	
	public void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		int index = 1;
		for (Iterator<DefaultFixedIssueFilter> it = defaultFixedIssueFilters.iterator(); it.hasNext();) {
			if (!getQueryUpdater(it.next(), index++).fixUndefinedFieldValues(resolutions))
				it.remove();
		}
	}	
	
	public void onRenameGroup(String oldName, String newName) {
		for (var jobSecret: getJobSecrets()) {
			if (jobSecret.getAuthorization() != null) {
				JobMatch jobMatch = JobMatch.parse(jobSecret.getAuthorization(), false, false);
				jobMatch.onRenameGroup(oldName, newName);
				jobSecret.setAuthorization(jobMatch.toString());
			}
		}
	}

	public Usage onDeleteGroup(String name) {
		Usage usage = new Usage();
		for (var jobSecret: getJobSecrets()) {
			if (jobSecret.getAuthorization() != null) {
				JobMatch jobMatch = JobMatch.parse(jobSecret.getAuthorization(), false, false);
				if (jobMatch.isUsingGroup(name))
					usage.add("job secret '" + jobSecret.getName() + "': authorization");
			}
		}
		return usage.prefix("build");
	}

	public void onRenameUser(String oldName, String newName) {
		for (var jobSecret: getJobSecrets()) {
			if (jobSecret.getAuthorization() != null) {
				JobMatch jobMatch = JobMatch.parse(jobSecret.getAuthorization(), false, false);
				jobMatch.onRenameUser(oldName, newName);
				jobSecret.setAuthorization(jobMatch.toString());
			}
		}
	}

	public Usage onDeleteUser(String name) {
		Usage usage = new Usage();
		for (var jobSecret: getJobSecrets()) {
			if (jobSecret.getAuthorization() != null) {
				JobMatch jobMatch = JobMatch.parse(jobSecret.getAuthorization(), false, false);
				if (jobMatch.isUsingUser(name))
					usage.add("job secret '" + jobSecret.getName() + "': authorization");
			}
		}
		return usage.prefix("build");
	}
	
}
