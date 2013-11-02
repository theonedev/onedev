package com.pmease.commons.git.command;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ReceiveCommand extends GitCommand<Void> {

	private InputStream input;
	
	private OutputStream output;
	
	public ReceiveCommand(File repoDir, Map<String, String> environments) {
		super(repoDir, environments);
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
		
		cmd.execute(output, new LineConsumer.ErrorLogger(), input).checkReturnCode();
		return null;
	}

}
