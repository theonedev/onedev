package io.onedev.server.util.markdown;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;

import io.onedev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface MarkdownProcessor {
	
	void process(Document rendered, @Nullable Object context);
	
}
