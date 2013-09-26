package com.pmease.gitop.web.page.account.setting.profile;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.form.FeedbackPanel;
import com.pmease.gitop.web.common.form.textfield.TextFieldElement;
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
		
	}
	
	private class ProfileForm extends Form<Void> {

		public ProfileForm(String id, IModel<Void> model) {
			super(id, model);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			add(new FeedbackPanel("feedback", this));
			add(new TextFieldElement<String>("displayName", "Display Name"))
		}
	}
}
