package io.onedev.server.web.behavior;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;

public class NoRecordsBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	public static final String CSS_CLASS = "norecords";
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		
		if (!(component instanceof IPageableItems)) 
			throw new RuntimeException("NoRecordsBehavior can only be applied to IPageableItems");
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);
		IPageableItems pageable = (IPageableItems) component;
		
		if (pageable.getItemCount() == 0)
			decorate(tag);
	}
	
	public static void decorate(ComponentTag tag) {
		String classes = tag.getAttribute("class");
		if (StringUtils.isNotBlank(classes))
			tag.put("class", classes + " " + CSS_CLASS);
		else
			tag.put("class", CSS_CLASS);
	}
	
}
