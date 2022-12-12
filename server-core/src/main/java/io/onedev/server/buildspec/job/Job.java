package io.onedev.server.buildspec.job;

import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_TAG;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.InPipelineOf;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.apache.wicket.Component;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.job.authorization.JobAuthorization;
import io.onedev.server.job.authorization.JobAuthorization.Context;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.RetryCondition;
import io.onedev.server.web.editable.annotation.SuggestionProvider;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Job implements NamedElement, Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String SELECTION_PREFIX = "jobs/";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_JOB_DEPENDENCIES = "jobDependencies";
	
	public static final String PROP_REQUIRED_SERVICES = "requiredServices";
	
	public static final String PROP_TRIGGERS = "triggers";
	
	public static final String PROP_STEPS = "steps";
	
	public static final String PROP_RETRY_CONDITION = "retryCondition";
	
	public static final String PROP_POST_BUILD_ACTIONS = "postBuildActions";
	
	private String name;
	
	private String jobExecutor;
	
	private List<Step> steps = new ArrayList<>();
	
	private List<ParamSpec> paramSpecs = new ArrayList<>();
	
	private List<JobDependency> jobDependencies = new ArrayList<>();
	
	private List<ProjectDependency> projectDependencies = new ArrayList<>();
	
	private List<String> requiredServices = new ArrayList<>();
	
	private List<JobTrigger> triggers = new ArrayList<>();
	
	private List<CacheSpec> caches = new ArrayList<>();

	private int cpuRequirement = 250;
	
	private int memoryRequirement = 256;
	
	private long timeout = 3600;
	
	private List<PostBuildAction> postBuildActions = new ArrayList<>();
	
	private String retryCondition = "never";
	
	private int maxRetries = 3;
	
	private int retryDelay = 30;
	
	private transient Map<String, ParamSpec> paramSpecMap;
	
	@Editable(order=100, description="Specify name of the job")
	@SuggestionProvider("getNameSuggestions")
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		BuildSpec buildSpec = BuildSpec.get();
		if (buildSpec != null) {
			List<String> candidates = new ArrayList<>(buildSpec.getJobMap().keySet());
			buildSpec.getJobs().forEach(it->candidates.remove(it.getName()));
			return BuildSpec.suggestOverrides(candidates, status);
		}
		return new ArrayList<>();
	}

	@Editable(order=200, placeholder="Use Any Applicable Executor", description="Optionally specify authorized executor "
			+ "for this job. Leave empty to use first authorized executor")
	@Interpolative(literalSuggester="suggestJobExecutors", variableSuggester="suggestVariables")
	public String getJobExecutor() {
		return jobExecutor;
	}

	public void setJobExecutor(String jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobExecutors(String matchWith) {
		List<String> applicableJobExecutors = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		String jobName = (String) EditContext.get().getInputValue(PROP_NAME);
		if (jobName != null) {
			Context context = new Context(page.getProject(), page.getBlobIdent().revision, jobName);
			for (JobExecutor executor: OneDev.getInstance(SettingManager.class).getJobExecutors()) {
				if (executor.isEnabled()) {
					if (executor.getJobAuthorization() == null) {
						applicableJobExecutors.add(executor.getName());
					} else {
						if (JobAuthorization.parse(executor.getJobAuthorization()).matches(context))
							applicableJobExecutors.add(executor.getName());
					}
				}
			}
		}
		
		return SuggestionUtils.suggest(applicableJobExecutors, matchWith);
	}
	
	@Editable(order=200, description="Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>")
	public List<Step> getSteps() {
		return steps;
	}
	
	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	@Editable(order=400, name="Parameter Specs", group="Params & Triggers", description="Optionally define parameter specifications of the job")
	@Valid
	public List<ParamSpec> getParamSpecs() {
		return paramSpecs;
	}

	public void setParamSpecs(List<ParamSpec> paramSpecs) {
		this.paramSpecs = paramSpecs;
	}

	@Editable(order=500, group="Params & Triggers", description="Use triggers to run the job automatically under certain conditions")
	@Valid
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(name="Job Dependencies", order=9110, group="Dependencies & Services", description="Job dependencies determines the order and "
			+ "concurrency when run different jobs. You may also specify artifacts to retrieve from upstream jobs")
	@Valid
	public List<JobDependency> getJobDependencies() {
		return jobDependencies;
	}

	public void setJobDependencies(List<JobDependency> jobDependencies) {
		this.jobDependencies = jobDependencies;
	}

	@Editable(name="Project Dependencies", order=9112, group="Dependencies & Services", description="Use project dependency to retrieve "
			+ "artifacts from other projects")
	@Valid
	public List<ProjectDependency> getProjectDependencies() {
		return projectDependencies;
	}

	public void setProjectDependencies(List<ProjectDependency> projectDependencies) {
		this.projectDependencies = projectDependencies;
	}

	@Editable(order=9114, group="Dependencies & Services", placeholder="No required services", 
			description="Optionally specify services required by this job. "
			+ "<b class='text-warning'>NOTE:</b> Services are only supported by docker aware executors "
			+ "(server docker executor, remote docker executor, or kubernetes executor)")
	@ChoiceProvider("getServiceChoices")
	public List<String> getRequiredServices() {
		return requiredServices;
	}

	public void setRequiredServices(List<String> requiredServices) {
		this.requiredServices = requiredServices;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getServiceChoices() {
		List<String> choices = new ArrayList<>();
		Component component = ComponentContext.get().getComponent();
		BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
		if (buildSpecAware != null) {
			BuildSpec buildSpec = buildSpecAware.getBuildSpec();
			if (buildSpec != null) { 
				choices.addAll(buildSpec.getServiceMap().values().stream()
						.map(it->it.getName()).collect(Collectors.toList()));
			}
		}
		return choices;
	}

	@Editable(order=9400, group="More Settings", description="Specify condition to retry build upon failure")
	@NotEmpty
	@RetryCondition
	public String getRetryCondition() {
		return retryCondition;
	}

	public void setRetryCondition(String retryCondition) {
		this.retryCondition = retryCondition;
	}

	@Editable(order=9410, group="More Settings", description="Maximum of retries before giving up")
	@Min(value=1, message="This value should not be less than 1")
	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Editable(order=9420, group="More Settings", description="Delay for the first retry in seconds. "
			+ "Delay of subsequent retries will be calculated using an exponential back-off "
			+ "based on this delay")
	@Min(value=1, message="This value should not be less than 1")
	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	@Editable(order=10050, name="CPU Requirement", group="More Settings", description="Specify CPU requirement of the job in millis. "
			+ "1000 millis means a single CPU core")
	public int getCpuRequirement() {
		return cpuRequirement;
	}

	public void setCpuRequirement(int cpuRequirement) {
		this.cpuRequirement = cpuRequirement;
	}

	@Editable(order=10060, group="More Settings", description="Specify memory requirement of the job in mega bytes")
	public int getMemoryRequirement() {
		return memoryRequirement;
	}

	public void setMemoryRequirement(int memoryRequirement) {
		this.memoryRequirement = memoryRequirement;
	}

	@Editable(order=10100, group="More Settings", description="Cache specific paths to speed up job execution. "
			+ "For instance for Java Maven projects executed by various docker executors, you may cache folder "
			+ "<tt>/root/.m2/repository</tt> to avoid downloading dependencies for subsequent executions.<br>"
			+ "<b class='text-danger'>WARNING</b>: When using cache, malicious jobs running with same job executor "
			+ "can read or even pollute the cache intentionally using same cache key as yours. To avoid this "
			+ "issue, make sure job executor executing your job can only be used by trusted jobs via job "
			+ "authorization setting</b>")
	@Valid
	public List<CacheSpec> getCaches() {
		return caches;
	}

	public void setCaches(List<CacheSpec> caches) {
		this.caches = caches;
	}

	@Editable(order=10500, group="More Settings", description="Specify timeout in seconds")
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	@Editable(order=10600, name="Post Build Actions", group="More Settings")
	@Valid
	public List<PostBuildAction> getPostBuildActions() {
		return postBuildActions;
	}
	
	public void setPostBuildActions(List<PostBuildAction> postBuildActions) {
		this.postBuildActions = postBuildActions;
	}
	
	@Nullable
	public JobTriggerMatch getTriggerMatch(ProjectEvent event) {
		for (JobTrigger trigger: getTriggers()) {
			SubmitReason reason = trigger.matches(event, this);
			if (reason != null)
				return new JobTriggerMatch(trigger, reason);
		}
		return null;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		
		Set<String> keys = new HashSet<>();
		Set<String> paths = new HashSet<>();
		for (CacheSpec cache: caches) {
			if (!keys.add(cache.getKey())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate key (" + cache.getKey() + ")")
						.addPropertyNode("caches").addConstraintViolation();
			}
			if (!paths.add(cache.getPath())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate path (" + cache.getPath() + ")")
						.addPropertyNode("caches").addConstraintViolation();
			} 
		}

		Set<String> dependencyJobNames = new HashSet<>();
		for (JobDependency dependency: jobDependencies) {
			if (!dependencyJobNames.add(dependency.getJobName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency (" + dependency.getJobName() + ")")
						.addPropertyNode("jobDependencies").addConstraintViolation();
			} 
		}
		
		Set<String> dependencyProjectPaths = new HashSet<>();
		for (ProjectDependency dependency: projectDependencies) {
			if (!dependencyProjectPaths.add(dependency.getProjectPath())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency (" + dependency.getProjectPath() + ")")
						.addPropertyNode("projectDependencies").addConstraintViolation();
			}
		}
		
		Set<String> paramSpecNames = new HashSet<>();
		for (ParamSpec paramSpec: paramSpecs) {
			if (!paramSpecNames.add(paramSpec.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate parameter spec (" + paramSpec.getName() + ")")
						.addPropertyNode("paramSpecs").addConstraintViolation();
			} 
		}
		
		if (getRetryCondition() != null) { 
			try {
				io.onedev.server.buildspec.job.retrycondition.RetryCondition.parse(this, getRetryCondition());
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null)
					message = "Malformed retry condition";
				context.buildConstraintViolationWithTemplate(message)
						.addPropertyNode(PROP_RETRY_CONDITION)
						.addConstraintViolation();
				isValid = false;
			}
		}
		
		if (isValid) {
			for (int triggerIndex=0; triggerIndex<getTriggers().size(); triggerIndex++) {
				JobTrigger trigger = getTriggers().get(triggerIndex);
				try {
					ParamUtils.validateParams(getParamSpecs(), trigger.getParams());
				} catch (Exception e) {
					String errorMessage = String.format("Error validating job parameters (item: #%s, error message: %s)", 
							(triggerIndex+1), e.getMessage());
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_TRIGGERS)
							.addConstraintViolation();
					isValid = false;
				}
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		
		return isValid;
	}
	
	public Map<String, ParamSpec> getParamSpecMap() {
		if (paramSpecMap == null)
			paramSpecMap = ParamUtils.getParamSpecMap(paramSpecs);
		return paramSpecMap;
	}
	
	public static String getBuildQuery(ObjectId commitId, String jobName, 
			@Nullable Build pipelineOf, @Nullable String refName, @Nullable PullRequest request) {
		String query = "" 
				+ Criteria.quote(NAME_COMMIT) + " " + getRuleName(Is) + " " + Criteria.quote(commitId.name()) 
				+ " " + getRuleName(And) + " "
				+ Criteria.quote(NAME_JOB) + " " + getRuleName(Is) + " " + Criteria.quote(jobName);
		if (pipelineOf != null) 
			query = query + " " + getRuleName(And) + " " + getRuleName(InPipelineOf) + " " + Criteria.quote("#" + pipelineOf.getNumber());
		if (request != null) {
			query = query 
					+ " " + getRuleName(And) + " " 
					+ Criteria.quote(NAME_PULL_REQUEST) + " " + getRuleName(Is) + " " + Criteria.quote("#" + request.getNumber());
		}
		if (refName != null) {
			String branch = GitUtils.ref2branch(refName);
			if (branch != null) {
				query = query 
					+ " " + getRuleName(And) + " " 
					+ Criteria.quote(NAME_BRANCH) + " " + getRuleName(Is) + " " + Criteria.quote(branch);
			} 
			String tag = GitUtils.ref2tag(refName);
			if (tag != null) {
				query = query 
					+ " " + getRuleName(And) + " " 
					+ Criteria.quote(NAME_TAG) + " " + getRuleName(Is) + " " + Criteria.quote(tag);
			} 
		}
		return query;
	}
	
	public static List<String> getChoices() {
		List<String> choices = new ArrayList<>();
		Component component = ComponentContext.get().getComponent();
		BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
		if (buildSpecAware != null) {
			BuildSpec buildSpec = buildSpecAware.getBuildSpec();
			if (buildSpec != null) {
				choices.addAll(buildSpec.getJobMap().values().stream()
						.map(it->it.getName()).collect(Collectors.toList()));
			}
			JobAware jobAware = WicketUtils.findInnermost(component, JobAware.class);
			if (jobAware != null) {
				Job job = jobAware.getJob();
				if (job != null)
					choices.remove(job.getName());
			}
		}
		return choices;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
}
