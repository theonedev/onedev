package com.pmease.gitplex.core.comment;

import javax.annotation.Nullable;

public interface InlineContextAware {
	
	@Nullable
	InlineContext getInlineContext(InlineComment comment);
	
}
