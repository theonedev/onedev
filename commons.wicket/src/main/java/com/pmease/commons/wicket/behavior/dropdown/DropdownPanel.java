package com.pmease.commons.wicket.behavior.dropdown;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * This panel can not be used separately. It should be used together with DropdownBehavior. 
 * 
 * @see DropdownBehavior
 * @author robin
 *
 */
public abstract class DropdownPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final boolean lazyLoad;
	
	public DropdownPanel(String id, boolean lazyLoad) {
		super(id);
		this.lazyLoad = lazyLoad;
	}
	
	public DropdownPanel(String id) {
		this(id, true);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		add(AttributeAppender.append("class", "dropdown-panel"));
		if (lazyLoad) {
			add(new Fragment("content", "loadingFrag", DropdownPanel.this)
					.add(AttributeModifier.append("class", "loading"))
					.setOutputMarkupId(true));
		} else {
			add(newContent("content").add(AttributeModifier.append("class", "content")));
		}
	}
	
	/**
	 * Close this popup.
	 * 
	 * @param target
	 * 			target of this ajax request
	 */
	public void close(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("hideDropdown('%s');", getMarkupId()));
	}
	
	void load(AjaxRequestTarget target) {
		Component content = newContent("content");
		content.add(AttributeModifier.append("class", "content"));
		replace(content);
		target.add(content);
	}
	
	protected abstract Component newContent(String id);
}
