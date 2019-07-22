package io.onedev.server.web.behavior.dropdown;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.Alignment;
import io.onedev.server.web.component.floating.ComponentTarget;
import io.onedev.server.web.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class DropdownHoverBehavior extends AbstractPostAjaxBehavior {

	private static final int DEFAULT_HOVER_DELAY = 350;
	
	private final Component alignTarget;
	
	private final AlignPlacement placement;
	
	private final int hoverDelay;
	
	private FloatingPanel dropdown;
	
	public DropdownHoverBehavior() {
		this(AlignPlacement.bottom(0));
	}
	
	public DropdownHoverBehavior(AlignPlacement placement) {
		this(placement, DEFAULT_HOVER_DELAY);
	}
	
	public DropdownHoverBehavior(AlignPlacement placement, int hoverDelay) {
		this(null, placement, hoverDelay);
	}
	
	public DropdownHoverBehavior(@Nullable Component alignTarget, AlignPlacement placement) {
		this(alignTarget, placement, DEFAULT_HOVER_DELAY);
	}

	public DropdownHoverBehavior(@Nullable Component alignTarget, AlignPlacement placement, int hoverDelay) {
		this.alignTarget = alignTarget;
		this.placement = placement;
		this.hoverDelay = hoverDelay;
	}
	
	protected void onInitialize(FloatingPanel dropdown) {
		
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		if (dropdown == null) {
			dropdown = new FloatingPanel(target, new Alignment(new ComponentTarget(alignTarget!=null?alignTarget:getComponent()), placement)) {
				
				@Override
				protected void onInitialize() {
					super.onInitialize();
					DropdownHoverBehavior.this.onInitialize(this);
				}
	
				@Override
				protected Component newContent(String id) {
					return DropdownHoverBehavior.this.newContent(id);
				}
	
				@Override
				protected void onClosed() {
					super.onClosed();
	
					AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
					if (target != null) {
						String script = String.format("onedev.server.dropdownHover.closed('%s', '%s');", 
								getComponent().getMarkupId(true), getMarkupId(true));
						target.appendJavaScript(script);
					}
					
					dropdown = null;
				}
	
			};
			String script = String.format("onedev.server.dropdownHover.opened('%s', '%s');", 
					getComponent().getMarkupId(true), dropdown.getMarkupId(true));
			target.appendJavaScript(script);
		}
	}

	public void close() {
		if (dropdown != null) 
			dropdown.close();
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(new DropdownHoverResourceReference()));
		String script = String.format("onedev.server.dropdownHover.onDomReady('%s', %s, %s);", 
				getComponent().getMarkupId(true), hoverDelay, getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String id);
}
