package com.pmease.commons.wicket.behavior.dropdown;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.component.floating.AlignFloatingWithComponent;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class DropdownHover extends AbstractDefaultAjaxBehavior {

	private static final int DEFAULT_HOVER_DELAY = 350;
	
	private final Component alignWith;
	
	private final Alignment alignment;
	
	private final int hoverDelay;
	
	private FloatingPanel dropdown;
	
	public DropdownHover() {
		this(new Alignment(0, 100, 0, 0, 8, true));
	}
	
	public DropdownHover(Alignment alignment) {
		this(alignment, DEFAULT_HOVER_DELAY);
	}
	
	public DropdownHover(Alignment alignment, int hoverDelay) {
		this(null, alignment, hoverDelay);
	}
	
	public DropdownHover(@Nullable Component alignWith, Alignment alignment) {
		this(alignWith, alignment, DEFAULT_HOVER_DELAY);
	}

	public DropdownHover(@Nullable Component alignWith, Alignment alignment, int hoverDelay) {
		this.alignWith = alignWith;
		this.alignment = alignment;
		this.hoverDelay = hoverDelay;
	}
	
	protected void onInitialize(FloatingPanel dropdown) {
		
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
		if (dropdown == null) {
			dropdown = new FloatingPanel(target, new AlignFloatingWithComponent(alignWith!=null?alignWith:getComponent()), alignment) {
				
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
				protected void onClosed(AjaxRequestTarget target) {
					super.onClosed(target);
	
					String script = String.format("pmease.commons.dropdownhover.closed('%s', '%s');", 
							getComponent().getMarkupId(true), getMarkupId(true));
					target.appendJavaScript(script);
					
					dropdown = null;
				}
	
			};
			String script = String.format("pmease.commons.dropdownhover.opened('%s', '%s');", 
					getComponent().getMarkupId(true), dropdown.getMarkupId(true));
			target.appendJavaScript(script);
		}
	}

	public void close(AjaxRequestTarget target) {
		if (dropdown != null) 
			dropdown.close(target);
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DropdownHover.class, "dropdown-hover.js")));
		String script = String.format("pmease.commons.dropdownhover.init('%s', %s, %s);", 
				getComponent().getMarkupId(true), hoverDelay, getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String id);
}
