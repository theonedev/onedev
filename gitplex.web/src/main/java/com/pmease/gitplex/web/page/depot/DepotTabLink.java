package com.pmease.gitplex.web.page.depot;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class DepotTabLink extends Panel {

	private final DepotTab tab;
	
	public DepotTabLink(String id, DepotTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DepotPage page = (DepotPage) getPage();
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), DepotPage.paramsOf(page.getDepot()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		
		link.add(new Label("text", tab.getTitleModel()));
		
		link.add(new Label("count", tab.getCount()).setVisible(tab.getCount()!=0));
		add(link);
	}

}
