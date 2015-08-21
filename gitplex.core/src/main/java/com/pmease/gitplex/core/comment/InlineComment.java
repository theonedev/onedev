package com.pmease.gitplex.core.comment;

import com.pmease.commons.git.BlobIdent;

public interface InlineComment extends Comment {

	BlobIdent getBlobIdent();
	
	int getLine();
	
}