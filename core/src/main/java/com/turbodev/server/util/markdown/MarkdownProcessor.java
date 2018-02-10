package com.turbodev.server.util.markdown;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;

import com.turbodev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface MarkdownProcessor {
	
	void process(Document rendered, @Nullable Object context);
	
}
