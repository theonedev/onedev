package io.onedev.server.web.page.project.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.sidebar.SidebarPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import io.onedev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionPage;

@SuppressWarnings("serial")
public abstract class ProjectSettingPage extends ProjectPage {

	public ProjectSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getProject());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SidebarPanel("projectSettingSidebar", null) {

			@Override
			protected List<? extends Tab> newTabs() {
				List<PageTab> tabs = new ArrayList<>();
				tabs.add(new ProjectSettingTab("General Setting", "fa fa-fw fa-sliders", GeneralSettingPage.class));
				tabs.add(new ProjectSettingTab("Authorizations", "fa fa-fw fa-user", ProjectAuthorizationsPage.class));
				tabs.add(new ProjectSettingTab("Branch Protection", "fa fa-fw fa-lock", BranchProtectionPage.class));
				tabs.add(new ProjectSettingTab("Tag Protection", "fa fa-fw fa-lock", TagProtectionPage.class));
				tabs.add(new ProjectSettingTab("Commit Message Transform", "fa fa-fw fa-exchange", CommitMessageTransformPage.class));
				return tabs;
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
