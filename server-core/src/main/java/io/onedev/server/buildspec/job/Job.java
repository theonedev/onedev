package io.onedev.server.buildspec.job;

import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_TAG;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.HttpHeaders;

import org.apache.wicket.Component;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.gitcredential.DefaultCredential;
import io.onedev.server.buildspec.job.gitcredential.GitCredential;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.RetryCondition;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Job implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String SELECTION_PREFIX = "jobs/";
	
	public static final String PROP_JOB_DEPENDENCIES = "jobDependencies";
	
	public static final String PROP_TRIGGERS = "triggers";
	
	public static final String PROP_RETRY_CONDITION = "retryCondition";
	
	public static final String PROP_POST_BUILD_ACTIONS = "postBuildActions";
	
	private String name;
	
	private List<ParamSpec> paramSpecs = new ArrayList<>();
	
	private String image;
	
	private List<String> commands;

	private boolean retrieveSource = true;
	
	private Integer cloneDepth;
	
	private GitCredential cloneCredential = new DefaultCredential();

	private List<JobDependency> jobDependencies = new ArrayList<>();
	
	private List<ProjectDependency> projectDependencies = new ArrayList<>();
	
	private List<JobService> services = new ArrayList<>();
	
	private String artifacts;
	
	private List<JobReport> reports = new ArrayList<>();

	private List<JobTrigger> triggers = new ArrayList<>();
	
	private List<CacheSpec> caches = new ArrayList<>();

	private String cpuRequirement = "250m";
	
	private String memoryRequirement = "128m";
	
	private long timeout = 3600;
	
	private List<PostBuildAction> postBuildActions = new ArrayList<>();
	
	private String retryCondition = "never";
	
	private int maxRetries = 3;
	
	private int retryDelay = 30;
	
	private transient Map<String, ParamSpec> paramSpecMap;
	
	@Editable(order=100, description="Specify name of the job")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=110, description="Specify docker image of the job")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public static List<InputSuggestion> suggestVariables(String matchWith) {
		Component component = ComponentContext.get().getComponent();
		List<InputSuggestion> suggestions = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
		if (buildSpecAware != null) {
			BuildSpec buildSpec = buildSpecAware.getBuildSpec();
			if (buildSpec != null) {
				JobAware jobAware = WicketUtils.findInnermost(component, JobAware.class);
				if (jobAware != null) {
					Job job = jobAware.getJob();
					if (job != null) {
						RevCommit commit;
						if (page.getBlobIdent().revision != null)
							commit = page.getCommit();
						else
							commit = null;
						suggestions.addAll(SuggestionUtils.suggestVariables(
								page.getProject(), commit, buildSpec, job, matchWith));
					}
				}
			}
		}
		return suggestions;
	}
	
	@Editable(order=120, name="Commands", description="Specify content of Linux shell script or Windows command batch to execute in above image. "
			+ "It will be executed under <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>, which may contain files of your repository and "
			+ "dependency artifacts based on your configuration below")
	@Interpolative
	@Code(language = Code.SHELL, variableProvider="getVariables")
	@Size(min=1, message="may not be empty")
	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getVariables() {
		List<String> variables = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		Project project = page.getProject();
		ObjectId commitId = page.getCommit();
		BuildSpec buildSpec = ComponentContext.get().getComponent().findParent(BuildSpecAware.class).getBuildSpec();
		Job job = ComponentContext.get().getComponent().findParent(JobAware.class).getJob();
		for (InputSuggestion suggestion: SuggestionUtils.suggestVariables(project, commitId, buildSpec, job, ""))  
			variables.add(suggestion.getContent());
		return variables;
	}
	
	@Editable(order=130, name="Parameter Specs", description="Optionally define parameter specifications of the job")
	@Valid
	public List<ParamSpec> getParamSpecs() {
		return paramSpecs;
	}

	public void setParamSpecs(List<ParamSpec> paramSpecs) {
		this.paramSpecs = paramSpecs;
	}

	@Editable(order=500, description="Use triggers to run the job automatically under certain conditions")
	@Valid
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(order=9000, group="Source Retrieval", description="Whether or not to retrieve files under the repository "
			+ "into <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>")
	public boolean isRetrieveSource() {
		return retrieveSource;
	}

	public void setRetrieveSource(boolean retrieveSource) {
		this.retrieveSource = retrieveSource;
	}
	
	@Editable(order=9050, group="Source Retrieval", description="Optionally specify depth for a shallow clone in order "
			+ "to speed up source retrieval")
	@ShowCondition("isRetrieveSourceEnabled")
	public Integer getCloneDepth() {
		return cloneDepth;
	}

	public void setCloneDepth(Integer cloneDepth) {
		this.cloneDepth = cloneDepth;
	}

	@Editable(order=9060, group="Source Retrieval", description="By default code is cloned via an auto-generated credential, "
			+ "which only has read permission over current project. In case the job needs to <a href='$docRoot/pages/push-in-job.md' target='_blank'>push code to server</a>, or want "
			+ "to <a href='$docRoot/pages/clone-submodules-via-ssh.md' target='_blank'>clone private submodules</a>, you should supply custom credential with appropriate permissions here")
	@ShowCondition("isRetrieveSourceEnabled")
	@NotNull
	public GitCredential getCloneCredential() {
		return cloneCredential;
	}

	public void setCloneCredential(GitCredential cloneCredential) {
		this.cloneCredential = cloneCredential;
	}

	@SuppressWarnings("unused")
	private static boolean isRetrieveSourceEnabled() {
		return (boolean) EditContext.get().getInputValue("retrieveSource");
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

	@Editable(order=9114, group="Dependencies & Services", description="Optionally define services used by this job")
	@Valid
	public List<JobService> getServices() {
		return services;
	}

	public void setServices(List<JobService> services) {
		this.services = services;
	}

	@Editable(order=9115, group="Artifacts & Reports", description="Optionally specify files to publish as job artifacts. "
			+ "Artifact files are relative to <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>, and may use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NameOfEmptyValue("No artifacts")
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@Editable(order=9120, group="Artifacts & Reports", description="Add job reports here")
	@Valid
	public List<JobReport> getReports() {
		return reports;
	}

	public void setReports(List<JobReport> reports) {
		this.reports = reports;
	}

	@Editable(order=9400, group="Retry Upon Failure", description="Specify condition to retry build upon failure")
	@NotEmpty
	@RetryCondition
	public String getRetryCondition() {
		return retryCondition;
	}

	public void setRetryCondition(String retryCondition) {
		this.retryCondition = retryCondition;
	}

	@Editable(order=9410, group="Retry Upon Failure", description="Maximum of retries before giving up")
	@Min(value=1, message="This value should not be less than 1")
	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Editable(order=9420, group="Retry Upon Failure", description="Delay for the first retry in seconds. "
			+ "Delay of subsequent retries will be calculated using an exponential back-off "
			+ "based on this delay")
	@Min(value=1, message="This value should not be less than 1")
	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	@Editable(order=9200, name="CPU Requirement", group="Resource Requirements", description="Specify CPU requirement of the job. "
			+ "Refer to <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-cpu' target='_blank'>kubernetes documentation</a> for details")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getCpuRequirement() {
		return cpuRequirement;
	}

	public void setCpuRequirement(String cpuRequirement) {
		this.cpuRequirement = cpuRequirement;
	}

	@Editable(order=9300, group="Resource Requirements", description="Specify memory requirement of the job. "
			+ "Refer to <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-memory' target='_blank'>kubernetes documentation</a> for details")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getMemoryRequirement() {
		return memoryRequirement;
	}

	public void setMemoryRequirement(String memoryRequirement) {
		this.memoryRequirement = memoryRequirement;
	}

	@Editable(order=10100, group="More Settings", description="Cache specific paths to speed up job execution. "
			+ "For instance for node.js projects, you may cache folder <tt>/root/.npm</tt> to avoid downloading "
			+ "node modules for subsequent job executions")
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
		Set<String> keys = new HashSet<>();
		Set<String> paths = new HashSet<>();
		
		boolean isValid = true;
		for (CacheSpec cache: caches) {
			if (keys.contains(cache.getKey())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate key: " + cache.getKey())
						.addPropertyNode("caches").addConstraintViolation();
			} else {
				keys.add(cache.getKey());
			}
			if (paths.contains(cache.getPath())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate path: " + cache.getPath())
						.addPropertyNode("caches").addConstraintViolation();
			} else {
				paths.add(cache.getPath());
			}
		}

		Set<String> dependencyJobs = new HashSet<>();
		for (JobDependency dependency: jobDependencies) {
			if (dependencyJobs.contains(dependency.getJobName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency: " + dependency.getJobName())
						.addPropertyNode("dependencies").addConstraintViolation();
			} else {
				dependencyJobs.add(dependency.getJobName());
			}
		}
		
		Set<String> paramSpecNames = new HashSet<>();
		for (ParamSpec paramSpec: paramSpecs) {
			if (paramSpecNames.contains(paramSpec.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate parameter spec: " + paramSpec.getName())
						.addPropertyNode("paramSpecs").addConstraintViolation();
			} else {
				paramSpecNames.add(paramSpec.getName());
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		
		return isValid;
	}
	
	public Map<String, ParamSpec> getParamSpecMap() {
		if (paramSpecMap == null)
			paramSpecMap = ParamSupply.getParamSpecMap(paramSpecs);
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
				for (Job eachJob: buildSpec.getJobs()) {
					if (eachJob.getName() != null)
						choices.add(eachJob.getName());
				}
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

	@Nullable
	public static String getToken(HttpServletRequest request) {
		String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (bearer != null && bearer.startsWith(KubernetesHelper.BEARER + " "))
			return bearer.substring(KubernetesHelper.BEARER.length() + 1);
		else
			return null;
	}
	
}
