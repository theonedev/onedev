package com.pmease.gitplex.core.comment;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.diff.AroundContext;

public interface InlineCommentSupport extends Serializable {
	
	Map<Integer, List<InlineComment>> getComments(BlobIdent blobIdent);
	
	InlineComment addComment(BlobIdent commentAt, BlobIdent compareWith, 
			AroundContext commentContext, int line, String content);
	
	InlineComment getConcernedComment();

	InlineComment loadComment(Long commentId);
}
