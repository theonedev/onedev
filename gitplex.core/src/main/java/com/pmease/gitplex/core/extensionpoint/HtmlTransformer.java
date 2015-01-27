package com.pmease.gitplex.core.extensionpoint;

import org.jsoup.nodes.Element;

public interface HtmlTransformer {
	Element transform(Element body);
}
