package io.onedev.server.markdown;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Project;

@ExtensionPoint
public interface MarkdownProcessor {
	
	void process(Document rendered, @Nullable Project project, @Nullable Object context);
	
}
