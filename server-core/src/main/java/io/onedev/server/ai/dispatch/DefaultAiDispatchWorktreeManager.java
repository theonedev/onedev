package io.onedev.server.ai.dispatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.model.AiDispatchRun;
import io.onedev.server.service.ProjectService;

@Singleton
public class DefaultAiDispatchWorktreeManager implements AiDispatchWorktreeManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAiDispatchWorktreeManager.class);

	private final ProjectService projectService;

	@Inject
	public DefaultAiDispatchWorktreeManager(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public PreparedWorktree prepare(AiDispatchRun run) {
		var sourceProject = run.getRequest().getSourceProject();
		if (sourceProject == null)
			throw new IllegalStateException("Source project is no longer available");

		File gitDir = projectService.getGitDir(sourceProject.getId());
		File worktreeDir = new File(Bootstrap.getTempDir(), "ai-run-" + run.getId());
		if (worktreeDir.exists())
			FileUtils.deleteDir(worktreeDir);

		Commandline git = newGit(gitDir);
		git.addArgs("worktree", "add");
		if (run.hasFlag("--no-commit"))
			git.addArgs("--detach");
		git.addArgs(worktreeDir.getAbsolutePath(), run.getRequest().getSourceBranch());
		git.execute(infoLogger(), errorLogger()).checkReturnCode();

		String baselineCommit = runGitAndGetSingleLine(worktreeDir, "rev-parse", "HEAD");
		return new PreparedWorktree(gitDir, worktreeDir, baselineCommit);
	}

	@Override
	public List<String> collectNewCommits(PreparedWorktree worktree) {
		var commits = new ArrayList<String>();
		Commandline git = newGit(worktree.getDirectory());
		git.addArgs("rev-list", "--reverse", worktree.getBaselineCommit() + "..HEAD");
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (!line.isBlank())
					commits.add(line.trim());
			}

		}, errorLogger()).checkReturnCode();
		return commits;
	}

	@Override
	public void cleanup(PreparedWorktree worktree) {
		try {
			if (worktree.getDirectory().exists() && worktree.getGitDir() != null) {
				Commandline git = newGit(worktree.getGitDir());
				git.addArgs("worktree", "remove", worktree.getDirectory().getAbsolutePath(), "--force");
				git.execute(infoLogger(), errorLogger()).checkReturnCode();
			}
		} catch (Exception e) {
			logger.warn("Error cleaning AI dispatch worktree {}", worktree.getDirectory(), e);
		} finally {
			if (worktree.getDirectory().exists())
				FileUtils.deleteDir(worktree.getDirectory());
		}
	}

	private String runGitAndGetSingleLine(File workingDir, String... args) {
		var lines = new ArrayList<String>();
		Commandline git = newGit(workingDir);
		git.addArgs(args);
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				lines.add(line);
			}

		}, errorLogger()).checkReturnCode();
		return lines.isEmpty()? null: lines.get(0).trim();
	}

	private Commandline newGit(File workingDir) {
		return CommandUtils.newGit().workingDir(workingDir);
	}

	private LineConsumer infoLogger() {
		return new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}

		};
	}

	private LineConsumer errorLogger() {
		return new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.warn(line);
			}

		};
	}

}
