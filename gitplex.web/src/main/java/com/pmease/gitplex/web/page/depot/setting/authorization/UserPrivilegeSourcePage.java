package com.pmease.gitplex.web.page.depot.setting.authorization;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.component.privilegesource.PrivilegeSourcePanel;

@SuppressWarnings("serial")
public class UserPrivilegeSourcePage extends DepotAuthorizationPage {

	private static final String PARAM_USER = "user";
	
	private final IModel<Account> userModel;
	
	public UserPrivilegeSourcePage(PageParameters params) {
		super(params);
		
		String userName = params.get(PARAM_USER).toString();
		userModel = new LoadableDetachableModel<Account>() {

			@Override
			protected Account load() {
				return Preconditions.checkNotNull(
						GitPlex.getInstance(AccountManager.class).findByName(userName));
			}
			
		};
		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PrivilegeSourcePanel("privilegeSource", userModel, depotModel));
	}

	public static PageParameters paramsOf(Depot depot, Account user) {
		PageParameters params = paramsOf(depot);
		params.add(PARAM_USER, user.getName());
		return params;
	}
	
	@Override
	protected void onDetach() {
		userModel.detach();
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		if (depot.getAccount().isOrganization())
			setResponsePage(DepotEffectivePrivilegePage.class, DepotEffectivePrivilegePage.paramsOf(depot));
		else
			setResponsePage(DepotCollaboratorListPage.class, DepotCollaboratorListPage.paramsOf(depot));
	}
	
}
