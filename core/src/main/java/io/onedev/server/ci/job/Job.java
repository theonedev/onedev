package io.onedev.server.ci.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ci.JobDependency;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.ci.job.log.LogLevel;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.Script;

@Editable
@Horizontal
@ClassValidating
public class Job implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String SELECTION_PREFIX = "jobs/";
	
	private String name;
	
	private List<InputSpec> paramSpecs = new ArrayList<>();
	
	private String environment;
	
	private String commands;
	
	private boolean cloneSource = true;
	
	private List<JobDependency> dependencies = new ArrayList<>();
	
	private List<JobOutcome> outcomes = new ArrayList<>();

	private List<JobTrigger> triggers = new ArrayList<>();
	
	private List<JobCache> caches = new ArrayList<>();
	
	private long timeout = 3600;
	
	private LogLevel logLevel = LogLevel.INFO;
	
	private transient Map<String, InputSpec> paramSpecMap;
	
	@Editable(order=100, description="Specify name of the job")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=105, name="Parameter Specs", description="Define parameter specifications of the job")
	public List<InputSpec> getParamSpecs() {
		return paramSpecs;
	}

	public void setParamSpecs(List<InputSpec> paramSpecs) {
		this.paramSpecs = paramSpecs;
	}

	@Editable(order=110, description="Specify the environment to run the command. Environment will be interpretated "
			+ "by underlying job executor. For instance, a docker executor will treat it as a docker image, and an "
			+ "agent executor will treat it as labels to match agents")
	@NotEmpty
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	@Editable(order=120, description="Specify commands to execute in above environment, with one command per line. "
			+ "For Windows based environments, commands will be interpretated by PowerShell, and for Unix/Linux "
			+ "based environments, commands will be interpretated by shell")
	@Script(Script.SHELL)
	@NotEmpty
	public String getCommands() {
		return commands;
	}

	public void setCommands(String commands) {
		this.commands = commands;
	}
	
	@Editable(order=130, description="Whether or not to clone the source code. If enabled, the repository will be "
			+ "cloned into job workspace")
	public boolean isCloneSource() {
		return cloneSource;
	}

	public void setCloneSource(boolean cloneSource) {
		this.cloneSource = cloneSource;
	}	
	
	@Editable(name="Dependency Jobs", order=140, description="Job dependencies determines the order and "
			+ "concurrency when run different jobs")
	public List<JobDependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<JobDependency> dependencies) {
		this.dependencies = dependencies;
	}

	@Editable(order=200, description="Specify job outcomes")
	public List<JobOutcome> getOutcomes() {
		return outcomes;
	}

	public void setOutcomes(List<JobOutcome> outcomes) {
		this.outcomes = outcomes;
	}

	@Editable(order=500, description="Use triggers to run the job automatically under certain conditions")
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(order=10100, group="More Settings", description="Cache specific paths to speed up job execution. For instance for node.js "
			+ "projects, you may cache the <tt>node_modules</tt> folder to avoid downloading node modules for "
			+ "subsequent job executions. Note that cache is considered as a best-effort approach and your "
			+ "build script should always consider that cache might not be available")
	public List<JobCache> getCaches() {
		return caches;
	}

	public void setCaches(List<JobCache> caches) {
		this.caches = caches;
	}

	@Editable(order=10200, group="More Settings", description="Specify timeout in seconds")
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Editable(order=10300, group="More Settings")
	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public JobTrigger getMatchedTrigger(ProjectEvent event) {
		for (JobTrigger trigger: getTriggers()) {
			if (trigger.matches(event, this))
				return trigger;
		}
		return null;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> keys = new HashSet<>();
		Set<String> paths = new HashSet<>();
		
		boolean isValid = true;
		for (JobCache cache: caches) {
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
		for (JobDependency dependency: dependencies) {
			if (dependencyJobs.contains(dependency.getJobName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency: " + dependency.getJobName())
						.addPropertyNode("dependencies").addConstraintViolation();
			} else {
				dependencyJobs.add(dependency.getJobName());
			}
		}
		
		Set<String> paramSpecNames = new HashSet<>();
		for (InputSpec paramSpec: paramSpecs) {
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

	public Map<String, InputSpec> getParamSpecMap() {
		if (paramSpecMap == null)
			paramSpecMap = JobParam.getParamSpecMap(paramSpecs);
		return paramSpecMap;
	}
	
}
