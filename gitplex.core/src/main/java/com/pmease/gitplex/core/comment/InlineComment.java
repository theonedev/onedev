package com.pmease.gitplex.core.comment;

import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.util.diff.AroundContext;

public interface InlineComment extends Comment {

	int CONTEXT_SIZE = 10;
	
	BlobInfo getBlobInfo();
	
	int getLine();
	
	AroundContext getContext();
}
