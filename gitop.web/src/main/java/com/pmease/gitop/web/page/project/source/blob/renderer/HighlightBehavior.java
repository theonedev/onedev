package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class HighlightBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	private static final ResourceReference HIGHLIGHT_JS = new JavaScriptResourceReference(HighlightBehavior.class, "res/highlight/highlight.pack.js");
	private static final ResourceReference HIGHLIGHT_CSS = new CssResourceReference(HighlightBehavior.class, "res/highlight/styles/default.css");
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptReferenceHeaderItem.forReference(HIGHLIGHT_JS));
		response.render(CssReferenceHeaderItem.forReference(HIGHLIGHT_CSS));
		response.render(OnDomReadyHeaderItem.forScript("hljs.tabReplace = '    ';\n"
				+ "$('#" + 
				component.getMarkupId(true) 
				+ "').each(function(i, e) {hljs.highlightBlock(e, null, false)});"));
	}
}
