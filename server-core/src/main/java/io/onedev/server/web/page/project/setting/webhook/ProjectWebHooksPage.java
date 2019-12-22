package io.onedev.server.web.page.project.setting.webhook;

import java.util.ArrayList;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.web.component.link.SettingInOwnerLink;
import io.onedev.server.web.component.webhook.WebHookListPanel;
import io.onedev.server.web.component.webhook.WebHooksBean;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.user.webhook.UserWebHooksPage;
import io.onedev.server.web.page.my.webhook.MyWebHooksPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class ProjectWebHooksPage extends ProjectSettingPage {

	public ProjectWebHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebHooksBean bean = new WebHooksBean();
		bean.setWebHooks(getProject().getWebHooks());
		add(new WebHookListPanel("projectSpecificWebHooks", bean) {
			
			@Override
			protected void onSaved(ArrayList<WebHook> webHooks) {
				getProject().setWebHooks(webHooks);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(ProjectWebHooksPage.class, ProjectWebHooksPage.paramsOf(getProject()));
			}
			
		});
		
		add(PropertyContext.view("inheritedWebHooks", getProject().getOwner(), "webHooks"));
		add(new SettingInOwnerLink("owner", projectModel, UserWebHooksPage.class, MyWebHooksPage.class));
	}

}
