package com.gitplex.server.web.component.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

@SuppressWarnings("serial")
public abstract class ModalLink extends AjaxLink<Void> {

	private ModalPanel modal;
	
	public ModalLink(String id) {
		super(id);
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		// if modal has not been created, or has been removed from page 
		// when the same page instance is refreshed 
		if (modal == null || modal.getParent() == null) {
			modal = new ModalPanel(target) {
	
				@Override
				protected Component newContent(String id) {
					return ModalLink.this.newContent(id);
				}

				@Override
				protected void onClosed(AjaxRequestTarget target) {
					super.onClosed(target);
					modal = null;
				}
				
			};
		}
	}

	protected void close(AjaxRequestTarget target) {
		if (modal != null)
			modal.close(target);
	}
	
	protected abstract Component newContent(String id);
}
