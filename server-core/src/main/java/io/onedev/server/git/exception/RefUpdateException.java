package io.onedev.server.git.exception;

import org.eclipse.jgit.lib.RefUpdate;

public class RefUpdateException extends GitException {

	private static final long serialVersionUID = 1L;
	
	private final RefUpdate.Result result;
	
	public RefUpdateException(RefUpdate.Result result) {
		super(result.name());
		this.result = result;
	}

	public RefUpdate.Result getResult() {
		return result;
	}
	
}
