package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface InlineCommentSupport extends Serializable {
	
	Map<Integer, List<InlineComment>> getOldComments();
	
	Map<Integer, List<InlineComment>> getNewComments();

	InlineComment addComment(String commit, String file, int line, String content);
	
	InlineComment getConcernedComment();
}
