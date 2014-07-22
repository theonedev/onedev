package com.pmease.gitplex.web.page.repository.admin;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class RepoAdminTabLink extends Panel {

	private final RepoAdminTab tab;
	
	public RepoAdminTabLink(String id, RepoAdminTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		RepoAdminPage page = (RepoAdminPage) getPage();
		Link<Void> link = new BookmarkablePageLink<Void>("link", 
				tab.getMainPageClass(), RepoAdminPage.paramsOf(page.getRepository()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
