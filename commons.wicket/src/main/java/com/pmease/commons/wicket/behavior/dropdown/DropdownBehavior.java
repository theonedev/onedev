package com.pmease.commons.wicket.behavior.dropdown;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;

public class DropdownBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	private final DropdownPanel dropdownPanel;
	
	private boolean hoverMode;
	
	private boolean showIndicator;
	
	private DropdownAlignment alignment = new DropdownAlignment();
	
	public DropdownBehavior(DropdownPanel dropdownPanel) {
		this.dropdownPanel = dropdownPanel;
	}
	
	public DropdownBehavior setHoverMode(boolean hoverMode) {
		this.hoverMode = hoverMode;
		return this;
	}

	public DropdownBehavior setAlignment(DropdownAlignment alignment) {
		this.alignment = alignment;
		if (alignment.getTarget() != null)
			alignment.getTarget().setOutputMarkupId(true);
		return this;
	}

	public DropdownBehavior setShowIndicator(boolean showIndicator) {
		this.showIndicator = showIndicator;
		return this;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(CssHeaderItem.forReference(new PackageResourceReference(DropdownBehavior.class, "dropdown.css")));
		response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(DropdownBehavior.class, "dropdown.js")));
		String script = String.format(
				"setupDropdown('%s', '%s', %s, %s, '%s', %s, %s, %s, %s, %s)", 
				getComponent().getMarkupId(), dropdownPanel.getMarkupId(), hoverMode, showIndicator, 
				alignment.getTarget()!=null?alignment.getTarget().getMarkupId():getComponent().getMarkupId(), 
				alignment.getTargetX(), alignment.getTargetY(), alignment.getDropdownX(), alignment.getDropdownY(), 
				getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		dropdownPanel.load(target);

		String script = String.format("dropdownLoaded('%s')", dropdownPanel.getMarkupId());
		
		target.appendJavaScript(script);
	}

}
