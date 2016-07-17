package com.pmease.gitplex.web.page.account.collaborators;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.component.privilegesource.PrivilegeSourcePanel;

@SuppressWarnings("serial")
public class CollaboratorPrivilegeSourcePage extends CollaboratorPage {

	private static final String PARAM_DEPOT = "depot";
	
	private final IModel<Depot> depotModel;
	
	public CollaboratorPrivilegeSourcePage(PageParameters params) {
		super(params);
		
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
		
		add(new PrivilegeSourcePanel("privilegeSource", collaboratorModel, depotModel));
	}

	public static PageParameters paramsOf(Depot depot, Account collaborator) {
		PageParameters params = paramsOf(depot.getAccount(), collaborator);
		params.add(PARAM_DEPOT, depot.getName());
		return params;
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}
	
}
