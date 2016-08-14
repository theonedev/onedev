package com.pmease.gitplex.core.event.pullrequest;

import javax.annotation.Nullable;

public interface MarkdownAware {
	@Nullable String getMarkdown();
}
