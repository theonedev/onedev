package io.onedev.server.ai.dispatch;

import java.io.File;
import java.util.List;

import io.onedev.server.model.AiDispatchRun;

public interface AiDispatchWorktreeManager {

	PreparedWorktree prepare(AiDispatchRun run);

	List<String> collectNewCommits(PreparedWorktree worktree);

	void cleanup(PreparedWorktree worktree);

	class PreparedWorktree {

		private final File gitDir;

		private final File directory;

		private final String baselineCommit;

		public PreparedWorktree(File gitDir, File directory, String baselineCommit) {
			this.gitDir = gitDir;
			this.directory = directory;
			this.baselineCommit = baselineCommit;
		}

		public File getGitDir() {
			return gitDir;
		}

		public File getDirectory() {
			return directory;
		}

		public String getBaselineCommit() {
			return baselineCommit;
		}

	}

}
