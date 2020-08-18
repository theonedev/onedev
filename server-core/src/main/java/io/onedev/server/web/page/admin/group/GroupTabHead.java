package io.onedev.server.web.page.admin.group;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class GroupTabHead extends Panel {

	private final GroupTab tab;
	
	public GroupTabHead(String id, GroupTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		GroupPage page = (GroupPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), GroupPage.paramsOf(page.getGroup()));
		link.add(new SpriteImage("icon", tab.getIconHref()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
