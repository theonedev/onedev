package com.gitplex.server.web.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.UserAuthorization;
import com.gitplex.server.core.manager.UserAuthorizationManager;
import com.gitplex.server.core.security.privilege.DepotPrivilege;

public class IsDepotCollaborator implements PrivilegeSource {

	private static final long serialVersionUID = 1L;

	private final Long authorizationId;
	
	private final DepotPrivilege privilege;
	
	public IsDepotCollaborator(UserAuthorization authorization) {
		this.authorizationId = authorization.getId();
		this.privilege = authorization.getPrivilege();
	}
	
	@Override
	public DepotPrivilege getPrivilege() {
		return privilege;
	}

	@Override
	public Component render(String componentId) {
		return new IsDepotCollaboratorPanel(componentId, new LoadableDetachableModel<UserAuthorization>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected UserAuthorization load() {
				return GitPlex.getInstance(UserAuthorizationManager.class).load(authorizationId);
			}
			
		});
	}

}
