package com.pmease.commons.wicket.behavior.dropdown;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class DropdownBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	private final DropdownPanel dropdownPanel;
	
	private DropdownMode displayMode = new DropdownMode.Click();
	
	private DropdownAlignment alignment = new DropdownAlignment(new AlignmentTarget(null, 0, 100), 0, 0, -1, false);
	
	public DropdownBehavior(DropdownPanel dropdownPanel) {
		this.dropdownPanel = dropdownPanel;
	}
	
	public DropdownBehavior mode(DropdownMode dropdownMode) {
		this.displayMode = dropdownMode;
		return this;
	}
	
	/**
	 * Specify how the dropdown panel is aligned to the target. 
	 * 
	 * @param alignment
	 * 			the {@link DropdownAlignment alignment} setting object
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignment(DropdownAlignment alignment) {
		this.alignment = alignment;
		if (alignment != null 
				&& alignment.getTarget() != null 
				&& alignment.getTarget().getComponent() != null) {
			alignment.getTarget().getComponent().setOutputMarkupId(true);
		}
		return this;
	}

	/**
	 * Align dropdown with cursor. 
	 * 
	 * @param dropdownX
	 * 			dropdown horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownY
	 * 			dropdown vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param offset
	 * 			offset of the dropdown from target
	 * @param showIndicator
	 * 			whether or not to display the triangle indicator nearby target of 
	 * 			the dropdown
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignWithCursor(int dropdownX, int dropdownY, int offset, boolean showIndicator) {
		return alignment(new DropdownAlignment(null, dropdownX, dropdownY, offset, showIndicator));
	}
	
	/**
	 * Align dropdown with cursor without displaying indicator. 
	 * 
	 * @param dropdownX
	 * 			dropdown horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownY
	 * 			dropdown vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignWithCursor(int dropdownX, int dropdownY) {
		return alignment(new DropdownAlignment(null, dropdownX, dropdownY, -1, false));
	}

	/**
	 * Align dropdown with specified component. 
	 * 
	 * @param component
	 * 			component to align with
	 * @param componentX
	 * 			component horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param componentY
	 * 			component vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownX
	 * 			dropdown horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownY
	 * 			dropdown vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param offset
	 * 			offset of the dropdown from target
	 * @param showIndicator
	 * 			whether or not to display the triangle indicator nearby target of 
	 * 			the dropdown
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignWithComponent(Component component, int componentX, int componentY, 
			int dropdownX, int dropdownY, int offset, boolean showIndicator) {
		AlignmentTarget target = new AlignmentTarget(component, componentX, componentY);
		return alignment(new DropdownAlignment(target, dropdownX, dropdownY, 
				offset, showIndicator));
	}

	/**
	 * Align dropdown with the component triggering this dropdown. 
	 * 
	 * @param triggerX
	 * 			trigger horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param triggerY
	 * 			trigger vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownX
	 * 			dropdown horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownY
	 * 			dropdown vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param offset
	 * 			offset of the dropdown from target
	 * @param showIndicator
	 * 			whether or not to display the triangle indicator nearby target of 
	 * 			the dropdown
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignWithTrigger(int triggerX, int triggerY, 
			int dropdownX, int dropdownY, int offset, boolean showIndicator) {
		AlignmentTarget target = new AlignmentTarget(null, triggerX, triggerY);
		return alignment(new DropdownAlignment(target, dropdownX, dropdownY, 
				offset, showIndicator));
	}

	/**
	 * Align dropdown with specified component without displaying the indicator. 
	 * 
	 * @param component
	 * 			component to align with
	 * @param componentX
	 * 			component horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param componentY
	 * 			component vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownX
	 * 			dropdown horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownY
	 * 			dropdown vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignWithComponent(Component component, 
			int targetX, int targetY, int dropdownX, int dropdownY) {
		AlignmentTarget target = new AlignmentTarget(component, targetX, targetY);
		return alignment(new DropdownAlignment(target, dropdownX, dropdownY, -1, false));
	}

	/**
	 * Align dropdown with the component triggering this dropdown without displaying 
	 * the indicator. 
	 * 
	 * @param triggerX
	 * 			trigger horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param triggerY
	 * 			trigger vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownX
	 * 			dropdown horizontal position in range of <tt>0</tt> to <tt>100</tt>
	 * @param dropdownY
	 * 			dropdown vertical position in range of <tt>0</tt> to <tt>100</tt>
	 * @return
	 * 			this behavior
	 */
	public DropdownBehavior alignWithTrigger(int targetX, int targetY, int dropdownX, int dropdownY) {
		AlignmentTarget target = new AlignmentTarget(null, targetX, targetY);
		return alignment(new DropdownAlignment(target, dropdownX, dropdownY, -1, false));
	}

	@Override
	protected void onBind() {
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		String mode;
		if (displayMode instanceof DropdownMode.Hover)
			mode = String.valueOf(((DropdownMode.Hover) displayMode).getDelay());
		else if (displayMode instanceof DropdownMode.Click)
			mode = "'click'";
		else
			mode = "undefined";
		
		String script = String.format(
				"pmease.commons.dropdown.setup('%s', '%s', %s, %s, %s)", 
				component.getMarkupId(), dropdownPanel.getMarkupId(), mode, 
				alignment.toJSON(component), getCallbackFunction());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		dropdownPanel.load(target);

		String script = String.format("pmease.commons.dropdown.loaded('%s')", dropdownPanel.getMarkupId());
		
		target.appendJavaScript(script);
	}

}
