package io.onedev.server.buildspec.job;

import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_TAG;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
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
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.RetryCondition;
import io.onedev.server.annotation.SuggestionProvider;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Job implements NamedElement, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String SELECTION_PREFIX = "jobs/";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_JOB_DEPENDENCIES = "jobDependencies";
	
	public static final String PROP_REQUIRED_SERVICES = "requiredServices";
	
	public static final String PROP_TRIGGERS = "triggers";
	
	public static final String PROP_STEPS = "steps";
	
	public static final String PROP_RETRY_CONDITION = "retryCondition";
	
	public static final String PROP_POST_BUILD_ACTIONS = "postBuildActions";

	public static final String PROP_JOB_EXECUTOR = "jobExecutor";
	
	private String name;
	
	private String jobExecutor;
	
	private List<Step> steps = new ArrayList<>();
	
	private List<ParamSpec> paramSpecs = new ArrayList<>();
	
	private List<JobDependency> jobDependencies = new ArrayList<>();
	
	private List<ProjectDependency> projectDependencies = new ArrayList<>();
	
	private List<String> requiredServices = new ArrayList<>();
	
	private List<JobTrigger> triggers = new ArrayList<>();

	private long timeout = 14400;
	
	private List<PostBuildAction> postBuildActions = new ArrayList<>();
	
	private String sequentialGroup;
	
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

	@Editable(order=200, placeholderProvider="getJobExecutorPlaceholder", descriptionProvider="getJobExecutorDescription")
	@Interpolative(literalSuggester="suggestJobExecutors", variableSuggester="suggestVariables")
	public String getJobExecutor() {
		return jobExecutor;
	}

	public void setJobExecutor(String jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	@SuppressWarnings("unused")
	private static String getJobExecutorPlaceholder() {
		if (OneDev.getInstance(SettingService.class).getJobExecutors().isEmpty())
			return _T("Auto-discovered executor");
		else 
			return _T("First applicable executor");
	}

	@SuppressWarnings("unused")
	private static String getJobExecutorDescription() {
		if (OneDev.getInstance(SettingService.class).getJobExecutors().isEmpty())
			return _T("Optionally specify executor for this job. Leave empty to use auto-discover executor");
		else 
			return _T("Optionally specify executor for this job. Leave empty to use first applicable executor");
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobExecutors(String matchWith) {
		List<String> applicableJobExecutors = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		String jobName = (String) EditContext.get().getInputValue(PROP_NAME);
		if (jobName != null) {
			String branch = page.getBlobIdent().revision;
			if (branch == null)
				branch = "main";
			JobMatchContext context = new JobMatchContext(page.getProject(), branch, null, jobName);
			for (JobExecutor executor: OneDev.getInstance(SettingService.class).getJobExecutors()) {
				if (executor.isEnabled()) {
					if (executor.getJobMatch() == null) {
						applicableJobExecutors.add(executor.getName());
					} else {
						if (JobMatch.parse(executor.getJobMatch(), true, true).matches(context))
							applicableJobExecutors.add(executor.getName());
					}
				}
			}
		}
		
		return SuggestionUtils.suggest(applicableJobExecutors, matchWith);
	}
	
	@Editable(order=200, description="Steps will be executed serially on same node, sharing the same <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>")
	@Valid
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

	@Editable(order=9300, group="More Settings", description = "Jobs with same sequential group and executor will be " +
			"executed sequentially. For instance you may specify this property as <tt>@project_path@:prod</tt> for " +
			"jobs executing by same executor and deploying to prod environment of current project to avoid " +
			"conflicting deployments")
	@Interpolative(variableSuggester="suggestVariables")
	public String getSequentialGroup() {
		return sequentialGroup;
	}

	public void setSequentialGroup(String sequentialGroup) {
		this.sequentialGroup = sequentialGroup;
	}
	
	@Editable(order=9400, group="More Settings", description="Specify condition to retry build upon failure")
	@RetryCondition
	@NotEmpty
	public String getRetryCondition() {
		return retryCondition;
	}

	public void setRetryCondition(String retryCondition) {
		this.retryCondition = retryCondition;
	}

	@Editable(order=9410, group="More Settings", description="Maximum of retries before giving up")
	@Min(value=1, message="This value should not be less than 1")
	@DependsOn(property="retryCondition", value = "never", inverse = true)
	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Editable(order=9420, group="More Settings", description="Delay for the first retry in seconds. " +
			"Delay of subsequent retries will be calculated using an exponential back-off based on " +
			"this value")
	@Min(value=1, message="This value should not be less than 1")
	@DependsOn(property="retryCondition", value = "never", inverse = true)
	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	@Editable(order=10500, group="More Settings", description="Specify timeout in seconds. It counts from " +
			"the time when job is submitted")
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
	public TriggerMatch getTriggerMatch(ProjectEvent event) {
		for (JobTrigger trigger: getTriggers()) {
			TriggerMatch match = trigger.matches(event, this);
			if (match != null)
				return match;
		}
		return null;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		
		var jobExecutors = OneDev.getInstance(SettingService.class).getJobExecutors();
		if (jobExecutor != null && !jobExecutor.contains("@") 
				&& jobExecutors.stream().noneMatch(it->it.getName().equals(jobExecutor))) {
			isValid = false;
			context.buildConstraintViolationWithTemplate("Job executor not found: " + jobExecutor)
					.addPropertyNode(PROP_JOB_EXECUTOR).addConstraintViolation();
		}
		
		Set<String> dependencyJobNames = new HashSet<>();
		for (JobDependency dependency: jobDependencies) {
			if (!dependencyJobNames.add(dependency.getJobName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency (" + dependency.getJobName() + ")")
						.addPropertyNode("jobDependencies").addConstraintViolation();
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
		
		if (isValid) {
			for (int triggerIndex=0; triggerIndex<getTriggers().size(); triggerIndex++) {
				JobTrigger trigger = getTriggers().get(triggerIndex);
				try {
					ParamUtils.validateParamMatrix(getParamSpecs(), trigger.getParamMatrix());
					for (var paramMap: trigger.getExcludeParamMaps())
						ParamUtils.validateParamMap(getParamSpecs(), paramMap.getParams());
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
			@Nullable String refName, @Nullable PullRequest request) {
		String query = "" 
				+ Criteria.quote(NAME_COMMIT) + " " + getRuleName(Is) + " " + Criteria.quote(commitId.name()) 
				+ " " + getRuleName(And) + " "
				+ Criteria.quote(NAME_JOB) + " " + getRuleName(Is) + " " + Criteria.quote(jobName);
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
