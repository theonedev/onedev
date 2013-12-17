package com.pmease.gitop.web.page.project.source.blob.renderer.highlighter;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AceHighlighter extends Behavior {
	private static final long serialVersionUID = 1L;

	private final IModel<String> modeModel;
	
	public AceHighlighter(IModel<String> mode) {
		this.modeModel = mode;
	}
	
	@SuppressWarnings("serial")
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String mode = modeModel.getObject();
				return "language-" + (mode == null ? "text" : mode);
			}
			
		}));
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forUrl("assets/js/vendor/ace-noconflict/ace.js"));
		response.render(JavaScriptHeaderItem.forUrl("assets/js/vendor/ace-noconflict/ext-static_highlight.js"));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(AceHighlighter.class, "acehighlighter.js")));
		response.render(OnDomReadyHeaderItem.forScript("SourceHighlighter.highlight('#" + component.getMarkupId(true) + "');"));
	}
	
	@Override
	public void detach(Component component) {
		super.detach(component);
		
		if (modeModel != null) {
			modeModel.detach();
		}
	}
}
