package com.pmease.gitplex.core.comment;

public interface LineComment extends Comment {

	String getCommit();
	
	int getLine();
}
