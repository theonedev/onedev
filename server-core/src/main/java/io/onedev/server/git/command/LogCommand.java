package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.GitUtils;

public abstract class LogCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(LogCommand.class); 
	
	public enum Field {PARENTS, AUTHOR, COMMITTER, COMMIT_DATE, SUBJECT, BODY, FILE_CHANGES, LINE_CHANGES};
	
 	private static final String BODY_END = "$<#BodyEnd#>$";
	
    private boolean firstParent;
    
    private EnumSet<Field> fields = EnumSet.noneOf(Field.class);
    
    private List<String> revisions = new ArrayList<>();
    
    public LogCommand(File gitDir) {
        super(gitDir);
    }

	public List<String> revisions() {
		return revisions;
	}

	public LogCommand revisions(List<String> revisions) {
		this.revisions = revisions;
		return this;
	}
	
	public LogCommand fields(EnumSet<Field> fields) {
		this.fields = fields;
		return this;
	}
	
	public LogCommand firstParent(boolean firstParent) {
		this.firstParent = firstParent;
		return this;
	}
	
	@Override
    public Void call() {
		Preconditions.checkArgument(!revisions.isEmpty(), "Log revisions have to be specified");
		
        Commandline cmd = cmd();

        String format = "hash:%H %n";

        if (fields.contains(Field.PARENTS)) {
        	format += "parents:%P %n";
        }
        if (fields.contains(Field.AUTHOR)) {
        	format += ""
        			+ "author:%an %n"
                    + "authorEmail:%ae %n"
        			+ "authorDate:%ad %n";
        }
        if (fields.contains(Field.COMMITTER)) {
        	format += ""
                    + "committer:%cn %n"
                    + "committerEmail:%ce %n"
        			+ "committerDate:%cd %n";
        }
        if (fields.contains(Field.COMMIT_DATE)) 
        	format += "commitDate:%cd %n";
        if (fields.contains(Field.SUBJECT)) 
        	format += "subject:%s %n";
        if (fields.contains(Field.BODY))
        	format += "body:%b" + BODY_END + "%n";

        if (fields.contains(Field.LINE_CHANGES)) 
	        cmd.addArgs("-c", "diff.renameLimit=1000", "log", "--numstat", "--find-renames");
        else if (fields.contains(Field.FILE_CHANGES))
            cmd.addArgs("-c", "diff.renameLimit=1000", "log", "--name-status", "--find-renames");
        else 
	        cmd.addArgs("log");
        
        cmd.addArgs("--format=" + format, "--date=raw");
        if (firstParent) {
        	cmd.addArgs("--first-parent");
            if (fields.contains(Field.LINE_CHANGES) || fields.contains(Field.FILE_CHANGES))
            	cmd.addArgs("-m");
        }

    	for (String revision: revisions)
    		cmd.addArgs(revision);

        AtomicReference<GitCommit.Builder> commitBuilderRef = new AtomicReference<>(null);
        AtomicBoolean inBodyRef = new AtomicBoolean(false);
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
	        	if (inBodyRef.get()) {
	        		line = line.trim();
	        		if (line.endsWith(BODY_END)) {
	        			commitBuilderRef.get().body += "\n" + line.substring(0, line.length()-BODY_END.length());
	        			inBodyRef.set(false);
	        		} else {
	        			commitBuilderRef.get().body += "\n" + line;
	        		}
	        	} else if (line.startsWith("body:")) {
	        		line = line.substring("body:".length()).trim();
	        		if (line.endsWith(BODY_END)) {
	        			commitBuilderRef.get().body = line.substring(0, line.length()-BODY_END.length());
	        		} else {
	        			commitBuilderRef.get().body = line;
	        			inBodyRef.set(true);
	        		}
	        	} else if (line.startsWith("hash:")) {
            		if (commitBuilderRef.get() != null)
	            		LogCommand.this.consume(commitBuilderRef.get().build());
            		commitBuilderRef.set(new GitCommit.Builder());
            		if (fields.contains(Field.PARENTS))
            			commitBuilderRef.get().parentHashes = new ArrayList<>();
            		if (fields.contains(Field.FILE_CHANGES) || fields.contains(Field.LINE_CHANGES))
            			commitBuilderRef.get().fileChanges = new ArrayList<>();
                	commitBuilderRef.get().hash = line.substring("hash:".length()).trim();
            	} else if (line.startsWith("parents:")) {
            		Splitter splitter = Splitter.on(" ").omitEmptyStrings().trimResults();
                	for (String each: splitter.split(line.substring("parents:".length())))
                		commitBuilderRef.get().parentHashes.add(each);
            	} else if (line.startsWith("author:")) {
            		commitBuilderRef.get().authorName = line.substring("author:".length()).trim();
            	} else if (line.startsWith("committer:")) {
            		commitBuilderRef.get().committerName = line.substring("committer:".length()).trim();
            	} else if (line.startsWith("authorEmail:")) {
            		commitBuilderRef.get().authorEmail = line.substring("authorEmail:".length()).trim();
            	} else if (line.startsWith("committerEmail:")) {
            		commitBuilderRef.get().committerEmail = line.substring("committerEmail:".length()).trim();
            	} else if (line.startsWith("committerDate:")) {
            		commitBuilderRef.get().committerDate = 
            				GitUtils.parseRawDate(line.substring("committerDate:".length()).trim());
            	} else if (line.startsWith("authorDate:")) {
            		commitBuilderRef.get().authorDate = 
            				GitUtils.parseRawDate(line.substring("authorDate:".length()).trim());
            	} else if (line.startsWith("commitDate:")) {
            		commitBuilderRef.get().commitDate = 
            				GitUtils.parseRawDate(line.substring("commitDate:".length()).trim());
            	} else if (line.startsWith("subject:")) {
            		commitBuilderRef.get().subject = line.substring("subject:".length()).trim();
            	} else if (line.trim().length() != 0 && line.contains("\t")) {
            		FileChange change;
            		if (fields.contains(Field.LINE_CHANGES)) {
            			change = parseNumStats(line);
            		} else {
                		StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                		String statusCode = tokenizer.nextToken();
                		if (statusCode.startsWith("R")) {
                			String oldPath = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
                			String newPath = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
                			change = new FileChange(oldPath, newPath, -1, -1);
                		} else if (statusCode.equals("M") || statusCode.equals("T")) {
                			String path = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
                			change = new FileChange(path, path, -1, -1);
                		} else if (statusCode.equals("D")) {
                			String oldPath = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
                			change = new FileChange(oldPath, null, -1, -1);
                		} else if (statusCode.equals("A")) {
                			String newPath = QuotedString.GIT_PATH.dequote(tokenizer.nextToken("\t"));
                			change = new FileChange(null, newPath, -1, -1);
                		} else {
                			change = null;
                		}
            		}
            		if (change != null)
            			commitBuilderRef.get().fileChanges.add(change);
            	}
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.contains("inexact rename detection was skipped") 
						|| line.contains("you may want to set your diff.renameLimit variable")) {
					logger.trace(line);
				} else {
					logger.error(line);
				}
			}
        	
        }).checkReturnCode();

        if (commitBuilderRef.get() != null)
        	consume(commitBuilderRef.get().build());
        
        return null;
    }
	
	protected abstract void consume(GitCommit commit);
	
}
