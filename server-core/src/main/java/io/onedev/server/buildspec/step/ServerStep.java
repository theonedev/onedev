package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.HashSet;

import io.onedev.k8shelper.Executable;
import io.onedev.k8shelper.ServerExecutable;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.patternset.PatternSet;

public abstract class ServerStep extends Step {

	private static final long serialVersionUID = 1L;

	@Override
	public Executable getExecutable(Build build, ParamCombination paramCombination) {
		PatternSet files = getFiles();
		return new ServerExecutable(this, files.getIncludes(), files.getExcludes());
	}

	protected PatternSet getFiles() {
		return new PatternSet(new HashSet<>(), new HashSet<>());
	}
	
	public abstract void run(Build build, File filesDir, SimpleLogger logger);
	
}
