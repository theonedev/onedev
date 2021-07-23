package io.onedev.server.web.page.admin.notificationtemplatesetting;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class PullRequestNotificationTemplatePage extends AdministrationPage {

	public PullRequestNotificationTemplatePage(PageParameters params) {
		super(params);
	}

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		NotificationTemplateSetting setting = getSettingManager().getNotificationTemplateSetting();
		
		Map<String, String> variableHelp = new LinkedHashMap<>();
		variableHelp.put("pullRequest", " represents the <a href='https://code.onedev.io/projects/onedev-server/blob/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified");
		add(new Label("notificationTemplateHelp", NotificationTemplateSetting.getTemplateHelp(variableHelp))
				.setEscapeModelStrings(false));
		
		BeanEditor editor = BeanContext.edit("editor", setting, Lists.newArrayList(NotificationTemplateSetting.PROP_PULL_REQUEST_NOTIFICATION_TEMPLATE), false);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				getSettingManager().saveNotificationTemplateSetting(setting);
				getSession().success("Issue notification template has been saved");
			}
			
		};
		
		Link<?> useDefaultLink;
		add(useDefaultLink = new Link<Void>("useDefault") {

			@Override
			public void onClick() {
				setting.setIssueNotificationTemplate(NotificationTemplateSetting.DEFAULT_TEMPLATE);
				getSettingManager().saveNotificationTemplateSetting(setting);
				setResponsePage(PullRequestNotificationTemplatePage.class);
			}

		});
		
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		form.add(saveButton);
		form.add(useDefaultLink);
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Pull Request Notification Template");
	}

}