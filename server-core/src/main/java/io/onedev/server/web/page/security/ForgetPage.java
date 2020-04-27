package io.onedev.server.web.page.security;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.validator.constraints.NotEmpty;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.notification.MailManager;
import io.onedev.server.util.JobLogger;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class ForgetPage extends BasePage {

	public ForgetPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		HelperBean bean = new HelperBean();
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));		
		form.add(BeanContext.edit("editor", bean));
		
		form.add(new TaskButton("resettingPassword") {
			
			@Override
			protected String runTask(JobLogger logger) {
				UserManager userManager = OneDev.getInstance(UserManager.class);
				User user = userManager.findByName(bean.getUserNameOrEmailAddress());
				if (user == null) {
					user = userManager.findByEmail(bean.getUserNameOrEmailAddress());
				}
				if (user == null) {
					throw new OneException("No user found with name or email: " + bean.getUserNameOrEmailAddress());
				} else {
					SettingManager settingManager = OneDev.getInstance(SettingManager.class);
					if (settingManager.getMailSetting() != null) {
						String password = RandomStringUtils.random(10, true, true);								
						user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(password));
						userManager.save(user);
						
						MailManager mailManager = OneDev.getInstance(MailManager.class);
						String mailBody = String.format("Dear %s, "
							+ "<p style='margin: 16px 0;'>"
							+ "Per your request, password of your user \"%s\" has been reset to:<br>"
							+ "%s<br><br>"
							+ "Please login and change the password in your earliest convenience."
							+ "<p style='margin: 16px 0;'>"
							+ "-- Sent by OneDev", 
							user.getDisplayName(), user.getName(), password);

						mailManager.sendMail(settingManager.getMailSetting(), Arrays.asList(user.getEmail()), 
								"Your OneDev password has been reset", mailBody);
						return "Please check your email " + user.getEmail() + " for the reset password";
					} else {
						throw new OneException("Unable to send password reset email as smtp setting is not defined");
					}
				}
			}
			
		});
		
		add(form);
	}

	@Editable
	public static class HelperBean implements Serializable {
		
		private String userNameOrEmailAddress;

		@Editable(name="Please specify your user name or email address")
		@NotEmpty
		public String getUserNameOrEmailAddress() {
			return userNameOrEmailAddress;
		}

		public void setUserNameOrEmailAddress(String userNameOrEmailAddress) {
			this.userNameOrEmailAddress = userNameOrEmailAddress;
		}
		
	}
}
