package com.gitplex.server.web.page.depot.setting;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.web.component.link.ViewStateAwarePageLink;

@SuppressWarnings("serial")
public class DepotSettingTabLink extends Panel {

	private final DepotSettingTab tab;
	
	public DepotSettingTabLink(String id, DepotSettingTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DepotSettingPage page = (DepotSettingPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), DepotSettingPage.paramsOf(page.getDepot()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
