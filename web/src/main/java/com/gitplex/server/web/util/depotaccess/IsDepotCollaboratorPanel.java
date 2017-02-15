package com.gitplex.server.web.util.depotaccess;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.entity.UserAuthorization;
import com.gitplex.server.web.page.depot.setting.authorization.DepotCollaboratorListPage;

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
		
		add(new BookmarkablePageLink<Void>("collaborator", DepotCollaboratorListPage.class, 
				DepotCollaboratorListPage.paramsOf(authorizationModel.getObject().getDepot())));
		add(new Label("privilege", authorizationModel.getObject().getPrivilege()));
	}

	@Override
	protected void onDetach() {
		authorizationModel.detach();
		super.onDetach();
	}

}
