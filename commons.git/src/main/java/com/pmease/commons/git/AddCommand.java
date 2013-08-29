package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.execution.Commandline;

public class AddCommand extends GitCommand<Git> {

	private List<String> paths = new ArrayList<String>();
	
	public AddCommand(Git git) {
		super(git);
	}
	
	public AddCommand addPath(String path) {
		paths.add(path);
		return this;
	}

	@Override
	public Git call() {
		Commandline cmd = git().cmd().addArgs("add");
		
		for (String path: paths) {
			cmd.addArgs(path);
		}
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
