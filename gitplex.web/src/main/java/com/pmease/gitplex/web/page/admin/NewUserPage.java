package com.pmease.gitplex.web.page.admin;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.collect.Sets;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;

@SuppressWarnings("serial")
public class NewUserPage extends AdministrationPage {

	private final Account account;
	
	public NewUserPage(Account account) {
		this.account = account;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor<?> editor = BeanContext.editBean("editor", account, 
				Sets.newHashSet("defaultPrivilege", "description"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				AccountManager userManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = userManager.findByName(account.getName());
				if (accountWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					userManager.save(account, null);
					Session.get().success("New account created");
					setResponsePage(UserListPage.class);
				}
			}
			
		};
		form.add(editor);
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(UserListPage.class);
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(UserListPage.class, "accounts.css")));
	}

}
