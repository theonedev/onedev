package com.pmease.commons.git.command;

import java.io.File;
import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AdvertiseReceiveRefsCommand extends GitCommand<Void> {

	private OutputStream output;
	
	public AdvertiseReceiveRefsCommand(File repoDir) {
		super(repoDir);
	}

	public AdvertiseReceiveRefsCommand output(OutputStream output) {
		this.output = output;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(output);
		
		Commandline cmd = cmd();
		cmd.addArgs("receive-pack", "--stateless-rpc", "--advertise-refs", ".");
		cmd.execute(output, new LineConsumer.ErrorLogger(), null).checkReturnCode();
		return null;
	}

}
