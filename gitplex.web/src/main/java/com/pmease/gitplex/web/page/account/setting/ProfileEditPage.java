package com.pmease.gitplex.web.page.account.setting;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class ProfileEditPage extends AccountSettingPage {

	public ProfileEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		sidebar.add(new Label("title", "Edit Profile of " + getAccount().getDisplayName()));
		
		final User account = getAccount();
		
		final BeanEditor<?> editor = BeanContext.editBean("editor", account);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				User accountWithSameEmail = userManager.findByEmail(account.getEmail());
				User accountWithSameName = userManager.findByName(account.getName());
				boolean hasError = false;
				if (accountWithSameName != null && !accountWithSameName.equals(account)) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
					hasError = true;
				}
				if (accountWithSameEmail != null && !accountWithSameEmail.equals(account)) {
					editor.getErrorContext(new PathSegment.Property("email"))
							.addError("This email has already been used by another account.");
					hasError = true;
				}
				
				if (!hasError) {
					editor.getBeanDescriptor().copyProperties(account, getAccount());
					userManager.save(getAccount());
					Session.get().info("Profile has been updated");
					setResponsePage(ProfileEditPage.class, AccountPage.paramsOf(getAccount()));
					backToPrevPage();
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(prevPageRef != null);
			}

			@Override
			public void onClick() {
				backToPrevPage();
			}
			
		});
		sidebar.add(form);
	}

}
