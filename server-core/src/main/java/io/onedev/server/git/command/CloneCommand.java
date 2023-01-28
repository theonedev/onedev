package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class CloneCommand {

	private static final Logger logger = LoggerFactory.getLogger(CloneCommand.class);
	
	private final File workingDir;
	
	private final String remotUrl;
	
	private boolean bare;
	
	private boolean shared;
	
	private boolean mirror;
	
	private boolean noCheckout;
	
	private boolean noLfs;
	
	private String branch;
	
	public CloneCommand(File workingDir, String remotUrl) {
		this.workingDir = workingDir;
		this.remotUrl = remotUrl;
	}

	public CloneCommand bare(boolean bare) {
		this.bare = bare;
		return this;
	}
	
	public CloneCommand mirror(boolean mirror) {
		this.mirror = mirror;
		return this;
	}
	
	public CloneCommand shared(boolean shared) {
		this.shared = shared;
		return this;
	}
	
	public CloneCommand noCheckout(boolean noCheckout) {
		this.noCheckout = noCheckout;
		return this;
	}
	
	public CloneCommand noLfs(boolean noLfs) {
		this.noLfs = noLfs;
		return this;
	}
	
	public CloneCommand branch(String branch) {
		this.branch = branch;
		return this;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public void run() {
		Commandline git = newGit().workingDir(workingDir).addArgs("clone");
		if (bare)
			git.addArgs("--bare");
		if (mirror)
			git.addArgs("--mirror");
		if (shared) 
			git.addArgs("--shared");
		if (noCheckout) 
			git.addArgs("--no-checkout");
		if (branch != null)
			git.addArgs("-b", branch);
		
		if (noLfs)
			git.environments().put("GIT_LFS_SKIP_SMUDGE", "1");
		
		git.addArgs(remotUrl);
		git.addArgs(".");
		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.trace(line);
			}
			
		}, new LineConsumer(){

			@Override
			public void consume(String line) {
				if (line.startsWith("Cloning into ") || line.equals("done."))
					logger.trace(line);
				else if (line.contains("You appear to have cloned an empty repository"))
					logger.warn(line);
				else
					logger.error(line);
			}
			
		}).checkReturnCode();
	}

}
