package com.gitplex.server.util.markdown;

import org.jsoup.nodes.Element;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface HtmlTransformer {
	void transform(Element body);
}
