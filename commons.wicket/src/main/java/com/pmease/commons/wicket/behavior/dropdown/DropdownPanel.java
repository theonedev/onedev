package com.pmease.commons.wicket.behavior.dropdown;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class DropdownPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public DropdownPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		add(new Fragment("content", "loadingFrag", DropdownPanel.this).setOutputMarkupId(true));
	}
	
	/**
	 * Close this popup.
	 * @param target
	 */
	public void close(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("hideDropdown('%s');", getMarkupId()));
	}
	
	public void load(AjaxRequestTarget target) {
		Component content = newContent("content");
		content.add(AttributeModifier.append("class", "dropdown-loaded"));
		replace(content);
		target.add(content);
	}
	
	protected abstract Component newContent(String id);
}
