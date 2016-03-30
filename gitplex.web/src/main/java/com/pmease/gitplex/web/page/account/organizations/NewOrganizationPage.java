package com.pmease.gitplex.web.page.account.organizations;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

import jersey.repackaged.com.google.common.collect.Sets;

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
		
		BeanEditor<?> editor = BeanContext.editBean("editor", membership.getOrganization(), 
				Sets.newHashSet("email", "password"));
		
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
		if (account.isOrganization())
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
		else
			setResponsePage(OrganizationListPage.class, paramsOf(account));
	}

}
