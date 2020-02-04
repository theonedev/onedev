package io.onedev.server.web.page.project.setting.build;

import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.component.build.authorization.ActionAuthorizationListPanel;
import io.onedev.server.web.component.build.authorization.ActionAuthorizationsBean;
import io.onedev.server.web.component.link.SettingInOwnerLink;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.user.buildsetting.UserActionAuthorizationsPage;
import io.onedev.server.web.page.my.buildsetting.MyActionAuthorizationsPage;

@SuppressWarnings("serial")
public class ProjectActionAuthorizationsPage extends ProjectBuildSettingPage {

	public ProjectActionAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ActionAuthorizationsBean bean = new ActionAuthorizationsBean();
		bean.setActionAuthorizations(getProject().getBuildSetting().getActionAuthorizations());
		add(new ActionAuthorizationListPanel("projectSpecificActionAuthorizations", bean) {
			
			@Override
			protected void onSaved(List<ActionAuthorization> actionAuthorizations) {
				getProject().getBuildSetting().setActionAuthorizations(actionAuthorizations);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(ProjectActionAuthorizationsPage.class, 
						ProjectActionAuthorizationsPage.paramsOf(getProject()));
			}
			
		});
		
		add(PropertyContext.view("inheritedActionAuthorizations", 
				getProject().getOwner().getBuildSetting(), "actionAuthorizations"));

		add(new SettingInOwnerLink("owner", projectModel, UserActionAuthorizationsPage.class, 
				MyActionAuthorizationsPage.class));
		
	}

}
