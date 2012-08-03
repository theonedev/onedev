package com.pmease.commons.wicket.dialog;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class Dialog extends Panel {

	private static final long serialVersionUID = 1L;
	
	private final String title;
	
	private String width = "600px";
	
	public Dialog(String id, String title) {
		super(id);
		this.title= title;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AttributeModifier("class", "modal hide"));
		add(new Label("title", title));
		
		add(newContent("content"));
	}
	
	/**
	 * Close this dialog.
	 * @param target
	 */
	public void close(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("$('#%s').modal('hide');", getMarkupId()));
	}
	
	/**
	 * Set width of this dialog. Default value is 600px.
	 *  
	 * @param width 
	 * 			Specify width of the dialog in css width format, for instance: 10em, 600px, auto, 50%
	 * @return
	 */
	public Dialog width(String width) {
		this.width = width;
		return this;
	}
	
	public String width() {
		return width;
	}
	
	protected abstract Component newContent(String id);
}
