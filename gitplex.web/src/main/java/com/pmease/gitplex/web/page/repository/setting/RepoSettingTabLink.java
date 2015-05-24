package com.pmease.gitplex.web.page.repository.setting;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class RepoSettingTabLink extends Panel {

	private final RepoSettingTab tab;
	
	public RepoSettingTabLink(String id, RepoSettingTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepoSettingPage page = (RepoSettingPage) getPage();
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), RepoSettingPage.paramsOf(page.getRepository()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
