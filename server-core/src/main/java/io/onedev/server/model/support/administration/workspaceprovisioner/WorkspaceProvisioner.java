package io.onedev.server.model.support.administration.workspaceprovisioner;

import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.initRepository;
import static io.onedev.k8shelper.KubernetesHelper.installGitLfs;
import static io.onedev.k8shelper.KubernetesHelper.setupGitCerts;
import static io.onedev.k8shelper.KubernetesHelper.setupOriginUrl;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.DnsName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;
import io.onedev.server.workspace.WorkspaceService;

@Editable
@ExtensionPoint
public abstract class WorkspaceProvisioner implements Serializable {

    private static final long serialVersionUID = 1L;

	private boolean enabled = true;

	private String name;

	private Integer concurrency;

	protected String applicableProjects;
	
	@Editable(order=1000, placeholder="CPU cores", description = """
			Specify max number of workspaces this provisioner can handle concurrently.
			Leave empty to set as CPU cores""")
	@Min(1)
	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10)
	@DnsName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	protected SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}

	protected ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	protected GitLocation getGitLocation() {
		return OneDev.getInstance(GitLocation.class);
	}

	protected WorkspaceService getWorkspaceService() {
		return OneDev.getInstance(WorkspaceService.class);
	}

	protected File getWorkspaceDir(WorkspaceContext context) {
		return getWorkspaceService().getWorkspaceDir(context.getProjectId(), context.getWorkspaceNumber());
	}

	protected void setupRepository(WorkspaceContext context, String runtimeWorkspaceDirPath, TaskLogger logger) {
		var localServer = getClusterService().getLocalServerAddress();
		logger.log(String.format("Provisioning workspace (provisioner: %s, server: %s)...",
				getName(), localServer));

		var workspaceDir = getWorkspaceDir(context);
		var workDir = Workspace.getWorkDir(context.getProjectId(), context.getWorkspaceNumber());
		FileUtils.createDir(workDir);

		var cloneInfo = context.getCloneInfo();

		var infoLogger = AgentUtils.newInfoLogger(logger);
		var warningLogger = AgentUtils.newWarningLogger(logger);

		var git = CommandUtils.newGit();
		git.workingDir(workDir);

		initRepository(git, infoLogger, warningLogger);

		git.args("-c", "safe.directory=*", "config", "user.name", context.getUserName());
		git.execute(infoLogger, warningLogger).checkReturnCode();

		git.args("-c", "safe.directory=*", "config", "pull.rebase", "false");
		git.execute(infoLogger, warningLogger).checkReturnCode();

		git.args("-c", "safe.directory=*", "config", "user.email", context.getUserEmail());
		git.execute(infoLogger, warningLogger).checkReturnCode();

		var noCommits = new AtomicBoolean(false);
		git.args("-c", "safe.directory=*", "status");		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("No commits yet") || line.startsWith("nothing to commit")) {
					noCommits.set(true);
				} else if (!line.startsWith("On branch") && line.trim().length() != 0) {
					logger.log(line);
				}
			}
			
		}, warningLogger).checkReturnCode();

		git.clearArgs();
		var trustCertsFile = new File(workspaceDir, "trust-certs.pem");
		setupGitCerts(git, Bootstrap.getTrustCertsDir(), trustCertsFile, 
				runtimeWorkspaceDirPath + "/" + trustCertsFile.getName(), infoLogger, warningLogger);
		cloneInfo.setupGitAuth(git, workspaceDir, runtimeWorkspaceDirPath, 
				infoLogger, warningLogger);

		if (noCommits.get()) {
			if (context.getBranch() != null) {
				logger.log("Cloning repository...");
				cloneRepository(git, context.getProjectGitDir(), cloneInfo.getCloneUrl(),
						GitUtils.branch2ref(context.getBranch()), null,
						context.getSpec().isRetrieveLfs(), false, 0,
						infoLogger, warningLogger);
			} else { // no commits at server side
				setupOriginUrl(git, cloneInfo.getCloneUrl(), infoLogger, warningLogger);
	
				git.args("-c", "safe.directory=*", "config", "push.autoSetupRemote", "true");
				git.execute(infoLogger, warningLogger).checkReturnCode();

				if (context.getSpec().isRetrieveLfs())
					installGitLfs(git, infoLogger, warningLogger);
			}
		} else {
			setupOriginUrl(git, cloneInfo.getCloneUrl(), infoLogger, warningLogger);

			if (context.getSpec().isRetrieveLfs())
				installGitLfs(git, infoLogger, warningLogger);

			logger.log("Repository already exists, skipping clone");
		}
	}

	public abstract boolean isApplicable(Project project);

	public abstract WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger);

	public void onMoveProject(String oldPath, String newPath) {
		applicableProjects = Project.substitutePath(applicableProjects, oldPath, newPath);
	}

	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		if (Project.containsPath(applicableProjects, projectPath))
			usage.add("applicable projects");
		return usage;
	}

}
