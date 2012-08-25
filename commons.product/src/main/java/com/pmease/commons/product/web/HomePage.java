package com.pmease.commons.product.web;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.behavior.tooltip.TooltipBehavior;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("toggle").add(new TooltipBehavior(Model.of("alert('just do it')"))));
	}	
}