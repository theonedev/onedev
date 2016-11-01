package com.gitplex.web.page.security;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.manager.AccountManager;
import com.gitplex.core.manager.ConfigManager;
import com.gitplex.core.manager.MailManager;
import com.gitplex.web.page.base.BasePage;
import com.gitplex.commons.wicket.behavior.testform.TestFormBehavior;
import com.gitplex.commons.wicket.behavior.testform.TestResult;
import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.commons.wicket.editable.annotation.Editable;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class ForgetPage extends BasePage {

	private static final Logger logger = LoggerFactory.getLogger(ForgetPage.class);
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final HelperBean bean = new HelperBean();
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));		
		form.add(BeanContext.editBean("editor", bean));
		
		form.add(new AjaxButton("reset") {
			private TestFormBehavior testBehavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(testBehavior = new TestFormBehavior() {

					@Override
					protected TestResult test() {
						AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
						Account user = accountManager.findByName(bean.getUserNameOrEmailAddress());
						if (user == null) {
							user = accountManager.findByEmail(bean.getUserNameOrEmailAddress());
						}
						if (user == null) {
							return new TestResult.Failed("No account found with name or email: " + bean.getUserNameOrEmailAddress());
						} else {
							ConfigManager configManager = GitPlex.getInstance(ConfigManager.class);
							if (configManager.getMailSetting() != null) {
								String password = RandomStringUtils.random(10, true, true);								
								user.setPassword(password);
								accountManager.save(user);
								
								MailManager mailManager = GitPlex.getInstance(MailManager.class);
								try {
									String mailBody = String.format("Dear %s, "
										+ "<p style='margin: 16px 0;'>"
										+ "Per your request, password of your account \"%s\" has been reset to:<br>"
										+ "%s<br><br>"
										+ "Please login and change the password in your earliest convenience."
										+ "<p style='margin: 16px 0;'>"
										+ "-- Sent by GitPlex", 
										user.getDisplayName(), user.getName(), password);

									mailManager.sendMail(configManager.getMailSetting(), Arrays.asList(user.getEmail()), 
											"Your GitPlex password has been reset", mailBody);
									return new TestResult.Successful("Please check your email " + user.getEmail() + " for the reset password.");
								} catch (Exception e) {
									logger.error("Error sending password reset email", e);
									return new TestResult.Failed("Error sending password reset email: " + e.getMessage());
								}
							} else {
								return new TestResult.Failed("Unable to send password reset email as smtp setting is not defined");
							}
						}
					}
					
				});
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				target.add(form);
				target.focusComponent(null);
				testBehavior.requestTest(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
		});
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new ForgetResourceReference()));
	}

	@Editable
	public static class HelperBean implements Serializable {
		
		private String userNameOrEmailAddress;

		@Editable(name="Please specify your account name or email address")
		@NotEmpty
		public String getUserNameOrEmailAddress() {
			return userNameOrEmailAddress;
		}

		public void setUserNameOrEmailAddress(String userNameOrEmailAddress) {
			this.userNameOrEmailAddress = userNameOrEmailAddress;
		}
		
	}
}
