package com.pmease.gitplex.web.page.admin;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.manager.UserManager;

@SuppressWarnings("serial")
public class NewAccountPage extends AdministrationPage {

	private final User account;
	
	public NewAccountPage(User account) {
		this.account = account;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final BeanEditor<?> editor = BeanContext.editBean("editor", account);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				User accountWithSameName = userManager.findByName(account.getName());
				if (accountWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					userManager.save(account);
					Session.get().success("New account created");
					setResponsePage(AccountListPage.class);
				}
			}
			
		};
		form.add(editor);
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(AccountListPage.class);
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(AccountListPage.class, "accounts.css")));
	}

}
