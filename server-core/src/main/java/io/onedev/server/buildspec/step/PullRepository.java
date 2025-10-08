package io.onedev.server.buildspec.step;

import static com.google.common.collect.Maps.difference;
import static io.onedev.server.git.GitUtils.getReachableCommits;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import javax.validation.constraints.NotEmpty;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.bootstrap.SecretMasker;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.LfsFetchAllCommand;
import io.onedev.server.git.command.LfsFetchCommand;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;

@Editable(order=1070, name="Pull from Remote", group=StepGroup.REPOSITORY_SYNC, description=""
		+ "This step pulls specified refs from remote")
public class PullRepository extends SyncRepository {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(PullRepository.class);
	
	private String targetProject;
	
	private String accessTokenSecret;
	
	private String refs = "refs/heads/* refs/tags/*";
	
	private boolean withLfs;

	@Editable(order=600, placeholder = "Current project", description=
			"Select project to sync to. Leave empty to sync to current project")
	@ProjectChoice
	public String getTargetProject() {
		return targetProject;
	}

	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}

	@Editable(order=650, name="Access Token for Target Project", description = "Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> " +
			"whose value is an access token with management permission for above project. Note that access token " +
			"is not required if sync to current or child project and build commit is reachable from " +
			"default branch")
	@ChoiceProvider("getSecretChoices")
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	@Editable(order=700, description="Specify space separated refs to pull from remote. '*' can be used in ref "
			+ "name for wildcard match<br>"
			+ "<b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update "
			+ "branches/tags via this step")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getRefs() {
		return refs;
	}

	public void setRefs(String refs) {
		this.refs = refs;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}

	@Editable(order=800, name="Transfer LFS Files", descriptionProvider="getLfsDescription")
	public boolean isWithLfs() {
		return withLfs;
	}

	public void setWithLfs(boolean withLfs) {
		this.withLfs = withLfs;
	}

	@SuppressWarnings("unused")
	private static String getLfsDescription() {
		if (!Bootstrap.isInDocker()) {
			return _T("If this option is enabled, git lfs command needs to be installed on OneDev server "
					+ "(even this step runs on other node)");
		} else {
			return null;
		}
	}

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(SessionService.class).call(() -> {
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			Project project = build.getProject();
			Project targetProject;
			if (getTargetProject() != null) {
				targetProject = getProjectService().findByPath(getTargetProject());
				if (targetProject == null)
					throw new ExplicitException("Target project not found: " + getTargetProject());
			} else {
				targetProject = project;
			}
			boolean authorized = false;
			if (project.isCommitOnBranch(build.getCommitId(), project.getDefaultBranch()) 
					&& project.isSelfOrAncestorOf(targetProject)) {
				authorized = true;
			} else if (getAccessTokenSecret() != null && 
					SecurityUtils.canManageProject(build.getAccessToken(getAccessTokenSecret()).asSubject(), targetProject)) {
				authorized = true;
			}
			if (!authorized) 
				throw new ExplicitException("This build is not authorized to sync to project: " + targetProject.getPath());

			Long userId;
			if (getAccessTokenSecret() != null) {
				userId = build.getAccessToken(getAccessTokenSecret()).getOwner().getId();
			} else {
				userId = User.SYSTEM_ID;
			}

			String remoteUrl = getRemoteUrlWithCredential(build);
			Long targetProjectId = targetProject.getId();
			var task = new PullTask(targetProjectId, userId, remoteUrl, getCertificate(), getRefs(), isForce(), isWithLfs(), getProxy(), build.getSecretMasker());
			getProjectService().runOnActiveServer(targetProjectId, task);
			return new ServerStepResult(true);
		});
	}
	
	private static ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	private static UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
	private static class PullTask implements ClusterTask<Void> {

		private final Long projectId;

		private final Long userId;
		
		private final String remoteUrl;
		
		private final String certificate;
		
		private final String refs;
		
		private final boolean force;
		
		private final boolean withLfs;
		
		private final String proxy;

		private final SecretMasker secretMasker;
		
		PullTask(Long projectId, Long userId, String remoteUrl, @Nullable String certificate, 
				 String refs, boolean force, boolean withLfs, @Nullable String proxy, 
				 SecretMasker secretMasker) {
			this.projectId = projectId;
			this.userId = userId;
			this.remoteUrl = remoteUrl;
			this.certificate = certificate;
			this.refs = refs;
			this.force = force;
			this.withLfs = withLfs;
			this.proxy = proxy;
			this.secretMasker = secretMasker;
		}

		private Map<String, ObjectId> getRefCommits(Repository repository) {
			Map<String, ObjectId> commitIds = new HashMap<>();
			var commitRefs = new ArrayList<RefFacade>();
			commitRefs.addAll(GitUtils.getCommitRefs(repository, R_HEADS));
			commitRefs.addAll(GitUtils.getCommitRefs(repository, R_TAGS));
			for (var ref: commitRefs) {
				boolean matches = false;
				for (String pattern: Splitter.on(" ").omitEmptyStrings().trimResults().split(refs)) {
					if (WildcardUtils.matchString(pattern, ref.getName())) {
						matches = true;
						break;
					}
				}
				if (matches)
					commitIds.put(ref.getName(), ref.getPeeledObj().copy());
			}
			return commitIds;
		}

		@Override
		public Void call() throws Exception {
			var certificateFile = writeCertificate(certificate);
			SecretMasker.push(secretMasker);
			try {
				Repository repository = getProjectService().getRepository(projectId);

				String defaultBranch = GitUtils.getDefaultBranch(repository);
				Map<String, ObjectId> oldCommitIds = getRefCommits(repository);

				Commandline git = CommandUtils.newGit();
				configureProxy(git, proxy);
				configureCertificate(git, certificateFile);
				git.workingDir(repository.getDirectory());

				git.addArgs("fetch");
				git.addArgs(remoteUrl);
				if (force)
					git.addArgs("--force");

				for (String each : Splitter.on(' ').omitEmptyStrings().trimResults().split(refs))
					git.addArgs(each + ":" + each);

				var errorMessage = new StringBuilder();
				var result = git.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.debug(line);
					}

				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						if (!line.startsWith("From")) {
							errorMessage.append(line).append("\n");
							logger.warn(line);
						} else {
							logger.debug(line);
						}
					}

				});
				if (errorMessage.length() != 0)
					result.setErrorMessage(errorMessage.toString());
				result.checkReturnCode();

				Map<String, ObjectId> newCommitIds = getRefCommits(repository);

				if (defaultBranch == null) {
					logger.debug("Determining remote head branch...");

					git.clearArgs();
					configureProxy(git, proxy);
					configureCertificate(git, certificateFile);
					git.addArgs("remote", "show", remoteUrl);

					AtomicReference<String> headBranch = new AtomicReference<>(null);
					errorMessage.setLength(0);
					result = git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.debug(line);
							if (line.trim().startsWith("HEAD branch:"))
								headBranch.set(line.trim().substring("HEAD branch:".length()).trim());
						}

					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							errorMessage.append(line).append("\n");
							logger.warn(line);
						}

					});
					if (errorMessage.length() != 0)
						result.setErrorMessage(errorMessage.toString());
					result.checkReturnCode();

					if (headBranch.get() != null) {
						if (GitUtils.resolve(repository, R_HEADS + headBranch.get(), false) == null) {
							logger.debug("Remote head branch not synced, using first branch as default");
							headBranch.set(null);
						}
					} else {
						logger.debug("Remote head branch not found, using first branch as default");
					}
					if (headBranch.get() == null) {
						git.clearArgs();
						git.addArgs("branch");

						errorMessage.setLength(0);
						result = git.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								if (headBranch.get() == null)
									headBranch.set(StringUtils.stripStart(line.trim(), "*").trim());
							}

						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								errorMessage.append(line).append("\n");
								logger.warn(line);
							}

						});
						if (errorMessage.length() != 0)
							result.setErrorMessage(errorMessage.toString());
						result.checkReturnCode();
					}
					if (headBranch.get() != null)
						GitUtils.setDefaultBranch(repository, headBranch.get());
				}

				if (withLfs) {
					git.clearArgs();
					configureProxy(git, proxy);
					configureCertificate(git, certificateFile);
					var sinceCommitIds = getProjectService().readLfsSinceCommits(projectId);

					if (sinceCommitIds.isEmpty()) {
						new LfsFetchAllCommand(git.workingDir(), remoteUrl) {
							protected Commandline newGit() {
								return git;
							}
						}.run();
					} else {
						var fetchCommitIds = getReachableCommits(repository, sinceCommitIds, newCommitIds.values())
								.stream().map(AnyObjectId::copy).collect(toList());
						new LfsFetchCommand(git.workingDir(), remoteUrl, fetchCommitIds) {
							@Override
							protected Commandline newGit() {
								return git;
							}
						}.run();
					}
					getProjectService().writeLfsSinceCommits(projectId, newCommitIds.values());
				}

				OneDev.getInstance(SessionService.class).runAsync(() -> {
					try {
						// Access db connection in a separate thread to avoid possible deadlock, as
						// the parent thread is blocking another thread holding database connections
						var project = getProjectService().load(projectId);
						var user = getUserService().load(userId);
						MapDifference<String, ObjectId> difference = difference(oldCommitIds, newCommitIds);
						ListenerRegistry registry = OneDev.getInstance(ListenerRegistry.class);
						for (Map.Entry<String, ObjectId> entry : difference.entriesOnlyOnLeft().entrySet()) {
							if (RefUpdated.isValidRef(entry.getKey()))
								registry.post(new RefUpdated(user, project, entry.getKey(), entry.getValue(), ObjectId.zeroId()));
						}
						for (Map.Entry<String, ObjectId> entry : difference.entriesOnlyOnRight().entrySet()) {
							if (RefUpdated.isValidRef(entry.getKey()))
								registry.post(new RefUpdated(user, project, entry.getKey(), ObjectId.zeroId(), entry.getValue()));
						}
						for (Map.Entry<String, ValueDifference<ObjectId>> entry : difference.entriesDiffering().entrySet()) {
							if (RefUpdated.isValidRef(entry.getKey())) {
								registry.post(new RefUpdated(user, project, entry.getKey(),
										entry.getValue().leftValue(), entry.getValue().rightValue()));
							}
						}
					} catch (Exception e) {
						logger.error("Error posting ref updated event", e);
					}
				});

				return null;
			} finally {
				SecretMasker.pop();
				if (certificateFile != null)
					FileUtils.deleteFile(certificateFile);
			}
		}
		
	}
}
