package io.onedev.server.model.support.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_CACHE_PRESERVE_DAYS = 7;

	private List<String> listParams;
	
	private List<NamedBuildQuery> namedQueries;

	private List<JobProperty> jobProperties = new ArrayList<>();
	
	private List<JobSecret> jobSecrets = new ArrayList<>();
	
	private List<BuildPreservation> buildPreservations = new ArrayList<>();
	
	private List<DefaultFixedIssueFilter> defaultFixedIssueFilters = new ArrayList<>();
	
	private Integer cachePreserveDays;
	
	private transient GlobalBuildSetting globalSetting;
	
	@Valid
	public List<JobProperty> getJobProperties() {
		return jobProperties;
	}

	public void setJobProperties(List<JobProperty> jobProperties) {
		this.jobProperties = jobProperties;
	}

	@Valid
	public List<JobSecret> getJobSecrets() {
		return jobSecrets;
	}

	public void setJobSecrets(List<JobSecret> jobSecrets) {
		this.jobSecrets = jobSecrets;
	}
	
	@Valid
	public List<BuildPreservation> getBuildPreservations() {
		return buildPreservations;
	}

	public void setBuildPreservations(List<BuildPreservation> buildPreservations) {
		this.buildPreservations = buildPreservations;
	}

	@Valid
	public List<DefaultFixedIssueFilter> getDefaultFixedIssueFilters() {
		return defaultFixedIssueFilters;
	}

	public void setDefaultFixedIssueFilters(List<DefaultFixedIssueFilter> defaultFixedIssueFilters) {
		this.defaultFixedIssueFilters = defaultFixedIssueFilters;
	}

	public Integer getCachePreserveDays() {
		return cachePreserveDays;
	}

	public void setCachePreserveDays(Integer cachePreserveDays) {
		this.cachePreserveDays = cachePreserveDays;
	}

	private GlobalBuildSetting getGlobalSetting() {
		if (globalSetting == null)
			globalSetting = OneDev.getInstance(SettingService.class).getBuildSetting();
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

	@Valid
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
		
}
