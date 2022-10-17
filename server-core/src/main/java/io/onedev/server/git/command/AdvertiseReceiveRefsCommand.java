package io.onedev.server.git.command;

import java.io.File;
import java.io.OutputStream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class AdvertiseReceiveRefsCommand {

	private static final Logger logger = LoggerFactory.getLogger(AdvertiseReceiveRefsCommand.class);
	
	private final File workingDir;
	
	private final OutputStream output;
	
	private String protocol;
	
	public AdvertiseReceiveRefsCommand(File workingDir, OutputStream output) {
		this.workingDir = workingDir;
		this.output = output;
	}

	public AdvertiseReceiveRefsCommand protocol(@Nullable String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public void run() {
		Commandline git = newGit().workingDir(workingDir);
		
		if (protocol != null)
			git.environments().put("GIT_PROTOCOL", protocol);
		
		git.addArgs("receive-pack", "--stateless-rpc", "--advertise-refs", ".");
		git.execute(output, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
	}

}
