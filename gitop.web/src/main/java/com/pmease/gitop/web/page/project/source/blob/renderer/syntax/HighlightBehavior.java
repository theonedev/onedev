package com.pmease.gitop.web.page.project.source.blob.renderer.syntax;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

public class HighlightBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	private final IModel<String> langModel;
	
	public HighlightBehavior(IModel<String> lang) {
		this.langModel = lang;
	}
	
	@Override
	public void bind(Component component) {
		component.add(AttributeModifier.replace("data-lang", langModel));
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptReferenceHeaderItem.forReference(HighlightJavaScriptResourceReference.getInstance()));
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("SourceHighLight.highlight($('#%s'));\n",
						component.getMarkupId(true))));

//		response.render(JavaScriptReferenceHeaderItem.forReference(new GoogleCodePrettifyResourceReference()));
//		response.render(OnDomReadyHeaderItem.forScript("prettyPrint()"));
	}
	
	@Override
	public void detach(Component component) {
		super.detach(component);
		
		if (langModel != null) {
			langModel.detach();
		}
	}
}
