package com.pmease.gitplex.web.page.organization;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.manager.MembershipManager;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;

@SuppressWarnings("serial")
public class MembershipPage extends AccountLayoutPage {

	private static final String PARAM_MEMBERSHIP = "membership";
	
	private final IModel<Membership> membershipModel;
	
	public MembershipPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
		membershipModel = new LoadableDetachableModel<Membership>() {

			@Override
			protected Membership load() {
				return GitPlex.getInstance(MembershipManager.class).load(params.get(PARAM_MEMBERSHIP).toLong());
			}
			
		};
	}

	public static PageParameters paramsOf(Membership membership) {
		PageParameters params = paramsOf(membership.getOrganization());
		params.set(PARAM_MEMBERSHIP, membership.getId());
		return params;
	}
	
	@Override
	protected void onDetach() {
		membershipModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(MemberListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
