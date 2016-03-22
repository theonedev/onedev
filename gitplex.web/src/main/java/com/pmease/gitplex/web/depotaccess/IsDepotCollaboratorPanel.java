package com.pmease.gitplex.web.depotaccess;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.UserAuthorization;

@SuppressWarnings("serial")
public class IsDepotCollaboratorPanel extends Panel {

	private final IModel<UserAuthorization> authorizationModel;
	
	public IsDepotCollaboratorPanel(String id, IModel<UserAuthorization> authorizationModel) {
		super(id);
		this.authorizationModel = authorizationModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("privilege", authorizationModel.getObject().getPrivilege()));
	}

	@Override
	protected void onDetach() {
		authorizationModel.detach();
		super.onDetach();
	}

}
