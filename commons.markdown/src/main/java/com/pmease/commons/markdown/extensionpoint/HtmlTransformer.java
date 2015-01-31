package com.pmease.commons.markdown.extensionpoint;

import org.jsoup.nodes.Element;

public interface HtmlTransformer {
	Element transform(Element body);
}
