package io.onedev.server.web.page.project.setting;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class ProjectSettingPage extends ProjectPage {

	public ProjectSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAdministrate(getProject().getFacade());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SideBar("projectSettingSidebar", null) {

			@Override
			protected List<? extends Tab> newTabs() {
				return newSettingTabs();
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new ProjectSettingResourceReference()));
		String script = String.format(""
				+ "var $projectSetting = $('#project-setting');"
				+ "$projectSetting.find('>table').height($projectSetting.parent().outerHeight());");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
