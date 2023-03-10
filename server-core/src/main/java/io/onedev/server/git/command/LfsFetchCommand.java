package io.onedev.server.git.command;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class LfsFetchCommand {

	private static final int LFS_FETCH_BATCH = 100;
	
	private static final Logger logger = LoggerFactory.getLogger(LfsFetchCommand.class);
	
	private final File workingDir;
	
	private final String remoteUrl;
	
	private final List<ObjectId> commitIds;
	
	public LfsFetchCommand(File workingDir, String remoteUrl, List<ObjectId> commitIds) {
		this.workingDir = workingDir;
		this.remoteUrl = remoteUrl;
		this.commitIds = commitIds;
	}

	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public void run() {
		Commandline git = newGit().workingDir(workingDir);

		for (List<ObjectId> batch : Lists.partition(commitIds, LFS_FETCH_BATCH)) {
			git.clearArgs();
			git.addArgs("lfs", "fetch", remoteUrl);
			for (ObjectId commitId : batch)
				git.addArgs(commitId.name());
			git.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.trace(line);
				}

			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}

			}).checkReturnCode();
		}
	}

}
