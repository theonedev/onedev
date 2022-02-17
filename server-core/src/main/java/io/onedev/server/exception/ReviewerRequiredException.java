package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.User;

public class ReviewerRequiredException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public ReviewerRequiredException(User user) {
		super("Reviewer '" + user.getDisplayName() + "' is required");
	}

}
