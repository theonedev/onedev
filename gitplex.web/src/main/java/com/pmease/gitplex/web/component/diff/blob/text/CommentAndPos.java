package com.pmease.gitplex.web.component.diff.blob.text;

import java.io.Serializable;

import com.pmease.gitplex.core.entity.Comment;

class CommentAndPos implements Serializable {

	private static final long serialVersionUID = 1L;

	Comment comment;
	
	int oldLineNo;
	
	int newLineNo;
	
}
