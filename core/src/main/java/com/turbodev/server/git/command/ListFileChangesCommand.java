package com.turbodev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turbodev.utils.command.Commandline;
import com.turbodev.utils.command.LineConsumer;
import com.google.common.base.Preconditions;

public class ListFileChangesCommand extends GitCommand<Collection<FileChange>> {

	private static final Logger logger = LoggerFactory.getLogger(ListFileChangesCommand.class);
	
	private String fromRev;
	
	private String toRev;
	
	private String path;
	
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
	
	public ListFileChangesCommand path(String path) {
		this.path = path;
		return this;
	}
	
	@Override
	public Collection<FileChange> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		final Collection<FileChange> fileChanges = new ArrayList<>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("diff", "--numstat", "--no-renames", fromRev + ".." + toRev);
		
		if (path != null)
			cmd.addArgs("--", path);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.contains("\t")) {
					StringTokenizer tokenizer = new StringTokenizer(line, "\t");
					
					int additions;
					String field = tokenizer.nextToken();
					if (field.equals("-"))
						additions = -1;
					else
						additions = Integer.parseInt(field);
					
					int deletions;
					field = tokenizer.nextToken();
					if (field.equals("-"))
						deletions = -1;
					else
						deletions = Integer.parseInt(field);
					
    				String file = QuotedString.GIT_PATH.dequote(tokenizer.nextToken());
    				
    				fileChanges.add(new FileChange(null, file, additions, deletions));
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
