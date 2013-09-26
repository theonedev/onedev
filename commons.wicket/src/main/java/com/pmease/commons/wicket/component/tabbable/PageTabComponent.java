package com.pmease.commons.wicket.component.tabbable;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class PageTabComponent extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	public PageTabComponent(String id, PageTab tab) {
		super(id);
		
		Link<?> pageLink = newLink("link", (Class<? extends Page>) tab.getPageClasses()[0]);
		add(pageLink);
		pageLink.add(new Label("label", tab.getTitleModel()));
	}

	protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
		return new BookmarkablePageLink<Void>("link", pageClass);
	}

}
