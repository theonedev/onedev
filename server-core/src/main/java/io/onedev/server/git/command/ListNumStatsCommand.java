package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class ListNumStatsCommand extends GitCommand<List<FileChange>> {

	private static final Logger logger = LoggerFactory.getLogger(ListNumStatsCommand.class);
	
	private String fromRev;
	
	private String toRev;
	
	public ListNumStatsCommand(File gitDir) {
		super(gitDir);
	}
	
	public ListNumStatsCommand fromRev(String fromRev) {
		this.fromRev = fromRev;
		return this;
	}
	
	public ListNumStatsCommand toRev(String toRev) {
		this.toRev = toRev;
		return this;
	}
	
	@Override
	public List<FileChange> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		List<FileChange> fileChanges = new ArrayList<>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("-c", "diff.renameLimit=1000", "diff", "--numstat", 
				"--find-renames", fromRev + ".." + toRev);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				fileChanges.add(parseNumStats(line));
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