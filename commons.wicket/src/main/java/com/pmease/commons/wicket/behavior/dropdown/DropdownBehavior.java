package com.pmease.commons.wicket.behavior.dropdown;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class DropdownBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	private final DropdownPanel dropdownPanel;
	
	private int hoverDelay = -1;
	
	private DropdownAlignment alignment = new DropdownAlignment();
	
	public DropdownBehavior(DropdownPanel dropdownPanel) {
		this.dropdownPanel = dropdownPanel;
	}
	
	/**
	 * Set time to delay in milliseconds before displaying the dropdown panel when hover the mouse 
	 * over the dropdown trigger. Specifically if this value is less than 0, the dropdown panel 
	 * can only be displayed when the trigger is clicked.  
	 *  
	 * @param hoverDelay
	 * 			Milliseconds to wait before displaying the dropdown panel when hover mouse over the 
	 * 			trigger. Use negative value to display dropdown panel upon clicking the trigger.  
	 * @return 
	 * 			This behavior.
	 */
	public DropdownBehavior setHoverDelay(int hoverDelay) {
		this.hoverDelay = hoverDelay;
		return this;
	}
	
	/**
	 * Specify whether or not to display the dropdown panel by clicking the trigger element.
	 * 
	 * @param clickMode
	 * 			Whether or not the dropdown panel is shown via clicking the trigger element. If 
	 * 			set to false, the dropdown panel will be displayed by hovering the mouse over 
	 * 			the trigger element.
	 * @return
	 * 			This behavior.
	 */
	public DropdownBehavior setClickMode(boolean clickMode) {
		if (clickMode)
			hoverDelay = -1;
		else
			hoverDelay = 350;
		
		return this;
	}
	
	/**
	 * Specify how the dropdown panel is aligned to the target. 
	 * @param alignment
	 * 			The {@link DropdownAlignment alignment} setting object. 
	 * @return
	 * 			This behavior.
	 */
	public DropdownBehavior setAlignment(DropdownAlignment alignment) {
		this.alignment = alignment;
		if (alignment.getTarget() != null)
			alignment.getTarget().setOutputMarkupId(true);
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
		response.render(JavaScriptHeaderItem.forReference(DropdownResourceReference.get()));
		String script = String.format(
				"setupDropdown('%s', '%s', %s, '%s', '%s', %s, %s, %s, %s, %d, %s)", 
				getComponent().getMarkupId(), dropdownPanel.getMarkupId(), hoverDelay, alignment.getIndicatorMode().name(), 
				alignment.getTarget()!=null?alignment.getTarget().getMarkupId():getComponent().getMarkupId(), 
				alignment.getTargetX(), alignment.getTargetY(), alignment.getDropdownX(), alignment.getDropdownY(), 
				alignment.getGap(), getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		dropdownPanel.load(target);

		String script = String.format("dropdownLoaded('%s')", dropdownPanel.getMarkupId());
		
		target.appendJavaScript(script);
	}

}
