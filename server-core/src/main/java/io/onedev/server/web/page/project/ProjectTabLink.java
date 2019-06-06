package io.onedev.server.web.page.project;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;

@SuppressWarnings("serial")
public class ProjectTabLink extends Panel {

	private final ProjectTab tab;
	
	public ProjectTabLink(String id, ProjectTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<?> link = newLink("link", tab.getMainPageClass());
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		
		link.add(new Label("text", tab.getTitleModel()));
		
		link.add(new Label("count", tab.getCount()).setVisible(tab.getCount()!=0));
		add(link);
	}
	
	protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
		ProjectPage page = (ProjectPage) getPage();
		return new ViewStateAwarePageLink<Void>("link",pageClass, ProjectPage.paramsOf(page.getProject()));
	}

}
