package com.pmease.gitplex.core.comment;

public interface InlineContextAware {
	
	InlineContext getInlineContext(InlineComment comment);
	
}
