package com.gitplex.server.core.event;

import javax.annotation.Nullable;

public interface MarkdownAware {
	@Nullable String getMarkdown();
}
