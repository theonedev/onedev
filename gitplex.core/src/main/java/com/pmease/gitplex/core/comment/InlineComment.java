package com.pmease.gitplex.core.comment;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.util.diff.AroundContext;

public interface InlineComment extends Comment {

	int CONTEXT_SIZE = 7;
	
	BlobIdent getBlobInfo();
	
	int getLine();
	
	AroundContext getContext();
}
