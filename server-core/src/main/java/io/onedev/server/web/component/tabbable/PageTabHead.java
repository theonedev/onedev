package io.onedev.server.web.component.tabbable;

import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;

import javax.annotation.Nullable;

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
		if (tab.getIconModel() != null)
			pageLink.add(new SpriteImage("icon", tab.getIconModel()));
		else
			pageLink.add(new WebMarkupContainer("icon").setVisible(false));
		pageLink.add(new Label("label", tab.getTitleModel()));
	}
	
	@Nullable
	protected String getIcon() {
		return null;
	}

	protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
		return new ViewStateAwarePageLink<Void>(linkId, pageClass);
	}

}
