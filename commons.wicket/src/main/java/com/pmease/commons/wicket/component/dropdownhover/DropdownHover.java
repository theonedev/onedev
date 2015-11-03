package com.pmease.commons.wicket.component.dropdownhover;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class DropdownHover extends WebMarkupContainer {

	private final Component alignWith;
	
	private final Alignment alignment;
	
	private final int hoverDelay;
	
	public DropdownHover(String id) {
		this(id, new Alignment(0, 100, 0, 0, 8, true));
	}
	
	public DropdownHover(String id, Alignment alignment) {
		this(id, alignment, 350);
	}
	
	public DropdownHover(String id, Alignment alignment, int hoverDelay) {
		this(id, null, null, alignment, hoverDelay);
	}

	public DropdownHover(String id, @Nullable IModel<?> model, Alignment alignment, int hoverDelay) {
		this(id, model, null, alignment, hoverDelay);
	}
	
	public DropdownHover(String id, @Nullable Component alignWith, Alignment alignment, int hoverDelay) {
		this(id, null, alignWith, alignment, hoverDelay);
	}
	
	public DropdownHover(String id, @Nullable IModel<?> model, 
			@Nullable Component alignWith, Alignment alignment, int hoverDelay) {
		super(id, model);
		
		this.alignWith = alignWith;
		this.alignment = alignment;
		this.hoverDelay = hoverDelay;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				FloatingPanel dropdown = new FloatingPanel(target, alignWith!=null?alignWith:DropdownHover.this, alignment) {
					
					@Override
					protected Component newContent(String id) {
						return DropdownHover.this.newContent(id);
					}

					@Override
					protected void onClosed(AjaxRequestTarget target) {
						super.onClosed(target);

						String script = String.format("pmease.commons.dropdownhover.closed('%s', '%s');", 
								DropdownHover.this.getMarkupId(true), getMarkupId(true));
						target.appendJavaScript(script);
					}
		
				};
				String script = String.format("pmease.commons.dropdownhover.opened('%s', '%s');", 
						DropdownHover.this.getMarkupId(true), dropdown.getMarkupId(true));
				target.appendJavaScript(script);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(DropdownHover.class, "dropdown-hover.js")));
				String script = String.format("pmease.commons.dropdownhover.init('%s', %s, %s);", 
						getMarkupId(true), hoverDelay, getCallbackFunction());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
	}

	protected abstract Component newContent(String id);
}
