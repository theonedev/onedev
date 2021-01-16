package io.onedev.server.model.support.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.model.support.build.actionauthorization.CloseMilestoneAuthorization;
import io.onedev.server.model.support.build.actionauthorization.CreateTagAuthorization;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectBuildSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> listParams;
	
	private List<NamedBuildQuery> namedQueries;
	
	private List<JobSecret> jobSecrets = new ArrayList<>();
	
	private List<BuildPreservation> buildPreservations = new ArrayList<>();
	
	private List<ActionAuthorization> actionAuthorizations = new ArrayList<>();
	
	private List<DefaultFixedIssueFilter> defaultFixedIssueFilters = new ArrayList<>();
	
	private transient GlobalBuildSetting globalSetting;
	
	public ProjectBuildSetting() {
		actionAuthorizations.add(new CloseMilestoneAuthorization());
		actionAuthorizations.add(new CreateTagAuthorization());
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

	public List<ActionAuthorization> getActionAuthorizations() {
		return actionAuthorizations;
	}

	public void setActionAuthorizations(List<ActionAuthorization> actionAuthorizations) {
		this.actionAuthorizations = actionAuthorizations;
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
	
	public boolean isActionAuthorized(Build build, PostBuildAction action) {
		List<ActionAuthorization> authorizations = getActionAuthorizations();
		for (ActionAuthorization authorization: authorizations) {
			if (authorization.matches(action) && build.isOnBranches(authorization.getAuthorizedBranches())) 
				return true;
		}
		return false;
	}
	
	@Nullable
	public String getDefaultFixedIssueQuery(String jobName) {
		Matcher matcher = new StringMatcher();
		for (DefaultFixedIssueFilter each: getDefaultFixedIssueFilters()) {
			if (PatternSet.parse(each.getJobNames()).matches(matcher, jobName))
				return each.getIssueQuery();
		}
		return null;
	}
	
	public Collection<String> getUndefinedStates(Project project) {
		Set<String> undefinedStates = new HashSet<>();

		for (DefaultFixedIssueFilter filter: defaultFixedIssueFilters) {
			try {
				undefinedStates.addAll(IssueQuery.parse(project, filter.getIssueQuery(), false, false, false, false, false).getUndefinedStates());
			} catch (Exception e) {
			}
		}
		
		return undefinedStates;
	}

	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		for (DefaultFixedIssueFilter filter: defaultFixedIssueFilters) {
			try {
				undefinedFields.addAll(IssueQuery.parse(project, filter.getIssueQuery(), false, false, false, false, false).getUndefinedFields());
			} catch (Exception e) {
			}
		}
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (DefaultFixedIssueFilter filter: defaultFixedIssueFilters) {
			try {
				undefinedFieldValues.addAll(IssueQuery.parse(project, filter.getIssueQuery(), false, false, false, false, false).getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions) {
		for (Iterator<DefaultFixedIssueFilter> it = defaultFixedIssueFilters.iterator(); it.hasNext();) {
			DefaultFixedIssueFilter filter = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(project, filter.getIssueQuery(), false, false, false, false, false);
				if (parsedQuery.fixUndefinedStates(resolutions))
					filter.setIssueQuery(parsedQuery.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}
	
	public void fixUndefinedFields(Project project, Map<String, UndefinedFieldResolution> resolutions) {
		for (Iterator<DefaultFixedIssueFilter> it = defaultFixedIssueFilters.iterator(); it.hasNext();) {
			DefaultFixedIssueFilter filter = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(project, filter.getIssueQuery(), false, false, false, false, false);
				if (parsedQuery.fixUndefinedFields(resolutions))
					filter.setIssueQuery(parsedQuery.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}	
	
	public void fixUndefinedFieldValues(Project project, Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<DefaultFixedIssueFilter> it = defaultFixedIssueFilters.iterator(); it.hasNext();) {
			DefaultFixedIssueFilter filter = it.next();
			try {
				IssueQuery parsedQuery = IssueQuery.parse(project, filter.getIssueQuery(), false, false, false, false, false);
				if (parsedQuery.fixUndefinedFieldValues(resolutions))
					filter.setIssueQuery(parsedQuery.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}	
}
