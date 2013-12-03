package com.pmease.commons.wicket.behavior.popover;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.dropdown.DropdownAlignment;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownAlignment.IndicatorMode;

@SuppressWarnings("serial")
public class PopoverBehavior extends DropdownBehavior {

	public PopoverBehavior(PopoverPanel popoverPanel) {
		super(popoverPanel);
		
		alignment(new DropdownAlignment(100, 50, 0, 50).indicatorMode(IndicatorMode.SHOW));
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(PopoverBehavior.class, "popover.css")));
	}

}
