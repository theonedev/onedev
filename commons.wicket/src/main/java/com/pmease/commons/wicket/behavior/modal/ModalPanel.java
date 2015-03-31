package com.pmease.commons.wicket.behavior.modal;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
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
@SuppressWarnings("serial")
public abstract class ModalPanel extends Panel {

	private String width;
	
	boolean showImmediately = true;
	
	public ModalPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(AttributeAppender.append("class", "modal popup"));
		
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

	/**
	 * Close this modal.
	 *
	 * @param target
	 * 			Wicket ajax request target 
	 */
	public void hide(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("pmease.commons.modal.hide('%s');", getMarkupId()));
	}
	
	void load(AjaxRequestTarget target) {
		Component content = newContent("content");
		content.add(AttributeModifier.append("class", " content"));
		replace(content);
		target.add(content);

		String script = String.format("pmease.commons.modal.loaded('%s')", getMarkupId());
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

		String script;
		if (width != null)
			script = String.format("pmease.commons.modal.setup('%s', '%s', %s);", getMarkupId(), width, showImmediately);
		else
			script = String.format("pmease.commons.modal.setup('%s', undefined, %s);", getMarkupId(), showImmediately);
		response.render(new PriorityHeaderItem(OnDomReadyHeaderItem.forScript(script)));
	}

}
