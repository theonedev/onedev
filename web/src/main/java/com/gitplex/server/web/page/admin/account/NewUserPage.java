package com.gitplex.server.web.page.admin.account;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class NewUserPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account user = new Account();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", user, Account.getUserExcludeProperties());
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = accountManager.findByName(user.getName());
				if (accountWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} 
				Account accountWithSameEmail = accountManager.findByEmail(user.getEmail());
				if (accountWithSameEmail != null) {
					editor.getErrorContext(new PathSegment.Property("email"))
							.addError("This email has already been used by another account.");
				} 
				if (!editor.hasErrors(true)){
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(user.getPassword()));
					accountManager.save(user, null);
					Session.get().success("New user account created");
					setResponsePage(UserListPage.class);
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

}
