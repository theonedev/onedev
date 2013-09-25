package com.pmease.commons.git;

import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class UploadCommand extends GitCommand<Git> {

	private InputStream input;
	
	private OutputStream output;
	
	public UploadCommand(Git git) {
		super(git);
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
	public Git call() {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(output);
		
		Commandline cmd = git().cmd();
		cmd.addArgs("upload-pack", "--stateless-rpc", ".");
		
		cmd.execute(output, new LineConsumer.ErrorLogger(), input).checkReturnCode();
		return git();
	}

}
