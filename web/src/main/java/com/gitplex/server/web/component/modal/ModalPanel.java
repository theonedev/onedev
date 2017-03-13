package com.gitplex.server.web.component.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class ModalPanel extends Panel {

	public enum Size {SMALL, MEDIUM, LARGE}
	
	private static final String CONTENT_ID = "content";
	
	private final Size size;
	
	public ModalPanel(AjaxRequestTarget target) {
		this(target, Size.MEDIUM);
	}
	
	public ModalPanel(AjaxRequestTarget target, Size size) {
		super(((BasePage)target.getPage()).getRootComponents().newChildId());
		
		this.size = size;
		
		BasePage page = (BasePage) target.getPage(); 
		page.getRootComponents().add(this);
		target.prependJavaScript(String.format("$('body').append(\"<div id='%s'></div>\");", getMarkupId()));
		target.add(this);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer dialog = new WebMarkupContainer("dialog");
		add(dialog);
		
		dialog.add(newContent(CONTENT_ID));
		
		if (size == Size.LARGE)
			dialog.add(AttributeAppender.append("class", "modal-lg"));
		else if (size == Size.SMALL)
			dialog.add(AttributeAppender.append("class", "modal-sm"));
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				ModalPanel.this.remove();
				onClosed(target);
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(new ModalResourceReference()));
				
				String script = String.format("gitplex.server.modal.onDomReady('%s', %s);", 
						getMarkupId(true), getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
		
		setOutputMarkupId(true);
	}
	
	protected abstract Component newContent(String id);

	public Component getContent() {
		return get(CONTENT_ID); 
	}
	
	public final void close(AjaxRequestTarget target) {
		String script = String.format("gitplex.server.modal.close($('#%s>.modal'), false);", getMarkupId(true));
		target.appendJavaScript(script);
		
		remove();
		
		onClosed(target);
	}
	
	protected void onClosed(AjaxRequestTarget target) {
		
	}
	
}