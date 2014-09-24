package com.pmease.gitplex.core.comment;

public interface InlineComment extends Comment {

	int CONTEXT_SIZE = 10;
	
	String getCommitHash();
	
	String getOldCommitHash();
	
	String getNewCommitHash();
	
	int getLine();
	
	String getFile();
	
	InlineContext getContext();
}
