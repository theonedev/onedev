package com.pmease.gitplex.web.exception;

@SuppressWarnings("serial")
public class AccessDeniedException extends RuntimeException {

	public AccessDeniedException() {
	}
	
	public AccessDeniedException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public AccessDeniedException(String msg) {
		super(msg);
	}
	
	public AccessDeniedException(Throwable e) {
		super(e);
	}
}
