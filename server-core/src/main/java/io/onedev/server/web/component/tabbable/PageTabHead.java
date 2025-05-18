package io.onedev.server.web.component.tabbable;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
		
		Link<?> link = newLink("link", tab.getMainPageClass(), tab.getMainPageParams());
		add(link);
		if (tab.getIconModel() != null)
			link.add(new SpriteImage("icon", tab.getIconModel()));
		else
			link.add(new WebMarkupContainer("icon").setVisible(false));
		link.add(new Label("label", tab.getTitleModel()));
	}

	protected Link<?> newLink(String id, Class<? extends Page> pageClass, PageParameters pageParams) {
		return new ViewStateAwarePageLink<Void>(id, pageClass, pageParams);
	}

}
