package io.onedev.server.web.page.admin.usermanagement.profile;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.mail.MailManager;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.page.admin.usermanagement.UserPage;
import io.onedev.server.web.page.admin.usermanagement.password.UserPasswordPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserProfilePage extends UserPage {

	public UserProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer authSourceContainer;
		if (getUser().getPassword() != null) {
			authSourceContainer = new Fragment("authSource", "authViaInternalDatabaseFrag", this);
			authSourceContainer.add(new Link<Void>("removePassword") {

				@Override
				public void onClick() {
					getUser().setPassword(null);
					getUserManager().update(getUser(), null);
					Session.get().success("Password has been removed");
					setResponsePage(UserPasswordPage.class, paramsOf(getUser()));
				}
			}.add(new ConfirmClickModifier("Do you really want to remove password of this user?")));
		} else {
			authSourceContainer = new Fragment("authSource", "authViaExternalSystemFrag", this);
			authSourceContainer.add(new BookmarkablePageLink<Void>("setPasswordForUser",
					UserPasswordPage.class, UserPasswordPage.paramsOf(getUser())));
			var form = new Form<Void>("tellUserToResetPassword");
			authSourceContainer.add(form);
			var userId = getUser().getId();
			form.add(new TaskButton("tellUserToResetPassword") {

				@Override
				protected TaskResult runTask(TaskLogger logger) {
					var sessionManager = OneDev.getInstance(SessionManager.class);
					return sessionManager.call(() -> {
						SettingManager settingManager = OneDev.getInstance(SettingManager.class);
						if (settingManager.getMailService() != null) {
							var user = getUserManager().load(userId);
							if (user.getPrimaryEmailAddress() != null && user.getPrimaryEmailAddress().isVerified()) {
								String passwordResetCode = CryptoUtils.generateSecret();
								user.setPasswordResetCode(passwordResetCode);
								getUserManager().update(user, null);

								MailManager mailManager = OneDev.getInstance(MailManager.class);

								Map<String, Object> bindings = new HashMap<>();
								bindings.put("passwordResetUrl", settingManager.getSystemSetting().getServerUrl() + "/~reset-password/" + passwordResetCode);
								bindings.put("user", user);

								var template = settingManager.getEmailTemplates().getPasswordReset();
								var htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
								var textBody = EmailTemplates.evalTemplate(false, template, bindings);

								mailManager.sendMail(Arrays.asList(user.getPrimaryEmailAddress().getValue()),
										Lists.newArrayList(), Lists.newArrayList(),
										"[Reset Password] Please Reset Your OneDev Password",
										htmlBody, textBody, null, null, null);

								return new TaskResult(true, new TaskResult.PlainMessage("Password reset request has been sent"));
							} else {
								return new TaskResult(false, new TaskResult.PlainMessage("No verified primary email address"));
							}
						} else {
							return new TaskResult(false, new TaskResult.PlainMessage("Unable to notify user as mail service is not configured"));
						}
					});
				}

			});
		}
		add(authSourceContainer);
		
		add(new ProfileEditPanel("content", userModel));
	}

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
}
