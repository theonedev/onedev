package com.gitplex.server.web.page.user;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import com.gitplex.server.web.component.link.ViewStateAwarePageLink;

@SuppressWarnings("serial")
public class UserTabLink extends Panel {

	private final UserTab tab;
	
	public UserTabLink(String id, UserTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		UserPage page = (UserPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), UserPage.paramsOf(page.getUser()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
