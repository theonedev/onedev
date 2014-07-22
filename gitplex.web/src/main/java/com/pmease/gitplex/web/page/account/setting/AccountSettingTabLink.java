package com.pmease.gitplex.web.page.account.setting;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class AccountSettingTabLink extends Panel {

	private final AccountSettingTab tab;
	
	public AccountSettingTabLink(String id, AccountSettingTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AccountSettingPage page = (AccountSettingPage) getPage();
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), AccountSettingPage.paramsOf(page.getAccount()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
