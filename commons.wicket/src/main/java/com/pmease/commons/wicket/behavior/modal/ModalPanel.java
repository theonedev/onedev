package com.pmease.commons.wicket.behavior.modal;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class ModalPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private String width;
	
	public ModalPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		add(new WebMarkupContainer("content")
				.add(AttributeModifier.replace("class", "loading"))
				.setOutputMarkupId(true));
	}
	
	/**
	 * Close this modal.
	 * @param target
	 */
	public void close(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("hideModal('%s');", getMarkupId()));
	}
	
	public void load(AjaxRequestTarget target) {
		Component content = newContent("content");
		content.add(AttributeModifier.append("class", " content"));
		replace(content);
		target.add(content);
	}
	
	public String getWidth() {
		return width;
	}

	/**
	 * Optionally specify width of the modal panel. For instance: 500px, 50%, etc. 
	 * @param width
	 */
	public ModalPanel setWidth(String width) {
		this.width = width;
		return this;
	}

	protected abstract Component newContent(String id);
}
