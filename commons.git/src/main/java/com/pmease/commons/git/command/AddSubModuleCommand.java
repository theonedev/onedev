package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class AddSubModuleCommand extends GitCommand<Void> {

	private String url;
	
	private String path;
	
	public AddSubModuleCommand(File repoDir) {
		super(repoDir);
	}
	
	public AddSubModuleCommand url(String url) {
		this.url = url;
		return this;
	}
	
	public AddSubModuleCommand path(String path) {
		this.path = path;
		return this;
	}

	@Override
	public Void call() {
		Preconditions.checkNotNull(url, "url should be specified.");
		Preconditions.checkNotNull(path, "path should be specified.");
		
		Commandline cmd = cmd().addArgs("submodule", "add", url, path);
		cmd.execute(debugLogger, errorLogger).checkReturnCode();
		
		return null;
	}

}
