package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.execution.Commandline;

public class RemoveCommand extends GitCommand<Void> {

	private List<String> paths = new ArrayList<String>();
	
	public RemoveCommand(File repoDir) {
		super(repoDir);
	}
	
	public RemoveCommand removePath(String path) {
		paths.add(path);
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("rm");
		
		for (String path: paths) {
			cmd.addArgs(path);
		}
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
