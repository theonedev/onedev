package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.util.diff.AroundContext;

public interface InlineCommentSupport extends Serializable {
	
	Map<Integer, List<InlineComment>> getOldComments();
	
	Map<Integer, List<InlineComment>> getNewComments();

	InlineComment addComment(BlobInfo commentAt, BlobInfo compareWith, 
			AroundContext commentContext, int line, String content);
	
	InlineComment getConcernedComment();

}
