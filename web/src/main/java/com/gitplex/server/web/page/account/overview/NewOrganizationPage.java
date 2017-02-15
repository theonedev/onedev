package com.gitplex.server.web.page.account.overview;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.OrganizationMembership;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.OrganizationMembershipManager;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.editable.BeanContext;
import com.gitplex.server.web.editable.BeanEditor;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.page.account.AccountLayoutPage;

@SuppressWarnings("serial")
public class NewOrganizationPage extends AccountLayoutPage {

	public NewOrganizationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account organization = new Account();
		organization.setOrganization(true);
		OrganizationMembership membership = new OrganizationMembership();
		membership.setAdmin(true);
		membership.setUser(getAccount());
		membership.setOrganization(organization);
		
		BeanEditor<?> editor = BeanContext.editBean("editor", membership.getOrganization(), Account.getOrganizationExcludeProperties()); 
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Account organization = membership.getOrganization();
				AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = accountManager.findByName(organization.getName());
				if (accountWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					GitPlex.getInstance(OrganizationMembershipManager.class).save(membership);
					Session.get().success("New organization created");
					setResponsePage(AccountOverviewPage.class, AccountOverviewPage.paramsOf(organization));
				}
			}
			
		};
		form.add(editor);
		
		add(form);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
