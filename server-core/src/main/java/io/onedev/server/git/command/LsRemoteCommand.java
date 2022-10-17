package io.onedev.server.git.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class LsRemoteCommand {

	private static final Logger logger = LoggerFactory.getLogger(LsRemoteCommand.class);
	
	private final String remote;
		
	private boolean quiet;
	
	private String refs;
	
	public LsRemoteCommand(String remote) {
		this.remote = remote;
	}

	public LsRemoteCommand quiet(boolean quiet) {
		this.quiet = quiet;
		return this;
	}
	
	public LsRemoteCommand refs(String refs) {
		this.refs = refs;
		return this;
	}

	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public Map<String, String> run() {
		Commandline cmd = newGit().addArgs("ls-remote");
		if (quiet)
			cmd.addArgs("--quiet");
		
		cmd.addArgs(remote);
		
		if (refs != null)
			cmd.addArgs(refs);
		
		Map<String, String> refCommits = new HashMap<>();
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				refCommits.put(StringUtils.substringAfter(line, "\t"), StringUtils.substringBefore(line, "\t"));
			}
			
		}, new LineConsumer(){

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return refCommits;
	}

}
