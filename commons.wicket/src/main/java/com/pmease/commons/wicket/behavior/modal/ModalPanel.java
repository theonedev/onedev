package com.pmease.commons.wicket.behavior.modal;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * This panel can be used together with ModalBehavior, or can be used separately.
 * If used separately, the modal panel will be shown up immediately.  
 * 
 * @see ModalBehavior
 * @author robin
 *
 */
public abstract class ModalPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private String width;
	
	boolean showImmediately = true;
	
	public ModalPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new WebMarkupContainer("content")
				.add(AttributeModifier.append("class", "loading"))
				.setOutputMarkupId(true));
	}
	
	@Override
	protected void onBeforeRender() {
		if (showImmediately)
			replace(newContent("content").add(AttributeModifier.append("class", "content")));
		
		super.onBeforeRender();
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("style", "display: none;");
	}

	/**
	 * Close this modal.
	 *
	 * @param target
	 * 			Wicket ajax request target 
	 */
	public void close(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("hideModal('%s');", getMarkupId()));
	}
	
	void load(AjaxRequestTarget target) {
		Component content = newContent("content");
		content.add(AttributeModifier.append("class", " content"));
		replace(content);
		target.add(content);

		String script = String.format("modalLoaded('%s')", getMarkupId());
		target.appendJavaScript(script);
	}
	
	public String getWidth() {
		return width;
	}

	/**
	 * Optionally specify width of the modal panel.  
	 * 
	 * @param width
	 * 			width of the modal, for instance: 500px, 50%, etc
	 */
	public ModalPanel setWidth(String width) {
		this.width = width;
		return this;
	}

	protected abstract Component newContent(String id);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(ModalResourceReference.get()));
		String script;
		if (width != null)
			script = String.format("setupModal('%s', '%s', %s);", getMarkupId(), width, showImmediately);
		else
			script = String.format("setupModal('%s', undefined, %s);", getMarkupId(), showImmediately);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
