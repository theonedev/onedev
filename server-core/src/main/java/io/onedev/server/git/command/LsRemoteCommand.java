package io.onedev.server.git.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class LsRemoteCommand extends GitCommand<Map<String, String>> {

	private static final Logger logger = LoggerFactory.getLogger(LsRemoteCommand.class);
	
	private String remote;
		
	private boolean quiet;
	
	private String refs;
	
	public LsRemoteCommand() {
		super(null);
	}

	public LsRemoteCommand remote(String remote) {
		this.remote = remote;
		return this;
	}
	
	public LsRemoteCommand quiet(boolean quiet) {
		this.quiet = quiet;
		return this;
	}
	
	public LsRemoteCommand refs(String refs) {
		this.refs = refs;
		return this;
	}
	
	@Override
	public Map<String, String> call() {
		Preconditions.checkNotNull(remote, "remote has to be specified.");
		
		Commandline cmd = cmd().addArgs("ls-remote");
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
