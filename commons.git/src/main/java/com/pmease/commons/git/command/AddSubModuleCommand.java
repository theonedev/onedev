package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

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
		cmd.execute(debugLogger, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("warning: "))
					warn(line.substring("warning: ".length()));
				else if (line.startsWith("The file will have its original line endings"))
					warn(line);
				else if (line.startsWith("The file will have its original line endings in your working directory"))
					warn(line);
				else if (line.startsWith("Cloning into"))
					info(line);
				else if (line.equals("done."))
					info(line);
				else
					error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
