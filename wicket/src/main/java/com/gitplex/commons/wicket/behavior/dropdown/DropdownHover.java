package com.gitplex.commons.wicket.behavior.dropdown;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;

import com.gitplex.commons.wicket.behavior.AbstractPostAjaxBehavior;
import com.gitplex.commons.wicket.component.floating.AlignPlacement;
import com.gitplex.commons.wicket.component.floating.ComponentTarget;
import com.gitplex.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class DropdownHover extends AbstractPostAjaxBehavior {

	private static final int DEFAULT_HOVER_DELAY = 350;
	
	private final Component alignTarget;
	
	private final AlignPlacement placement;
	
	private final int hoverDelay;
	
	private FloatingPanel dropdown;
	
	public DropdownHover() {
		this(AlignPlacement.bottom(0));
	}
	
	public DropdownHover(AlignPlacement placement) {
		this(placement, DEFAULT_HOVER_DELAY);
	}
	
	public DropdownHover(AlignPlacement placement, int hoverDelay) {
		this(null, placement, hoverDelay);
	}
	
	public DropdownHover(@Nullable Component alignTarget, AlignPlacement placement) {
		this(alignTarget, placement, DEFAULT_HOVER_DELAY);
	}

	public DropdownHover(@Nullable Component alignTarget, AlignPlacement placement, int hoverDelay) {
		this.alignTarget = alignTarget;
		this.placement = placement;
		this.hoverDelay = hoverDelay;
	}
	
	protected void onInitialize(FloatingPanel dropdown) {
		
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		if (dropdown == null) {
			dropdown = new FloatingPanel(target, new ComponentTarget(alignTarget!=null?alignTarget:getComponent()), placement) {
				
				@Override
				protected void onInitialize() {
					super.onInitialize();
					DropdownHover.this.onInitialize(this);
				}
	
				@Override
				protected Component newContent(String id) {
					return DropdownHover.this.newContent(id);
				}
	
				@Override
				protected void onClosed() {
					super.onClosed();
	
					AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
					if (target != null) {
						String script = String.format("gitplex.commons.dropdownhover.closed('%s', '%s');", 
								getComponent().getMarkupId(true), getMarkupId(true));
						target.appendJavaScript(script);
					}
					
					dropdown = null;
				}
	
			};
			String script = String.format("gitplex.commons.dropdownhover.opened('%s', '%s');", 
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
		String script = String.format("gitplex.commons.dropdownhover.init('%s', %s, %s);", 
				getComponent().getMarkupId(true), hoverDelay, getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String id);
}
