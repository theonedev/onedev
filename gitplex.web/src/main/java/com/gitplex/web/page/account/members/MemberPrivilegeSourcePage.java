package com.gitplex.web.page.account.members;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.OrganizationMembership;
import com.gitplex.core.manager.DepotManager;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.web.component.privilegesource.PrivilegeSourcePanel;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class MemberPrivilegeSourcePage extends MemberPage {

	private static final String PARAM_DEPOT = "depot";
	
	private final IModel<Depot> depotModel;
	
	public MemberPrivilegeSourcePage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
		
		String depotName = params.get(PARAM_DEPOT).toString();
		depotModel = new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				return Preconditions.checkNotNull(
						GitPlex.getInstance(DepotManager.class).find(getAccount(), depotName));
			}
			
		};
		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PrivilegeSourcePanel("privilegeSource", new AbstractReadOnlyModel<Account>() {

			@Override
			public Account getObject() {
				return getMembership().getUser();
			}
			
		}, depotModel));
	}

	public static PageParameters paramsOf(OrganizationMembership membership, Depot depot) {
		PageParameters params = paramsOf(membership);
		params.add(PARAM_DEPOT, depot.getName());
		return params;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}
	
	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}
	
}
