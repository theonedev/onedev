package com.pmease.commons.wicket.behavior.modal;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.pmease.commons.wicket.asset.Asset;

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
		add(new AttributeModifier("class", "modal hide"));
		
		Fragment content = new Fragment("content", "loadingFrag", ModalPanel.this);
		content.setOutputMarkupId(true);
		content.add(new Image("loading", new PackageResourceReference(Asset.class, "image/ajax-loading-bar.gif")));
		add(content);
	}
	
	/**
	 * Close this dialog.
	 * @param target
	 */
	public void close(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("hideModal('%s');", getMarkupId()));
	}
	
	public void load(AjaxRequestTarget target) {
		Component content = newContent("content");
		content.add(AttributeModifier.append("class", "loaded"));
		replace(content);
		target.add(content);
	}
	
	public String width() {
		return width;
	}

	/**
	 * Optionally specify width of the modal panel. For instance: 500px, 50%, etc. 
	 * @param width
	 */
	public ModalPanel width(String width) {
		this.width = width;
		return this;
	}

	protected abstract Component newContent(String id);
}
