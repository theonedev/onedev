package com.gitplex.server.git.command;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.utils.command.Commandline;
import com.gitplex.utils.command.LineConsumer;
import com.google.common.base.Preconditions;

public class ReceiveCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveCommand.class);
	
	private InputStream input;
	
	private OutputStream output;
	
	public ReceiveCommand(File gitDir, Map<String, String> environments) {
		super(gitDir, environments);
	}
	
	public ReceiveCommand input(InputStream input) {
		this.input = input;
		return this;
	}
	
	public ReceiveCommand output(OutputStream output) {
		this.output = output;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(output);
		
		Commandline cmd = cmd();
		cmd.addArgs("receive-pack", "--stateless-rpc", ".");
		
		cmd.execute(output, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}, input).checkReturnCode();
		
		return null;
	}

}
