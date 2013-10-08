package com.pmease.gitop.web.page.account.setting.password;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.messenger.Messenger;
import com.pmease.gitop.web.common.form.passwordfield.PasswordFieldElement;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountPasswordPage extends AccountSettingPage {

	private String oldPass;
	private String newPass;
	private String confirmPass;
	
	@Override
	protected String getPageTitle() {
		return "Change Password";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.PASSWORD;
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		Form<User> form = new Form<User>("form", new UserModel(getAccount()));
		add(form);
		form.add(new PasswordFieldElement("oldPass", "Current Password", 
				new PropertyModel<String>(this, "oldPass"))
				.add(new IValidator<String>() {

					@Override
					public void validate(IValidatable<String> validatable) {
						String pass = validatable.getValue();
						PasswordService ps = AppLoader.getInstance(PasswordService.class);
						User user = getAccount();
						if (!ps.passwordsMatch(pass, user.getPasswordHash())) {
							validatable.error(new ValidationError().setMessage("The password is wrong"));
						}
					}
				})
		);
		
		PasswordFieldElement newPassField = new PasswordFieldElement("newPass", "New Password",
				new PropertyModel<String>(this, "newPass"));
		newPassField.setHelp("Use at least six characters.");
		form.add(newPassField);
		
		PasswordFieldElement confirmPassField = new PasswordFieldElement("confirmPass", "Confirmed Password",
				new PropertyModel<String>(this, "confirmPass"));
		form.add(confirmPassField);
		
		form.add(new EqualPasswordInputValidator(
				newPassField.getFormComponent(),
				confirmPassField.getFormComponent()));
		
		form.add(new AjaxFallbackButton("submit", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				PasswordService ps = AppLoader.getInstance(PasswordService.class);
				User account = getAccount();
				account.setPasswordHash(ps.encryptPassword(newPass));
				AppLoader.getInstance(UserManager.class).save(account);
				if (target != null) {
					oldPass = null;
					newPass = null;
					confirmPass = null;
					target.add(form);
					Messenger.success("Your password has been changed successfully.").execute(target);
				}
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				if (target != null)
					target.add(form);
			}
		});
		
	}

	public String getOldPass() {
		return oldPass;
	}

	public void setOldPass(String oldPass) {
		this.oldPass = oldPass;
	}

	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public String getConfirmPass() {
		return confirmPass;
	}

	public void setConfirmPass(String confirmPass) {
		this.confirmPass = confirmPass;
	}
}
