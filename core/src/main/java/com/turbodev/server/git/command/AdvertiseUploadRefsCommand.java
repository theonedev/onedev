package com.turbodev.server.git.command;

import java.io.File;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turbodev.utils.command.Commandline;
import com.turbodev.utils.command.LineConsumer;
import com.google.common.base.Preconditions;

public class AdvertiseUploadRefsCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(AdvertiseUploadRefsCommand.class);
	
	private OutputStream output;
	
	public AdvertiseUploadRefsCommand(File gitDir) {
		super(gitDir);
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
