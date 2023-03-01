package io.onedev.server.buildspec.job.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.validation.Valid;

import io.onedev.server.buildspec.job.TriggerMatch;
import org.apache.wicket.Component;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ParamSpecProvider;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.annotation.VariableOption;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
public abstract class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	public static Function<RevCommit, Boolean> SKIP_COMMIT = new Function<RevCommit, Boolean>() {

		@Override
		public Boolean apply(RevCommit commit) {
			String message = commit.getFullMessage().toLowerCase();
			return message.contains("[skip ci]") || message.contains("[ci skip]") || message.contains("[no ci]")
					|| message.contains("[skip job]") || message.contains("[job skip]") || message.contains("[no job]");
		}
		
	};
	
	private String projects;
	
	private List<ParamSupply> params = new ArrayList<>();
	
	@Editable(name="Applicable Projects", order=900, placeholder="Any project", description=""
			+ "Optionally specify space-separated projects applicable for this trigger. "
			+ "This is useful for instance when you want to prevent the job from being "
			+ "triggered in forked projects. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all projects")
	@Patterns(suggester="suggestProjects", path=true)
	public String getProjects() {
		return projects;
	}

	public void setProjects(String projects) {
		this.projects = projects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}

	@Editable(name="Job Parameters", order=1000)
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=false, withDynamicVariables=false)
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
	public TriggerMatch matches(ProjectEvent event, Job job) {
		String projectPath = event.getProject().getPath();
		Matcher matcher = new PathMatcher();
		if (projects == null || PatternSet.parse(projects).matches(matcher, projectPath)) 
			return triggerMatches(event, job);
		else 
			return null;
	}
	
	public String getDescription() {
		String description = getTriggerDescription();
		if (projects != null)
			description += " in projects '" + projects + "'";
		return description;
	}

	@Nullable
	protected abstract TriggerMatch triggerMatches(ProjectEvent event, Job job);
	
	public abstract String getTriggerDescription();
	
}
