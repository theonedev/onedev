package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AddCommand extends GitCommand<Void> {

	private List<String> paths = new ArrayList<String>();
	
	public AddCommand(File repoDir) {
		super(repoDir);
	}
	
	public AddCommand addPaths(String... paths) {
		for (String path: paths)
			this.paths.add(path);
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("add");
		
		for (String path: paths) {
			cmd.addArgs(path);
		}
		
		cmd.execute(debugLogger, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("warning: "))
					warn(line.substring("warning: ".length()));
				else if (line.startsWith("The file will have its original line endings"))
					warn(line);
				else
					error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
