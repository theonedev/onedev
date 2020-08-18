package io.onedev.server.web.component.tabbable;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;

public class PageTabHead extends Panel {

	private static final long serialVersionUID = 1L;

	private final PageTab tab;
	
	public PageTabHead(String id, PageTab tab) {
		super(id);
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<?> pageLink = newLink("link", tab.getMainPageClass());
		add(pageLink);
		pageLink.add(new Label("label", tab.getTitleModel()));
	}

	protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
		return new ViewStateAwarePageLink<Void>(linkId, pageClass);
	}

}
