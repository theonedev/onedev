package com.pmease.commons.product.web;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		DropdownPanel dropdownPanel = new DropdownPanel("dropdown") {

			@Override
			protected Component newContent(String id) {
				final Fragment fragment = new Fragment(id, "dropdownFrag", HomePage.this);
				fragment.add(new AjaxLink<Void>("test") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						fragment.get("test").add(new AttributeModifier("style", "display:block; width: 500px;"));
						target.add(fragment.get("test"));
					}
					
				}.setOutputMarkupId(true));
				return fragment;
			}
			
		};
		add(dropdownPanel);
		
		add(new WebMarkupContainer("dropdownTrigger").add(new DropdownBehavior(dropdownPanel)));
	}	
}