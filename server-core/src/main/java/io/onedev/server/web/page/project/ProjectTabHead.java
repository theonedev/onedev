package io.onedev.server.web.page.project;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class ProjectTabHead extends Panel {

	private final ProjectTab tab;
	
	public ProjectTabHead(String id, ProjectTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<?> link = newLink("link", tab.getMainPageClass());
		link.add(new SpriteImage("icon", tab.getIconHref()));
		
		link.add(new Label("text", tab.getTitleModel()));
		
		link.add(new Label("count", tab.getCount()).setVisible(tab.getCount()!=0));
		add(link);
	}
	
	protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
		ProjectPage page = (ProjectPage) getPage();
		return new ViewStateAwarePageLink<Void>("link",pageClass, ProjectPage.paramsOf(page.getProject()));
	}
	
}
