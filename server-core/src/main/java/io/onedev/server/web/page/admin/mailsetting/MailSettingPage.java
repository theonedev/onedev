package io.onedev.server.web.page.admin.mailsetting;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.notification.MailManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class MailSettingPage extends AdministrationPage {

	public MailSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MailSettingHolder mailSettingHolder = new MailSettingHolder();
		mailSettingHolder.setMailSetting(OneDev.getInstance(SettingManager.class).getMailSetting());
		
		BeanEditor editor = BeanContext.edit("editor", mailSettingHolder);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(SettingManager.class).saveMailSetting(mailSettingHolder.getMailSetting());
				getSession().success("Mail setting has been saved");
			}
			
		};
		TaskButton testButton = new TaskButton("sendingTestMail") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor mailSettingEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

					public void component(BeanEditor component, IVisit<BeanEditor> visit) {
						visit.stop(component);
					}
					
				});
				setVisible(mailSettingEditor != null && mailSettingEditor.isVisibleInHierarchy());
			}

			@Override
			protected String runTask(SimpleLogger logger) {
				User user = SecurityUtils.getUser();
				
				String body = "Great, your mail setting is working!";
				OneDev.getInstance(MailManager.class).sendMail(mailSettingHolder.getMailSetting(), 
						Sets.newHashSet(user.getEmail()), "Test email from OneDev", body, body);
				return "Test mail has been sent to " + user.getEmail() + ", please check your mail box";
			}

		};
		
		Form<?> form = new Form<Void>("mailSetting") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof BeanUpdating) {
					BeanUpdating beanUpdating = (BeanUpdating) event.getPayload();
					beanUpdating.getHandler().add(testButton);
				}
				
			}

		};
		
		form.add(editor);
		form.add(saveButton);
		form.add(testButton);
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Mail Setting");
	}

}