package com.gitplex.server.web.component.floating;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.gitplex.server.web.behavior.AbstractPostAjaxBehavior;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class FloatingPanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private final AlignTarget alignTarget;
	
	private final AlignPlacement placement;
	
	public FloatingPanel(AjaxRequestTarget target, AlignTarget alignTarget, AlignPlacement placement) {
		super(((BasePage)target.getPage()).getRootComponents().newChildId());
		
		BasePage page = (BasePage) target.getPage(); 
		page.getRootComponents().add(this);
		target.prependJavaScript(String.format("$('body').append(\"<div id='%s'></div>\");", getMarkupId()));
		target.add(this);

		this.alignTarget = alignTarget;
		this.placement = placement;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent(CONTENT_ID).setOutputMarkupId(true));
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				FloatingPanel.this.remove();
				onClosed();
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(new FloatingResourceReference()));
				
				String script = String.format("gitplex.server.floating.init('%s', {target:%s, placement:%s}, %s);", 
						getMarkupId(true), alignTarget, placement, getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
		
		add(AttributeAppender.append("class", "floating"));
		setOutputMarkupId(true);
	}
	
	protected abstract Component newContent(String id);

	public Component getContent() {
		return get(CONTENT_ID); 
	}
	
	public final void close() {
		AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
		if (target != null) {
			String script = String.format("gitplex.server.floating.close($('#%s'), false);", getMarkupId(true));
			target.appendJavaScript(script);
		} 
		if (getParent() != null)
			remove();
		onClosed();
	}
	
	protected void onClosed() {
		
	}
}