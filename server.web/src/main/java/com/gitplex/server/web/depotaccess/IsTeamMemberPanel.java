package com.gitplex.server.web.depotaccess;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.core.entity.TeamMembership;
import com.gitplex.server.core.security.privilege.DepotPrivilege;
import com.gitplex.server.web.page.account.teams.TeamDepotListPage;
import com.gitplex.server.web.page.account.teams.TeamMemberListPage;

@SuppressWarnings("serial")
public class IsTeamMemberPanel extends Panel {

	private final IModel<TeamMembership> membershipModel;
	
	private final DepotPrivilege privilege;
	
	public IsTeamMemberPanel(String id, IModel<TeamMembership> membershipModel, DepotPrivilege privilege) {
		super(id);
		this.membershipModel = membershipModel;
		this.privilege = privilege;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<Void> memberListLink = new BookmarkablePageLink<Void>(
				"memberListLink", 
				TeamMemberListPage.class, 
				TeamMemberListPage.paramsOf(membershipModel.getObject().getTeam()));
		memberListLink.add(new Label("name", membershipModel.getObject().getTeam().getName()));
		add(memberListLink);
		
		Link<Void> depotListLink = new BookmarkablePageLink<Void>(
				"depotListLink", 
				TeamDepotListPage.class, 
				TeamDepotListPage.paramsOf(membershipModel.getObject().getTeam()));
		depotListLink.add(new Label("privilege", privilege));
		add(depotListLink);
	}

	@Override
	protected void onDetach() {
		membershipModel.detach();
		super.onDetach();
	}

}
