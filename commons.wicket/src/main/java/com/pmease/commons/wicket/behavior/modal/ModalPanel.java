package com.pmease.commons.wicket.behavior.modal;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
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
@SuppressWarnings("serial")
public abstract class ModalPanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private boolean showImmediately;
	
	public ModalPanel(String id) {
		super(id);
	}
	
	public ModalPanel(String id, boolean showImmediately) {
		super(id);
		this.showImmediately = showImmediately;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(AttributeAppender.append("class", "modal popup"));
		
		add(newContentPlaceholder());
		
		add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				unload(target);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				String script = String.format("pmease.commons.modal.setup('%s', %s, %s);", 
						getMarkupId(), !showImmediately?getCallbackFunction():"undefined", showImmediately);
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
	}
	
	@Override
	protected void onBeforeRender() {
		if (showImmediately)
			replace(newContent(CONTENT_ID, null).add(AttributeModifier.append("class", "content")));
		
		super.onBeforeRender();
	}
	
	/**
	 * Close this modal.
	 *
	 * @param target
	 * 			Wicket ajax request target 
	 */
	public void close(AjaxRequestTarget target) {
		if (!showImmediately)
			unload(target);
		target.appendJavaScript(String.format("pmease.commons.modal.hide('%s', true);", getMarkupId()));
	}
	
	private void unload(AjaxRequestTarget target) {
		Component content = newContentPlaceholder();
		replace(content);
		target.add(content);
	}
	
	protected Component newContentPlaceholder() {
		return new WebMarkupContainer(CONTENT_ID)
				.add(AttributeModifier.append("class", "loading"))
				.setOutputMarkupId(true);
	}
	
	void load(AjaxRequestTarget target, ModalBehavior behavior) {
		Component content = newContent(CONTENT_ID, behavior);
		content.add(AttributeModifier.append("class", " content"));
		replace(content);
		target.add(content);

		String script = String.format("pmease.commons.modal.loaded('%s')", getMarkupId());
		target.appendJavaScript(script);
	}
	
	protected abstract Component newContent(String id, @Nullable ModalBehavior behavior);

}
