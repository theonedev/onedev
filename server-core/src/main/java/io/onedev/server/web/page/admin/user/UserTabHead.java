package io.onedev.server.web.page.admin.user;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class UserTabHead extends Panel {

	private final UserTab tab;
	
	public UserTabHead(String id, UserTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		UserPage page = (UserPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), UserPage.paramsOf(page.getUser()));
		link.add(new SpriteImage("icon", tab.getIconHref()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
