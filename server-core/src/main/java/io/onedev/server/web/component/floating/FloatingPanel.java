package io.onedev.server.web.component.floating;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.DisplayNoneBehavior;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.Animation;

@SuppressWarnings("serial")
public abstract class FloatingPanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private final Alignment alignment;
	
	private final Animation animation;
	
	public FloatingPanel(AjaxRequestTarget target, @Nullable Alignment alignment, 
			@Nullable Animation animation) {
		super(((BasePage)target.getPage()).getRootComponents().newChildId());
		
		this.alignment = alignment;
		this.animation = animation;
		
		BasePage page = (BasePage) target.getPage(); 
		page.getRootComponents().add(this);
		
		String script = String.format("$('body').append(\"<div id='%s'></div>\");", 
				getMarkupId());
		target.prependJavaScript(script);
		
		if (animation != null)
			add(new DisplayNoneBehavior());
		target.add(this);

		if (animation != null) {
			script = String.format("$('#%s').show('slide', {direction: '%s'}, 200);", 
					getMarkupId(true), animation.name().toLowerCase());	
			target.appendJavaScript(script);
		}
	}

	public FloatingPanel(AjaxRequestTarget target, @Nullable Alignment alignment) {
		this(target, alignment, null);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent(CONTENT_ID).setOutputMarkupId(true));
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				FloatingPanel.this.remove();
				String script = String.format("onedev.server.floating.close('%s');", getMarkupId(true));
				target.appendJavaScript(script);
				onClosed();
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);

				response.render(JavaScriptHeaderItem.forReference(new FloatingResourceReference()));
				
				String jsonOfAlignment;
				if (alignment != null) {
					jsonOfAlignment = String.format("{target:%s, placement:%s}", 
						alignment.getTarget(), alignment.getPlacement());
				} else {
					jsonOfAlignment = "undefined";
				}
				String script = String.format("onedev.server.floating.init('%s', %s, %s, %s);", 
						getMarkupId(true), jsonOfAlignment, 
						animation!=null?"'" + animation + "'":"undefined", 
						getCallbackFunction());
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
			String script = String.format("onedev.server.floating.close('%s');", getMarkupId(true));
			target.appendJavaScript(script);
		} 
		if (getParent() != null)
			remove();
		onClosed();
	}
	
	protected void onClosed() {
		
	}
}