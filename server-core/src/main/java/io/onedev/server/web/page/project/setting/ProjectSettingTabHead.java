package io.onedev.server.web.page.project.setting;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class ProjectSettingTabHead extends Panel {

	private final ProjectSettingTab tab;
	
	public ProjectSettingTabHead(String id, ProjectSettingTab tab) {
		super(id);
		this.tab = tab;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ProjectPage page = (ProjectPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
				tab.getMainPageClass(), ProjectPage.paramsOf(page.getProject()));
		link.add(new SpriteImage("icon", tab.getIconHref()));
		link.add(new Label("label", tab.getTitleModel()));
		add(link);
	}

}
