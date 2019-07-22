package io.onedev.server.event;

import javax.annotation.Nullable;

public interface MarkdownAware {
	@Nullable String getMarkdown();
}
