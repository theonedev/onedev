package io.onedev.server.web.page.my.webhook;

import java.util.ArrayList;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.web.component.webhook.WebHookListPanel;
import io.onedev.server.web.component.webhook.WebHooksBean;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyWebHooksPage extends MyPage {

	public MyWebHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebHooksBean bean = new WebHooksBean();
		bean.setWebHooks(getLoginUser().getWebHooks());
		add(new WebHookListPanel("webHooks", bean) {
			
			@Override
			protected void onSaved(ArrayList<WebHook> webHooks) {
				getLoginUser().setWebHooks(bean.getWebHooks());
				OneDev.getInstance(UserManager.class).save(getLoginUser());
				setResponsePage(MyWebHooksPage.class);
			}
			
		});
	}

}
