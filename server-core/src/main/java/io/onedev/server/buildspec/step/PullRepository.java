package io.onedev.server.buildspec.step;

import com.google.common.base.Splitter;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.LfsFetchAllCommand;
import io.onedev.server.git.command.LfsFetchCommand;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.match.WildcardUtils;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Maps.difference;
import static io.onedev.server.git.GitUtils.getReachableCommits;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jgit.lib.Constants.R_HEADS;

@Editable(order=60, name="Pull from Remote", group=StepGroup.REPOSITORY_SYNC, description=""
		+ "This step pulls specified refs from remote. For security reason, it is only allowed "
		+ "to run from default branch")
public class PullRepository extends SyncRepository {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(PullRepository.class);
	
	private boolean syncToChildProject;
	
	private String childProject;

	private String refs = "refs/heads/* refs/tags/*";
	
	private boolean withLfs;
	
	@Editable(order=205, description="If enabled, sync to child project instead of current project")
	public boolean isSyncToChildProject() {
		return syncToChildProject;
	}

	public void setSyncToChildProject(boolean syncToChildProject) {
		this.syncToChildProject = syncToChildProject;
	}

	@Editable(order=210, description="Select child project to sync to")
	@ShowCondition("isSyncToChildProjectEnabled")
	@ChoiceProvider("getChildProjects")
	@NotEmpty
	public String getChildProject() {
		return childProject;
	}

	public void setChildProject(String childProject) {
		this.childProject = childProject;
	}

	@SuppressWarnings("unused")
	private static boolean isSyncToChildProjectEnabled() {
		return (boolean) EditContext.get().getInputValue("syncToChildProject");
	}
	
	@SuppressWarnings("unused")
	private static List<String> getChildProjects() {
		int prefixLen = Project.get().getPath().length() + 1;
		List<String> choices = new ArrayList<>(Project.get().getDescendants()
				.stream().map(pr -> pr.getPath().substring(prefixLen)).collect(toList()));
		Collections.sort(choices);
		return choices;
	}

	@Editable(order=410, description="Specify space separated refs to pull from remote. '*' can be used in ref "
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

	@Editable(order=450, name="Transfer LFS Files", descriptionProvider="getLfsDescription")
	public boolean isWithLfs() {
		return withLfs;
	}

	public void setWithLfs(boolean withLfs) {
		this.withLfs = withLfs;
	}

	@SuppressWarnings("unused")
	private static String getLfsDescription() {
		if (!Bootstrap.isInDocker()) {
			return "If this option is enabled, git lfs command needs to be installed on OneDev server "
					+ "(even this step runs on other node)";
		} else {
			return null;
		}
	}

	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		Project buildProject = build.getProject();
		if (!buildProject.isCommitOnBranch(build.getCommitId(), buildProject.getDefaultBranch()))
			throw new ExplicitException("For security reason, this step is only allowed to run from default branch");
		
		String remoteUrl = getRemoteUrlWithCredential(build);
		Long projectId = getTargetProject(build).getId();
		
		var task = new PullTask(projectId, remoteUrl, getCertificate(), getRefs(), isForce(), isWithLfs(), getProxy());
		getProjectManager().runOnActiveServer(projectId, task);
		
		return null;
	}
	
	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private Project getTargetProject(Build build) {
		Project buildProject = build.getProject();
		if (isSyncToChildProject()) {
			String projectPath = buildProject.getPath() + "/" + getChildProject();
			Project childProject = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
			if (childProject == null)
				throw new ExplicitException("Unable to find child project: " + getChildProject());
			return childProject;
		} else {
			return buildProject;
		}
	}
	
	private static class PullTask implements ClusterTask<Void> {

		private final Long projectId;
		
		private final String remoteUrl;
		
		private final String certificate;
		
		private final String refs;
		
		private final boolean force;
		
		private final boolean withLfs;
		
		private final String proxy;
		
		PullTask(Long projectId, String remoteUrl, @Nullable String certificate, 
				 String refs, boolean force, boolean withLfs, @Nullable String proxy) {
			this.projectId = projectId;
			this.remoteUrl = remoteUrl;
			this.certificate = certificate;
			this.refs = refs;
			this.force = force;
			this.withLfs = withLfs;
			this.proxy = proxy;
		}

		private Map<String, ObjectId> getBranchCommits(Repository repository) {
			Map<String, ObjectId> commitIds = new HashMap<>();
			for (var ref: GitUtils.getCommitRefs(repository, R_HEADS)) {
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
			try {
				Repository repository = getProjectManager().getRepository(projectId);

				String defaultBranch = GitUtils.getDefaultBranch(repository);
				Map<String, ObjectId> oldCommitIds = getBranchCommits(repository);

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

				git.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.debug(line);
					}

				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						if (!line.startsWith("From") && !line.contains("->"))
							logger.error(line);
						else
							logger.debug(line);
					}

				}).checkReturnCode();

				Map<String, ObjectId> newCommitIds = getBranchCommits(repository);

				if (defaultBranch == null) {
					logger.debug("Determining remote head branch...");

					git.clearArgs();
					configureProxy(git, proxy);
					configureCertificate(git, certificateFile);
					git.addArgs("remote", "show", remoteUrl);

					AtomicReference<String> headBranch = new AtomicReference<>(null);
					git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.debug(line);
							if (line.trim().startsWith("HEAD branch:"))
								headBranch.set(line.trim().substring("HEAD branch:".length()).trim());
						}

					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.warn(line);
						}

					}).checkReturnCode();

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

						git.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								if (headBranch.get() == null)
									headBranch.set(StringUtils.stripStart(line.trim(), "*").trim());
							}

						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.warn(line);
							}

						}).checkReturnCode();
					}
					if (headBranch.get() != null)
						GitUtils.setDefaultBranch(repository, headBranch.get());
				}

				if (withLfs) {
					git.clearArgs();
					configureProxy(git, proxy);
					configureCertificate(git, certificateFile);
					var sinceCommitIds = getProjectManager().readLfsSinceCommits(projectId);

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
					getProjectManager().writeLfsSinceCommits(projectId, newCommitIds.values());
				}

				OneDev.getInstance(SessionManager.class).runAsync(() -> {
					try {
						// Access db connection in a separate thread to avoid possible deadlock, as
						// the parent thread is blocking another thread holding database connections
						var project = getProjectManager().load(projectId);
						MapDifference<String, ObjectId> difference = difference(oldCommitIds, newCommitIds);
						ListenerRegistry registry = OneDev.getInstance(ListenerRegistry.class);
						for (Map.Entry<String, ObjectId> entry : difference.entriesOnlyOnLeft().entrySet()) {
							if (RefUpdated.isValidRef(entry.getKey()))
								registry.post(new RefUpdated(project, entry.getKey(), entry.getValue(), ObjectId.zeroId()));
						}
						for (Map.Entry<String, ObjectId> entry : difference.entriesOnlyOnRight().entrySet()) {
							if (RefUpdated.isValidRef(entry.getKey()))
								registry.post(new RefUpdated(project, entry.getKey(), ObjectId.zeroId(), entry.getValue()));
						}
						for (Map.Entry<String, ValueDifference<ObjectId>> entry : difference.entriesDiffering().entrySet()) {
							if (RefUpdated.isValidRef(entry.getKey())) {
								registry.post(new RefUpdated(project, entry.getKey(),
										entry.getValue().leftValue(), entry.getValue().rightValue()));
							}
						}
					} catch (Exception e) {
						logger.error("Error posting ref updated event", e);
					}
				});

				return null;
			} finally {
				if (certificateFile != null)
					FileUtils.deleteFile(certificateFile);
			}
		}
		
	}
}
