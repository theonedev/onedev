package com.pmease.commons.wicket.component;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class DropdownLink<T> extends AjaxLink<T> {

	private final Component alignWith;
	
	private final Alignment alignment;
	
	private FloatingPanel dropdown;

	public DropdownLink(String id) {
		this(id, new Alignment(0, 100, 0, 0, 8, true));
	}
	
	public DropdownLink(String id, Alignment alignment) {
		this(id, null, null, alignment);
	}

	public DropdownLink(String id, @Nullable IModel<T> model, Alignment alignment) {
		this(id, model, null, alignment);
	}
	
	public DropdownLink(String id, @Nullable Component alignWith, Alignment alignment) {
		this(id, null, alignWith, alignment);
	}
	
	public DropdownLink(String id, @Nullable IModel<T> model, 
			@Nullable Component alignWith, Alignment alignment) {
		super(id, model);
		
		this.alignWith = alignWith;
		this.alignment = alignment;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", "dropdown-link"));
	}

	protected void onInitialize(FloatingPanel dropdown) {
		
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		if (dropdown == null || dropdown.findParent(Page.class) == null) {
			dropdown = new FloatingPanel(target, alignWith!=null?alignWith:this, alignment) {
	
				@Override
				protected void onInitialize() {
					super.onInitialize();
					DropdownLink.this.onInitialize(this);
				}

				@Override
				protected Component newContent(String id) {
					return DropdownLink.this.newContent(id);
				}

				@Override
				protected void onClosed(AjaxRequestTarget target) {
					super.onClosed(target);

					String script = String.format("$('#%s').removeClass('dropdown-open');", 
							DropdownLink.this.getMarkupId(true));
					target.appendJavaScript(script);
				}
	
			};
			String script = String.format("$('#%s').addClass('dropdown-open');", getMarkupId(true));
			target.appendJavaScript(script);
		} else {
			dropdown.close(target);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		/*
		 * We want to toggle open/close of the dropdown if we click on the link, 
		 * below code is added in order not to close the dropdown from browser side,
		 * otherwise, the dropdown will be closed at browser side and opened again 
		 * from server side
		 */
		String script = String.format(""
				+ "$('#%s').on('mouseup touchstart', function() {"
				+ "  return false;"
				+ "});", getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String id);
}
