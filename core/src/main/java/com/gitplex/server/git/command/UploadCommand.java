package com.gitplex.server.git.command;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.util.execution.Commandline;
import com.gitplex.server.util.execution.LineConsumer;
import com.google.common.base.Preconditions;

public class UploadCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(UploadCommand.class);
	
	private InputStream input;
	
	private OutputStream output;
	
	public UploadCommand(File gitDir, Map<String, String> environments) {
		super(gitDir, environments);
	}
	
	public UploadCommand input(InputStream input) {
		this.input = input;
		return this;
	}
	
	public UploadCommand output(OutputStream output) {
		this.output = output;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(output);
		
		Commandline cmd = cmd();
		cmd.addArgs("upload-pack", "--stateless-rpc", ".");
		
		cmd.execute(output, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}, input).checkReturnCode();
		
		return null;
	}

}
