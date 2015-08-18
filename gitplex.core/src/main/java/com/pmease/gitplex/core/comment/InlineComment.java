package com.pmease.gitplex.core.comment;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.diff.AroundContext;

public interface InlineComment extends Comment {

	BlobIdent getBlobIdent();
	
	int getLine();
	
	AroundContext getContext();
}
