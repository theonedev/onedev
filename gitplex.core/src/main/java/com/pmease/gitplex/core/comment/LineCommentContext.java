package com.pmease.gitplex.core.comment;

import java.util.Map;

public interface LineCommentContext {
	
	Map<Integer, LineComment> getOldComments();
	
	Map<Integer, LineComment> getNewComments();

	LineComment addComment(String commit, String file, int line, String content);
	
	LineComment getConcernedComment();
}
