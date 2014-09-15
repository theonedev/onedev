package com.pmease.gitplex.core.comment;

public interface InlineComment extends Comment {

	int CONTEXT_SIZE = 10;
	
	String getCommit();
	
	String getOldCommit();
	
	String getNewCommit();
	
	int getLine();
	
	String getFile();
	
	InlineContext getContext();
}
