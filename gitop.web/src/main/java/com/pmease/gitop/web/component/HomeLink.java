package com.pmease.gitop.web.component;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class HomeLink extends BookmarkablePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public HomeLink(String id) {
		super(id, Application.get().getHomePage());
	}

}
