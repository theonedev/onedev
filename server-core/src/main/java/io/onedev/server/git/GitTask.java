package io.onedev.server.git;

import io.onedev.commons.utils.command.Commandline;

public interface GitTask<T> {

	T call(Commandline git);
	
}
