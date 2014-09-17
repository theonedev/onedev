package com.pmease.gitplex.core.comment;

import java.util.List;
import java.util.Map;

public interface InlineCommentSupport {
	
	Map<Integer, List<InlineComment>> getOldComments();
	
	Map<Integer, List<InlineComment>> getNewComments();

	InlineComment addComment(String commit, String file, int line, String content);
	
	InlineComment getConcernedComment();
}
