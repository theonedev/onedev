package com.pmease.gitplex.core.comment;

import java.util.Map;

public interface InlineCommentSupport {
	
	Map<Integer, InlineComment> getOldComments();
	
	Map<Integer, InlineComment> getNewComments();

	InlineComment addComment(String commit, String file, int line, String content);
	
	InlineComment getConcernedComment();
}
