package com.pmease.gitop.web.page.account.setting.profile;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.textfield.TextFieldElement;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountProfilePage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Your Profile";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.PROFILE;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ProfileForm("form", new UserModel(getAccount())));
	}
	
	private class ProfileForm extends Form<User> {

		public ProfileForm(String id, IModel<User> model) {
			super(id, model);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			@SuppressWarnings("unchecked")
			final IModel<User> model = (IModel<User>) getDefaultModel();
			
			add(new FeedbackPanel("feedback", this));
			add(new TextFieldElement<String>("displayName", "Display Name",
					new PropertyModel<String>(model, "displayName"))
					.setRequired(false)
					.add(new PropertyValidator<String>()));
			add(new TextFieldElement<String>("email", "Email Address",
					new PropertyModel<String>(model, "email"))
					.add(new PropertyValidator<String>()));
			
			add(new AjaxButton("submit", this) {
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(form);
				}
				
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					User user = model.getObject();
					AppLoader.getInstance(UserManager.class).save(user);
					form.success("Your profile has been updated");
					target.add(form);
				}
			});
		}
	}
}
