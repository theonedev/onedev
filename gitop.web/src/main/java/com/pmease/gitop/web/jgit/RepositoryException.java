package com.pmease.gitop.web.jgit;

public class RepositoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RepositoryException(String msg) {
		super(msg);
	}
	
	public RepositoryException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public RepositoryException(Throwable e) {
		super(e);
	}
}
