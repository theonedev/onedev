package com.pmease.commons.wicket.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;


@SuppressWarnings("serial")
public abstract class ModalBehavior extends AbstractDefaultAjaxBehavior {

	public ModalBehavior() {
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		ModalPanel modal = newModal(getModalId());
		getComponent().getParent().addOrReplace(modal);
		modal.setOutputMarkupId(true);
		modal.setMarkupId(getModalMarkupId());
		modal.setMarkup(Markup.of(String.format("<div wicket:id='%s'></div>", getModalMarkupId())));
		
		target.add(modal);
		
		String script = String.format(
				"modalLoaded('%s', '%s', '%s')", 
				getComponent().getMarkupId(), getModalMarkupId(), modal.width());
		target.appendJavaScript(script);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(ModalBehavior.class, "modal.js")));
	}
	
	private String getModalMarkupId() {
		return getComponent().getMarkupId() + "-dialog";
	}
	
	private String getModalId() {
		return getComponent().getId() + "-dialog";
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		String script = String.format(
				"openModal('%s', '%s', %s)", 
				getComponent().getMarkupId(), getModalMarkupId(), getCallbackFunction());
		
		tag.put("onclick", script);
		
		if (tag.getName().equals("a"))
			tag.put("href", "#");
	}
	
	protected abstract ModalPanel newModal(String id);
}
