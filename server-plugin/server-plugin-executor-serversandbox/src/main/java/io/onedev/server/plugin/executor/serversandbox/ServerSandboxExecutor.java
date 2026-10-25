package io.onedev.server.plugin.executor.serversandbox;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;

import io.onedev.agent.SandboxSettings;
import io.onedev.agent.job.SandboxJobController;
import io.onedev.agent.job.SandboxJobData;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobRunnable;
import io.onedev.server.job.JobService;
import io.onedev.server.job.JobTerminal;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.SandboxAware;
import io.onedev.server.service.ResourceService;
import io.onedev.server.service.SettingService;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.util.Testable;

@Editable(order=ServerSandboxExecutor.ORDER, name="Server Sandbox Executor", description="Runs shell and Docker steps in a fresh Docker Sandbox on the server. "
		+ "Job services run inside the sandbox and are accessible by service name from shell and container steps. "
		+ "The sandbox and its services are deleted after each job. "
		+ "See <a href='${docsite}/tutorials/sandbox/working-with-sandbox' target='_blank'>Working with Sandbox</a> "
		+ "for host setup, login, and security practices. "
		+ "<b class='text-warning'>WARNING:</b> For better security, use a Remote Sandbox Executor "
		+ "on a dedicated agent host, especially for untrusted jobs.")
@ClassValidating
public class ServerSandboxExecutor extends JobExecutor implements SandboxAware, Testable<ServerSandboxExecutor.TestData>, Validatable {

	@Serial
	private static final long serialVersionUID = 1L;

	static final int ORDER = 350;

	private String sandboxExecutable = "sbx";

	private boolean alwaysPullTemplateImage = true;

	private Integer cpus;

	private String memory;

	private Integer concurrency;

	private boolean alwaysPullContainerImage = true;

	private String runOptions;

	private String networkOptions;

	private List<RegistryLogin> registryLogins = new ArrayList<>();

	@Editable(order=500, group="Container Settings", description="Whether or not to always pull images when running containers or building images "
			+ "inside the sandbox. If disabled, cached images are used when available.")
	public boolean isAlwaysPullContainerImage() {
		return alwaysPullContainerImage;
	}

	public void setAlwaysPullContainerImage(boolean alwaysPullContainerImage) {
		this.alwaysPullContainerImage = alwaysPullContainerImage;
	}

	@Editable(order=520, group="Container Settings", description="Optional registry logins for steps and services that use container images inside the sandbox. "
			+ "These logins are not used to pull the sandbox template image. "
			+ "Logins specified in a step or service override these for the same registry. For built-in registry, use "
			+ "<code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and access token for password.")
	public List<@Valid RegistryLogin> getRegistryLogins() {
		// Existing executor settings may predate registry logins.
		if (registryLogins == null)
			registryLogins = new ArrayList<>();
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Override
	public List<RegistryLoginFacade> getRegistryLogins(String jobToken) {
		return getRegistryLogins().stream().map(it -> it.getFacade(jobToken)).toList();
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		var registryUrls = new HashSet<String>();
		for (var login : getRegistryLogins()) {
			if (!registryUrls.add(login.getRegistryUrl())) {
				context.disableDefaultConstraintViolation();
				var message = login.getRegistryUrl() != null
						? "Duplicate login entry for registry '" + login.getRegistryUrl() + "'"
						: "Duplicate login entry for official registry";
				context.buildConstraintViolationWithTemplate(message)
						.addPropertyNode("registryLogins").addConstraintViolation();
				return false;
			}
		}
		return true;
	}

	@Editable(order=510, group="Container Settings", description="Optionally specify Docker options to run containers inside the sandbox. "
			+ "Multiple options should be separated by space, and single options containing spaces should be quoted. "
			+ "Do not specify <code>--network</code> or <code>--net</code> for jobs using services, as their network is managed automatically. "
			+ "File paths refer to the sandbox filesystem; relative file paths are resolved from the job working directory.")
	@ReservedOptions({"--", "(--workdir|--detach|--attach|--tty|--interactive|--rm|--restart|--name|--user|--entrypoint|--stop-timeout)(=.*)?",
			"(-[Pq]*[wdatiu]).*"})
	public String getRunOptions() {
		return runOptions;
	}

	public void setRunOptions(String runOptions) {
		this.runOptions = runOptions;
	}

	@Editable(order=515, group="Container Settings", description="Optionally specify Docker options to create the network for jobs using services. "
			+ "Multiple options should be separated by space, and single options containing spaces should be quoted.")
	@ReservedOptions({"--", "(-d).*", "--driver", "(--driver)=.*"})
	public String getNetworkOptions() {
		return networkOptions;
	}

	public void setNetworkOptions(String networkOptions) {
		this.networkOptions = networkOptions;
	}

	@Editable(order=50100, group="More Settings", name="Sandbox Executable", description="Path to the Docker Sandboxes sbx executable")
	@NotEmpty
	public String getSandboxExecutable() {
		return sandboxExecutable;
	}

	public void setSandboxExecutable(String sandboxExecutable) {
		this.sandboxExecutable = sandboxExecutable;
	}

	@Editable(order=600, group="More Settings", description="Whether or not to always pull the template image when "
			+ "creating a sandbox. If disabled, a cached image is used when available.")
	public boolean isAlwaysPullTemplateImage() {
		return alwaysPullTemplateImage;
	}

	public void setAlwaysPullTemplateImage(boolean alwaysPullTemplateImage) {
		this.alwaysPullTemplateImage = alwaysPullTemplateImage;
	}

	@Editable(order=120, name="CPUs", placeholder = "Docker sandbox default", description="CPU count for each sandbox. Leave empty to use the Docker Sandboxes default (All CPUs).")
	@Min(1)
	public Integer getCpus() {
		return cpus;
	}

	public void setCpus(Integer cpus) {
		this.cpus = cpus;
	}

	@Editable(order=130, placeholder = "Docker sandbox default", description="Memory for each sandbox, for example 2g. Leave empty to use the Docker Sandboxes default (50% of host memory).")
	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	@Editable(order=1000, description="Maximum concurrent jobs on each server. Leave empty to use its CPU count.")
	@Min(1)
	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	protected int getConcurrencyNumber() {
		return concurrency != null ? concurrency : 0;
	}

	@Editable(order=10000, name="Applicable Jobs")
	@io.onedev.server.annotation.JobMatch(withProjectCriteria=true, withJobCriteria=true)
	public String getJobMatch() {
		return jobMatch;
	}

	public void setJobMatch(String jobMatch) {
		this.jobMatch = jobMatch;
	}

	@Override
	public boolean isApplicable(JobMatchContext context) {
		return jobMatch == null || JobMatch.parse(jobMatch, true, true).matches(context);
	}

	protected SandboxSettings getSandboxSettings(String jobToken, @Nullable String sandboxTemplate) {
		return new SandboxSettings(sandboxTemplate, cpus, memory, alwaysPullTemplateImage, alwaysPullContainerImage,
                OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl(), runOptions, networkOptions,
				getRegistryLogins(jobToken));
	}

	@Override
	public boolean execute(JobContext context, TaskLogger logger) {
		ClusterTask<Boolean> runnable = () -> OneDev.getInstance(JobService.class).runJob(context, new JobRunnable() {

			@Serial
			private static final long serialVersionUID = 1L;

			private transient volatile SandboxJobController sandboxController;

			private transient TaskLogger sandboxLogger;

			@Override
			public boolean run(TaskLogger logger) {
				notifyJobRunning(context.getBuildId(), null);
				logger.log("Executing job with sandbox executor '" + getName() + "'...");
				sandboxLogger = logger;
				var settings = getSandboxSettings(context.getJobToken(), context.getSandboxTemplate());
				sandboxController = new SandboxJobController(getSandboxExecutable(), settings);
				var data = new SandboxJobData(context.getJobToken(), getName(), context.getProjectPath(),
						context.getProjectId(), context.getRefName(), context.getCommitId().name(),
						context.getBuildNumber(), context.getSubmitSequence(), context.getActions(), context.getSecretMasker(),
						context.getServices(), settings);
				return sandboxController.runJob(data, context.getServices(), Bootstrap.getTrustCertsDir(), Map.of(), logger);
			}

			@Override
			public void resume(JobContext context) {
				if (sandboxController != null)
					sandboxController.resume(sandboxLogger);
			}

			@Override
			public Shell openShell(JobContext context, JobTerminal terminal) {
				if (sandboxController == null)
					throw new ExplicitException("Sandbox not running");
				return new CommandlineShell(terminal, sandboxController.openShell());
			}
		});
		logger.log("Pending resource allocation...");
		try {
			return OneDev.getInstance(ResourceService.class).submitServerTask(null, getName(), getConcurrencyNumber(), 1, runnable).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void test(TestData data, TaskLogger logger) {
		var dockerImage = data.isTestContainer() ? data.getImage() : null;
		new SandboxJobController(getSandboxExecutable(), getSandboxSettings(UUID.randomUUID().toString(), data.getTemplate()))
				.testSandbox(dockerImage, Bootstrap.getTrustCertsDir(), logger);
	}

	@Editable(name="Test Sandbox Executor")
	public static class TestData implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private String template;

		private boolean testContainer;

		private String image;

		@Editable(order=100, description="""
				Specify a Docker sandbox shell template image to test against. This image should \
				contain Python 3 and Git. To also test running a container, configure a Docker daemon \
				and provide passwordless sudo if the default user is non-root""")
		@NotEmpty
		public String getTemplate() {
			return template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

		@Editable(order=200, description="Enable to also test running a command in a Docker container inside the sandbox")
		public boolean isTestContainer() {
			return testContainer;
		}

		public void setTestContainer(boolean testContainer) {
			this.testContainer = testContainer;
		}

		@Editable(order=300, name="container:image", description="Specify the container image to test against")
		@DependsOn(property="testContainer")
		@NotEmpty
		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}
	}

}
