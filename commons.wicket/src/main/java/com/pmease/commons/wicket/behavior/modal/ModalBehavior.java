package com.pmease.commons.wicket.behavior.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class ModalBehavior extends AbstractDefaultAjaxBehavior {

	private final ModalPanel modalPanel;
	
	public ModalBehavior(ModalPanel modalPanel) {
		this.modalPanel = modalPanel;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		modalPanel.load(target);

		String script = String.format("modalLoaded('%s', '%s')", modalPanel.getMarkupId(), modalPanel.getWidth());
		
		target.appendJavaScript(script);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new ModalResourceReference()));
		String script = String.format(
				"setupModal('%s', '%s', '%s', %s)", 
				getComponent().getMarkupId(), modalPanel.getMarkupId(), 
				modalPanel.getWidth(), getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
		
}
