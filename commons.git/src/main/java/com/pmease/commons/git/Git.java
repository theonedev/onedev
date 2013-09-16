package com.pmease.commons.git;

import java.io.File;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class Git {
	
	private static final String GIT_EXE = "git";
	
	private static final String MIN_VERSION = "1.0.0";
	
	private final File repoDir;
	
	public Git(final File repoDir) {
		this.repoDir = repoDir;
	}
	
	public InitCommand init() {
		return new InitCommand(this);
	}
	
	public AddCommand add() {
		return new AddCommand(this);
	}
	
	public CommitCommand commit() {
		return new CommitCommand(this);
	}
	
	public FindChangedFilesCommand findChangedFiles() {
		return new FindChangedFilesCommand(this);
	}
	
	public CheckAncestorCommand checkAncestor() {
		return new CheckAncestorCommand(this);
	}
	
	public CalcMergeBaseCommand calcMergeBase() {
		return new CalcMergeBaseCommand(this);
	}
	
	/**
	 * Check if there are any errors with git command line. 
	 *
	 * @return
	 * 			error message if failed to check git command line, 
	 * 			or <code>null</code> otherwise
	 * 			
	 */
	public static String checkError() {
		try {
			final String[] version = new String[]{null};
			
			new Commandline(GIT_EXE).addArgs("--version").execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.trim().length() != 0)
						version[0] = line.trim();
				}
				
			}, new LineConsumer.ErrorLogger()).checkReturnCode();
	
			if (version[0] == null)
				throw new RuntimeException("Unable to determine git version.");
			
			GitVersion gitVersion = new GitVersion(version[0]);
			
			if (gitVersion.isOlderThan(new GitVersion(MIN_VERSION)))
				throw new RuntimeException("Git version should be at least " + MIN_VERSION);
			
			return null;
			
		} catch (Exception e) {
			return ExceptionUtils.getMessage(e);
		}
	}
	
	public Commandline cmd() {
		return new Commandline(GIT_EXE).workingDir(repoDir);
	}

}
