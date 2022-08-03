package io.onedev.server.git.command;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Nullable;

import org.eclipse.jgit.util.QuotedString;

import io.onedev.agent.Agent;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.git.config.GitConfig;

public abstract class GitCommand<V> {

	private static final String MIN_VERSION = "2.11.1";
	
	protected final File gitDir;
	
	private final Map<String, String> environments;
	
	public GitCommand(@Nullable File gitDir, @Nullable Map<String, String> environments) {
		this.gitDir = gitDir;
		this.environments = environments;
		
		if (gitDir != null && !gitDir.exists())
		    FileUtils.createDir(gitDir);
	}

	public GitCommand(File gitDir) {
		this(gitDir, null);
	}
	
	/**
	 * Check if there are any errors with git command line. 
	 *
	 * @return
	 * 			error message if failed to check git command line, 
	 * 			or <tt>null</tt> otherwise
	 * 			
	 */
	public static String checkError(String gitExe) {
		return Agent.checkGitError(gitExe, MIN_VERSION);
	}
	
	public Commandline cmd() {
		String gitExe = getGitExe();
		Commandline cmd = new Commandline(gitExe);
		if (gitDir != null)
			cmd.workingDir(gitDir);
		if (environments != null)
			cmd.environments(environments);
		return cmd;
	}
	
	public static FileChange parseNumStats(String line) {
		FileChange change;
		StringTokenizer tokenizer = new StringTokenizer(line, "\t");
		String additionsToken = tokenizer.nextToken();
		int additions = additionsToken.equals("-")?-1:Integer.parseInt(additionsToken);
		String deletionsToken = tokenizer.nextToken();
		int deletions = deletionsToken.equals("-")?-1:Integer.parseInt(deletionsToken);
		
		String path = tokenizer.nextToken();
		int renameSignIndex = path.indexOf(" => ");
		if (renameSignIndex != -1) {
			int leftBraceIndex = path.indexOf("{");
			int rightBraceIndex = path.indexOf("}");
			if (leftBraceIndex != -1 && rightBraceIndex != -1 && leftBraceIndex<renameSignIndex
					&& rightBraceIndex>renameSignIndex) {
				String leftCommon = path.substring(0, leftBraceIndex);
				String rightCommon = path.substring(rightBraceIndex+1);
				String oldPath = leftCommon + path.substring(leftBraceIndex+1, renameSignIndex) 
						+ rightCommon;
				String newPath = leftCommon + path.substring(renameSignIndex+4, rightBraceIndex) 
						+ rightCommon;
    			change = new FileChange(oldPath, newPath, additions, deletions);
			} else {
				String oldPath = QuotedString.GIT_PATH.dequote(path.substring(0, renameSignIndex));
				String newPath = QuotedString.GIT_PATH.dequote(path.substring(renameSignIndex+4));
    			change = new FileChange(oldPath, newPath, additions, deletions);
			}
		} else {
			path = QuotedString.GIT_PATH.dequote(path);
			change = new FileChange(null, path, additions, deletions);
		}            			
		return change;
	}
	
	protected String getGitExe() {
		return AppLoader.getInstance(GitConfig.class).getExecutable();
	}
	
	public abstract V call();
	
}
