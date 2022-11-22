package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class ListNumStatsCommand {

	private static final Logger logger = LoggerFactory.getLogger(ListNumStatsCommand.class);
	
	private final File workingDir;
	
	private final String fromRev;
	
	private final String toRev;
	
	private final boolean noRenames;
	
	public ListNumStatsCommand(File workingDir, String fromRev, String toRev, boolean noRenames) {
		this.workingDir = workingDir;
		this.fromRev = fromRev;
		this.toRev = toRev;
		this.noRenames = noRenames;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public List<FileChange> run() {
		List<FileChange> fileChanges = new ArrayList<>();
		
		Commandline git = newGit().workingDir(workingDir);

		if (noRenames) {
			git.addArgs("diff", "--numstat", "--no-renames", fromRev + ".." + toRev);
		} else {
			git.addArgs("-c", "diff.renameLimit=1000", "diff", "--numstat", 
					"--find-renames", fromRev + ".." + toRev);
		}
		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				fileChanges.add(CommandUtils.parseNumStats(line));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return fileChanges;
	}

}