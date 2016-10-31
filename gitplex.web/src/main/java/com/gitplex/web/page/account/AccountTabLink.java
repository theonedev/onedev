package com.gitplex.web.page.account;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class AccountTabLink extends Panel {

	private final AccountTab tab;
	
	public AccountTabLink(String id, AccountTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AccountPage page = (AccountPage) getPage();
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), AccountPage.paramsOf(page.getAccount()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		link.add(new Label("label", tab.getTitleModel()));
		link.add(new Label("count", tab.getCount()).setVisible(tab.getCount()!=0));		
		add(link);
	}

}
