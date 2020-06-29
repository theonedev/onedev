package io.onedev.server.web.component.modal;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class ModalPanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private boolean inited;
	
	public ModalPanel(IPartialPageRequestHandler handler) {
		this(handler, null);
	}
	
	public ModalPanel(IPartialPageRequestHandler handler, IModel<?> model) {
		super(((BasePage)handler.getPage()).getRootComponents().newChildId(), model);
		
		BasePage page = (BasePage) handler.getPage(); 
		page.getRootComponents().add(this);
		handler.prependJavaScript(String.format("$('body').append(\"<div id='%s'></div>\");", getMarkupId()));
		handler.add(this);
	}
	
	@Override
	protected void onBeforeRender() {
		if (!inited) {
			WebMarkupContainer dialog = new WebMarkupContainer("dialog");
			add(dialog);
			
			dialog.add(newContent(CONTENT_ID));

			String cssClass = getCssClass();
			if (cssClass != null)
				dialog.add(AttributeAppender.append("class", cssClass));
			
			inited = true;
		}
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				ModalPanel.this.remove();
				onClosed();
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(new ModalResourceReference()));
				
				String script = String.format("onedev.server.modal.onDomReady('%s', %s);", 
						getMarkupId(true), getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
		
		add(AttributeAppender.append("class", "modal"));
		setOutputMarkupId(true);
	}
	
	protected abstract Component newContent(String id);
	
	@Nullable
	protected String getCssClass() {
		return null;
	}

	public Component getContent() {
		return get(CONTENT_ID); 
	}
	
	public final void close() {
		AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
		
		if (target != null) {
			String script = String.format("onedev.server.modal.close($('#%s'), false);", getMarkupId(true));
			target.appendJavaScript(script);
		}
		remove();
		onClosed();
	}
	
	protected void onClosed() {
	}
	
}