package com.pmease.gitplex.web.page.repository.info.code.blob.renderer.highlighter;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Strings;

public class AceHighlighter extends Behavior {
	private static final long serialVersionUID = 1L;

	private IModel<Boolean> hasLineId = Model.of(Boolean.TRUE);
	private IModel<String> selector = Model.of("");
	
	public AceHighlighter() {
	}
	
	@Override
	public void bind(Component component) {
		super.bind(component);
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(AceHighlighterReference.instance()));
		response.render(OnDomReadyHeaderItem.forScript(String.format(
				"SourceHighlighter.highlight('%s', %s)",
				Strings.isNullOrEmpty(selector.getObject()) ? "#" + component.getMarkupId(true) : selector.getObject(),
				String.valueOf(hasLineId.getObject()))));
	}

	public AceHighlighter hasLineId(boolean hasLineId) {
		this.hasLineId.setObject(hasLineId);
		return this;
	}
	
	public AceHighlighter withSelector(String selector) {
		this.selector.setObject(selector);
		return this;
	}
	
	@Override
	public void detach(Component component) {
		if (hasLineId != null) {
			hasLineId.detach();
		}
		if (selector != null) {
			selector.detach();
		}
		super.detach(component);
	}
}
