package io.onedev.server.git.exception;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;
import org.eclipse.jgit.lib.RefUpdate;

public class RefUpdateException extends ExplicitException {

	@Serial
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
