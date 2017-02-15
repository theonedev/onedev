package com.gitplex.server.git.exception;

public class GitObjectAlreadyExistsException extends GitException {

	private static final long serialVersionUID = 1L;

	public GitObjectAlreadyExistsException(String message) {
		super(message);
	}

}
