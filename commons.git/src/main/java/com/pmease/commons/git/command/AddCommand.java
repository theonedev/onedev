package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.execution.Commandline;

public class AddCommand extends GitCommand<Void> {

	private List<String> paths = new ArrayList<String>();
	
	public AddCommand(File repoDir) {
		super(repoDir);
	}
	
	public AddCommand addPath(String path) {
		paths.add(path);
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("add");
		
		for (String path: paths) {
			cmd.addArgs(path);
		}
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
