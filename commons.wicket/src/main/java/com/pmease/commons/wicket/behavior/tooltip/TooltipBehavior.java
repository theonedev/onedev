package com.pmease.commons.wicket.behavior.tooltip;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.dropdown.AlignmentTarget;
import com.pmease.commons.wicket.behavior.dropdown.DropdownAlignment;
import com.pmease.commons.wicket.behavior.dropdown.DropdownResourceReference;

public class TooltipBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	private final IModel<String> contentModel;
	
	private DropdownAlignment alignment = new DropdownAlignment(
			new AlignmentTarget(null, 50, 0), 50, 100, -1, true);
	
	public TooltipBehavior(IModel<String> contentModel) {
		this.contentModel = contentModel;
	}
	
	public TooltipBehavior(String content) {
		this(Model.of(content));
	}
	
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	/**
	 * Specify how the tooltip is aligned to the target. 
	 * 
	 * @param alignment
	 * 			The {@link DropdownAlignment alignment} setting object. 
	 * @return
	 * 			This behavior.
	 */
	public TooltipBehavior alignment(DropdownAlignment alignment) {
		this.alignment = alignment;
		if (alignment != null 
				&& alignment.getTarget() != null 
				&& alignment.getTarget().getComponent() != null) {
			alignment.getTarget().getComponent().setOutputMarkupId(true);
		}
		return this;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(DropdownResourceReference.get()));
		
		String escapedContent = StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(contentModel.getObject()));
		String script = String.format(
				"setupDropdown('%s', '<div class=\\'tooltip\\' id=\\'%s-dropdown\\'><div class=\\'content\\'>%s</div></div>', 0, %s, undefined)", 
				component.getMarkupId(), component.getMarkupId(), escapedContent, alignment.toJSON(component));
		response.render(OnDomReadyHeaderItem.forScript(script));
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(TooltipBehavior.class, "tooltip.css")));
	}

	@Override
	public void detach(Component component) {
		contentModel.detach();
		super.detach(component);
	}

}
