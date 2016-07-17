package com.pmease.gitplex.web.page.admin.account;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;

import com.google.common.collect.Sets;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class NewUserPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account user = new Account();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", user, 
				Sets.newHashSet("defaultPrivilege", "description"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				AccountManager userManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = userManager.find(user.getName());
				if (accountWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					userManager.save(user, null);
					Session.get().success("New user account created");
					setResponsePage(AccountListPage.class);
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

}
