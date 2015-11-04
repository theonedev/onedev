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

import com.pmease.commons.wicket.component.floating.AlignFloatingWith;
import com.pmease.commons.wicket.component.floating.AlignFloatingWithComponent;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.commons.wicket.dropdown.AlignDropdownWith;
import com.pmease.commons.wicket.dropdown.AlignDropdownWithComponent;
import com.pmease.commons.wicket.dropdown.AlignDropdownWithMe;
import com.pmease.commons.wicket.dropdown.AlignDropdownWithMouse;

@SuppressWarnings("serial")
public abstract class DropdownLink<T> extends AjaxLink<T> {

	private final AlignDropdownWith alignWith;
	
	private final Alignment alignment;
	
	private FloatingPanel dropdown;

	public DropdownLink(String id) {
		this(id, new Alignment(0, 100, 0, 0, 8, true));
	}
	
	public DropdownLink(String id, Alignment alignment) {
		this(id, null, new AlignDropdownWithMe(), alignment);
	}

	public DropdownLink(String id, @Nullable IModel<T> model, Alignment alignment) {
		this(id, model, new AlignDropdownWithMe(), alignment);
	}
	
	public DropdownLink(String id, AlignDropdownWith alignWith, Alignment alignment) {
		this(id, null, alignWith, alignment);
	}
	
	public DropdownLink(String id, @Nullable IModel<T> model, 
			AlignDropdownWith alignWith, Alignment alignment) {
		super(id, model);
		
		this.alignWith = alignWith;
		this.alignment = alignment;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", "dropdown-link"));
		
		setOutputMarkupId(true);
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
			AlignFloatingWith alignFloatingWith;
			if (alignWith instanceof AlignDropdownWithComponent) {
				Component component = ((AlignDropdownWithComponent)alignWith).getComponent();
				alignFloatingWith =  new AlignFloatingWithComponent(component);
			} else if (alignWith instanceof AlignDropdownWithMe) { 
				alignFloatingWith =  new AlignFloatingWithComponent(this);
			} else {
				int mouseX = RequestCycle.get().getRequest().getRequestParameters()
						.getParameterValue("mouseX").toInt();
				int mouseY = RequestCycle.get().getRequest().getRequestParameters()
						.getParameterValue("mouseY").toInt();
				alignFloatingWith = ((AlignDropdownWithMouse)alignWith).asCoords(mouseX, mouseY);
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
			String script = String.format(""
					+ "$('#%s').addClass('dropdown-open');"
					+ "$('#%s').data('trigger', $('#%s'));", 
					getMarkupId(), dropdown.getMarkupId(), getMarkupId());
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
		
		String script = String.format(""
				+ "$('#%s').on('click', function(e){"
				+ "  $(this).data('mouseX', e.pageX).data('mouseY', e.pageY);"
				+ "});", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	protected abstract Component newContent(String id);
}
