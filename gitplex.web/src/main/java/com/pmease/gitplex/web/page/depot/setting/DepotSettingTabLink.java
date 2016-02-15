package com.pmease.gitplex.web.page.depot.setting;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

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
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), DepotSettingPage.paramsOf(page.getDepot()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
