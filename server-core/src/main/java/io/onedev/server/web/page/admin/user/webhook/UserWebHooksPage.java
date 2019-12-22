package io.onedev.server.web.page.admin.user.webhook;

import java.util.ArrayList;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.web.component.webhook.WebHookListPanel;
import io.onedev.server.web.component.webhook.WebHooksBean;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserWebHooksPage extends UserPage {

	public UserWebHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebHooksBean bean = new WebHooksBean();
		bean.setWebHooks(getUser().getWebHooks());
		add(new WebHookListPanel("webHooks", bean) {
			
			@Override
			protected void onSaved(ArrayList<WebHook> webHooks) {
				getUser().setWebHooks(bean.getWebHooks());
				OneDev.getInstance(UserManager.class).save(getUser());
				setResponsePage(UserWebHooksPage.class, UserWebHooksPage.paramsOf(getUser()));
			}
			
		});
	}

}
