package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class ListFileChangesCommand {

	private static final Logger logger = LoggerFactory.getLogger(ListFileChangesCommand.class);
	
	private final File workingDir;
	
	private final String fromRev;
	
	private final String toRev;
	
	public ListFileChangesCommand(File workingDir, String fromRev, String toRev) {
		this.workingDir = workingDir;
		this.fromRev = fromRev;
		this.toRev = toRev;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public Collection<FileChange> run() {
		List<FileChange> fileChanges = new ArrayList<>();
		
		Commandline git = newGit().workingDir(workingDir);
		
		git.addArgs("diff", "--name-status", "--no-renames", fromRev + ".." + toRev);
		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
        		StringTokenizer tokenizer = new StringTokenizer(line, "\t");
        		String statusCode = tokenizer.nextToken();
        		if (statusCode.equals("M") || statusCode.equals("T")) {
        			String path = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
        			fileChanges.add(new FileChange(path, path, -1, -1));
        		} else if (statusCode.equals("D")) {
        			String oldPath = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
        			fileChanges.add(new FileChange(oldPath, null, -1, -1));
        		} else if (statusCode.equals("A")) {
        			String newPath = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
        			fileChanges.add(new FileChange(null, newPath, -1, -1));
        		}
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
