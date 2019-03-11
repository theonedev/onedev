package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.Project;

@ExtensionPoint
public interface MarkdownProcessor {
	
	void process(Project project, Document rendered, @Nullable Object context);
	
}
