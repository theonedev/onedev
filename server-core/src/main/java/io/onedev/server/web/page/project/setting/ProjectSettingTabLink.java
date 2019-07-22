package io.onedev.server.web.page.project.setting;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class ProjectSettingTabLink extends Panel {

	private final ProjectSettingTab tab;
	
	public ProjectSettingTabLink(String id, ProjectSettingTab tab) {
		super(id);
		
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ProjectPage page = (ProjectPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), ProjectPage.paramsOf(page.getProject()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
