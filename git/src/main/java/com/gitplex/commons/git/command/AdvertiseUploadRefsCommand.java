package com.gitplex.commons.git.command;

import java.io.File;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.commons.util.execution.Commandline;
import com.gitplex.commons.util.execution.LineConsumer;
import com.google.common.base.Preconditions;

public class AdvertiseUploadRefsCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(AdvertiseUploadRefsCommand.class);
	
	private OutputStream output;
	
	public AdvertiseUploadRefsCommand(File repoDir) {
		super(repoDir);
	}

	public AdvertiseUploadRefsCommand output(OutputStream output) {
		this.output = output;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(output);
		
		Commandline cmd = cmd();
		cmd.addArgs("upload-pack", "--stateless-rpc", "--advertise-refs", ".");
		cmd.execute(output, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		return null;
	}

}
