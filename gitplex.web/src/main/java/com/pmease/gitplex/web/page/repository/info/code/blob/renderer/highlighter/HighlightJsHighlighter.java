package com.pmease.gitplex.web.page.repository.info.code.blob.renderer.highlighter;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class HighlightJsHighlighter extends Behavior {
	private static final long serialVersionUID = 1L;

	public HighlightJsHighlighter() {
	}
	
	@Override
	public void bind(Component component) {
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptReferenceHeaderItem.forReference(HighlightJsResourceReference.getInstance()));
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("SourceHighLight.highlight($('#%s'));\n",
						component.getMarkupId(true))));
	}
	
	@Override
	public void detach(Component component) {
		super.detach(component);
	}
}
