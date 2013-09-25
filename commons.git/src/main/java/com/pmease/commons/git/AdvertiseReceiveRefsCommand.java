package com.pmease.commons.git;

import java.io.OutputStream;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AdvertiseReceiveRefsCommand extends GitCommand<Git> {

	private OutputStream output;
	
	public AdvertiseReceiveRefsCommand(Git git) {
		super(git);
	}

	public AdvertiseReceiveRefsCommand output(OutputStream output) {
		this.output = output;
		return this;
	}
	
	@Override
	public Git call() {
		Preconditions.checkNotNull(output);
		
		Commandline cmd = git().cmd();
		cmd.addArgs("receive-pack", "--stateless-rpc", "--advertise-refs", ".");
		cmd.execute(output, new LineConsumer.ErrorLogger(), null).checkReturnCode();
		return git();
	}

}
