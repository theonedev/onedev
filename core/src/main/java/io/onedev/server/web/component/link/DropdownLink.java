package io.onedev.server.web.component.link;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.AlignTarget;
import io.onedev.server.web.component.floating.Alignment;
import io.onedev.server.web.component.floating.ComponentTarget;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.floating.RectTarget;

@SuppressWarnings("serial")
public abstract class DropdownLink extends AjaxLink<Void> {

	private final boolean alignTargetMouse;
	
	private final AlignPlacement placement;
	
	private FloatingPanel dropdown;

	public DropdownLink(String id) {
		this(id, AlignPlacement.bottom(0));
	}
	
	public DropdownLink(String id, AlignPlacement placement) {
		this(id, false, placement);
	}

	public DropdownLink(String id, boolean alignTargetMouse) {
		this(id, alignTargetMouse, AlignPlacement.bottom(0));
	}
	
	public DropdownLink(String id, boolean alignTargetMouse, AlignPlacement placement) {
		super(id);
		
		this.alignTargetMouse = alignTargetMouse;
		this.placement = placement;
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

		attributes.setMethod(Method.POST);
		
		String script = String.format(""
				+ "return {mouseX: $('#%s').data('mouseX'), mouseY: $('#%s').data('mouseY')};", 
				getMarkupId(), getMarkupId());
		attributes.getDynamicExtraParameters().add(script);
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		// if dropdown has not been created, or has been removed from page 
		// when the same page instance is refreshed 
		if (dropdown == null || dropdown.getParent() == null) { 
			AlignTarget alignFloatingWith;
			if (alignTargetMouse) {
				int mouseX = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("mouseX").toInt();
				int mouseY = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("mouseY").toInt();
				alignFloatingWith = RectTarget.ofMouse(mouseX, mouseY);
			} else { 
				alignFloatingWith =  new ComponentTarget(this);
			} 
			
			dropdown = new FloatingPanel(target, new Alignment(alignFloatingWith, placement)) {
	
				@Override
				protected void onInitialize() {
					super.onInitialize();
					DropdownLink.this.onInitialize(this);
				}

				@Override
				protected Component newContent(String id) {
					return DropdownLink.this.newContent(id, this);
				}

				@Override
				protected void onClosed() {
					super.onClosed();

					AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
					if (target != null) {
						String script = String.format("$('#%s').removeClass('dropdown-open');", 
								DropdownLink.this.getMarkupId(true));
						target.appendJavaScript(script);
					}
					
					dropdown = null;
				}
	
			};
			String script = String.format(""
					+ "$('#%s').addClass('dropdown-open');"
					+ "$('#%s').data('trigger', $('#%s'));", 
					getMarkupId(), dropdown.getMarkupId(), getMarkupId());
			target.appendJavaScript(script);
		} else {
			dropdown.close();
		}
	}

	public String getCloseScript() {
		if (dropdown != null) {
			return String.format("onedev.server.floating.close($('#%s'), true);", dropdown.getMarkupId());
		} else {
			return "";
		}
	}
	
	public String closeBeforeClick(CharSequence onClickScript) {
		return getCloseScript() + (onClickScript!=null?onClickScript:"");
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

	protected abstract Component newContent(String id, FloatingPanel dropdown);
}
