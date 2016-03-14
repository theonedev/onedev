package com.pmease.gitplex.web.page.user.organizations;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.MembershipManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;

import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class NewOrganizationPage extends AccountLayoutPage {

	private final Membership membership;
	
	public NewOrganizationPage(Membership membership) {
		super(paramsOf(membership.getUser()));
		
		this.membership = membership;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
					GitPlex.getInstance(MembershipManager.class).save(membership);
					Session.get().success("New organization created");
					setResponsePage(AccountOverviewPage.class, AccountOverviewPage.paramsOf(organization));
				}
			}
			
		};
		form.add(editor);
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(OrganizationListPage.class, paramsOf(getAccount()));
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				NewOrganizationPage.class, "organization-list.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
		else
			setResponsePage(OrganizationListPage.class, paramsOf(account));
	}

}
