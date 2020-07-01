package io.onedev.server.buildspec.job.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;

import org.apache.wicket.Component;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
public abstract class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projects;
	
	private List<ParamSupply> params = new ArrayList<>();

	@Editable(name="Applicable Projects", order=900, description="Optionally specify space-separated projects "
			+ "applicable for this trigger. This is useful for instance when you want to prevent "
			+ "the job from being triggered in forked projects. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude. Leave empty to match all projects")
	@Patterns(suggester="suggestProjects")
	@NameOfEmptyValue("Any project")
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjects(matchWith);
	}

	@Editable(name="Job Parameters", order=1000)
	@ParamSpecProvider("getParamSpecs")
	@OmitName
	@Valid
	public List<ParamSupply> getParams() {
		return params;
	}

	public void setParams(List<ParamSupply> params) {
		this.params = params;
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		Component component = ComponentContext.get().getComponent();
		JobAware jobAware = WicketUtils.findInnermost(component, JobAware.class);
		if (jobAware != null) {
			Job job = jobAware.getJob();
			if (job != null)
				return job.getParamSpecs();
		}
		return new ArrayList<>();
	}
	
	@Nullable
	public SubmitReason matches(ProjectEvent event, Job job) {
		String projectName = event.getProject().getName();
		Matcher matcher = new StringMatcher();
		if (projects == null || PatternSet.parse(projects).matches(matcher, projectName)) 
			return matchesWithoutProject(event, job);
		else 
			return null;
	}
	
	public String getDescription() {
		String description = getDescriptionWithoutProject();
		if (projects != null)
			description += " in projects '" + projects + "'";
		return description;
	}

	@Nullable
	public abstract SubmitReason matchesWithoutProject(ProjectEvent event, Job job);
	
	public abstract String getDescriptionWithoutProject();
}
