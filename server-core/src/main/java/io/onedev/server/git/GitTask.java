package io.onedev.server.git;

import io.onedev.commons.utils.command.Commandline;

import java.io.IOException;

public interface GitTask<T> {

	T call(Commandline git) throws IOException;
	
}
