package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class CloneCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CloneCommand.class);
	
	private String from;
	
	private boolean bare;
	
	private boolean shared;
	
	private boolean mirror;
	
	private boolean noCheckout;
	
	private String branch;
	
	public CloneCommand(File gitDir) {
		super(gitDir);
	}

	public CloneCommand from(String from) {
		this.from = from;
		return this;
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
	
	public CloneCommand branch(String branch) {
		this.branch = branch;
		return this;
	}
	
	@Override
	public Void call(Logger logger) {
		Preconditions.checkNotNull(from, "from has to be specified.");
		
		Commandline cmd = cmd().addArgs("clone");
		if (bare)
			cmd.addArgs("--bare");
		if (mirror)
			cmd.addArgs("--mirror");
		if (shared) 
			cmd.addArgs("--shared");
		if (noCheckout) 
			cmd.addArgs("--no-checkout");
		if (branch != null)
			cmd.addArgs("-b", branch);
		
		cmd.addArgs(from);
		cmd.addArgs(".");
		
		Logger effectiveLogger = logger!=null?logger:CloneCommand.logger;
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				effectiveLogger.trace(line);
			}
			
		}, new LineConsumer(){

			@Override
			public void consume(String line) {
				if (line.startsWith("Cloning into ") || line.equals("done."))
					effectiveLogger.trace(line);
				else if (line.contains("You appear to have cloned an empty repository"))
					effectiveLogger.warn(line);
				else
					effectiveLogger.error(line);
			}
			
		}, logger).checkReturnCode();
		
		return null;
	}

}
