package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class ListFileChangesCommand extends GitCommand<Collection<FileChange>> {

	private static final Logger logger = LoggerFactory.getLogger(ListFileChangesCommand.class);
	
	private String fromRev;
	
	private String toRev;
	
	public ListFileChangesCommand(File gitDir) {
		super(gitDir);
	}
	
	public ListFileChangesCommand fromRev(String fromRev) {
		this.fromRev = fromRev;
		return this;
	}
	
	public ListFileChangesCommand toRev(String toRev) {
		this.toRev = toRev;
		return this;
	}
	
	@Override
	public Collection<FileChange> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		List<FileChange> fileChanges = new ArrayList<>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("diff", "--name-status", "--no-renames", fromRev + ".." + toRev);
		
		cmd.execute(new LineConsumer() {

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
