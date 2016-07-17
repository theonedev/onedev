package com.pmease.gitplex.web.page.security;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.home.DashboardPage;

import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class RegisterPage extends BasePage {
	
	public RegisterPage() {
		if (getLoginUser() != null)
			throw new IllegalStateException("Can not sign up an account while signed in.");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		final Account account = new Account();
		final BeanEditor<?> editor = BeanContext.editBean("editor", account, 
				Sets.newHashSet("description", "defaultPrivilege"));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				AccountManager userManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = userManager.find(account.getName());
				if (accountWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					userManager.save(account, null);
					Session.get().success("New account registered");
					SecurityUtils.getSubject().runAs(account.getPrincipals());
					setResponsePage(AvatarEditPage.class, AccountPage.paramsOf(account));
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(DashboardPage.class);
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(RegisterPage.class, "register.css")));
	}

}
