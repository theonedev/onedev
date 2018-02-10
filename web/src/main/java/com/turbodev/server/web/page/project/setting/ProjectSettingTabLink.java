package com.turbodev.server.web.page.project.setting;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import com.turbodev.server.web.component.link.ViewStateAwarePageLink;

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
		
		ProjectSettingPage page = (ProjectSettingPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), ProjectSettingPage.paramsOf(page.getProject()));
		link.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", tab.getIconClass())));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
