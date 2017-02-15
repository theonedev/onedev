package com.gitplex.server.git.exception;

public class GitObjectNotFoundException extends GitException {

	private static final long serialVersionUID = 1L;

	public GitObjectNotFoundException(String message) {
		super(message);
	}

}
