package com.pmease.commons.wicket.component;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.commons.wicket.dropdown.AlignWith;
import com.pmease.commons.wicket.dropdown.AlignWithComponent;
import com.pmease.commons.wicket.dropdown.AlignWithMe;
import com.pmease.commons.wicket.dropdown.AlignWithMouse;

@SuppressWarnings("serial")
public abstract class DropdownLink<T> extends AjaxLink<T> {

	private final AlignWith alignWith;
	
	private final Alignment alignment;
	
	private FloatingPanel dropdown;

	public DropdownLink(String id) {
		this(id, new Alignment(0, 100, 0, 0, 8, true));
	}
	
	public DropdownLink(String id, Alignment alignment) {
		this(id, null, new AlignWithMe(), alignment);
	}

	public DropdownLink(String id, @Nullable IModel<T> model, Alignment alignment) {
		this(id, model, new AlignWithMe(), alignment);
	}
	
	public DropdownLink(String id, AlignWith alignWith, Alignment alignment) {
		this(id, null, alignWith, alignment);
	}
	
	public DropdownLink(String id, @Nullable IModel<T> model, 
			AlignWith alignWith, Alignment alignment) {
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
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);

		String script = String.format(""
				+ "return {mouseX: $('#%s').data('mouseX'), mouseY: $('#%s').data('mouseY')};", 
				getMarkupId(), getMarkupId());
		attributes.getDynamicExtraParameters().add(script);
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		if (dropdown == null) { 
			com.pmease.commons.wicket.component.floating.AlignWith alignFloatingWith;
			if (alignWith instanceof AlignWithComponent) {
				Component component = ((AlignWithComponent)alignWith).getComponent();
				alignFloatingWith =  new com.pmease.commons.wicket.component.floating.AlignWithComponent(component);
			} else if (alignWith instanceof AlignWithMe) { 
				alignFloatingWith =  new com.pmease.commons.wicket.component.floating.AlignWithComponent(this);
			} else {
				int mouseX = RequestCycle.get().getRequest().getRequestParameters()
						.getParameterValue("mouseX").toInt();
				int mouseY = RequestCycle.get().getRequest().getRequestParameters()
						.getParameterValue("mouseY").toInt();
				alignFloatingWith = ((AlignWithMouse)alignWith).asCoords(mouseX, mouseY);
			}
			
			dropdown = new FloatingPanel(target, alignFloatingWith, alignment) {
	
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
					
					dropdown = null;
				}
	
			};
			String script = String.format("$('#%s').addClass('dropdown-open');", getMarkupId(true));
			target.appendJavaScript(script);
		} else {
			dropdown.close(target);
		}
	}

	public void close(AjaxRequestTarget target) {
		if (dropdown != null)
			dropdown.close(target);
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
				+ "}).on('click', function(e){"
				+ "  $(this).data('mouseX', e.pageX).data('mouseY', e.pageY);"
				+ "});", getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String id);
}
