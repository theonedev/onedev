package com.pmease.commons.product.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.ActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.commons.wicket.page.CommonPage;

@SuppressWarnings("serial")
public class HomePage extends CommonPage  {
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<Tab>();
		tabs.add(new ActionTab(Model.of("panel1")) {

			@Override
			protected void tabActivated() {
				HomePage.this.replace(new Label("content", "panel1"));
			}
			
		}.setActive(true));
		tabs.add(new ActionTab(Model.of("panel2")) {

			@Override
			protected void tabActivated() {
				HomePage.this.replace(new Label("content", "panel2"));
			}
			
		});

		add(new Tabbable("tabs", tabs) {

			@Override
			protected String getCssClasses() {
				return "nav nav-tabs";
			}
			
		});
		
		add(new Label("content", "panel1"));
	}	
	
}