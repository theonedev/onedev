package com.gitplex.server.web.util.depotaccess;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.OrganizationMembership;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.page.account.setting.ProfileEditPage;

public class IsOrganizationMemberPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private final IModel<OrganizationMembership> membershipModel;
	
	public IsOrganizationMemberPanel(String id, IModel<OrganizationMembership> membershipModel) {
		super(id);
		this.membershipModel = membershipModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Account organization = membershipModel.getObject().getOrganization();
		add(new Label("organizationName", organization.getDisplayName()));
		
		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("organizationSetting", 
				ProfileEditPage.class, ProfileEditPage.paramsOf(organization));
		link.add(new Label("privilegeName", organization.getDefaultPrivilege()));
		link.setEnabled(SecurityUtils.canManage(organization));
		add(link);
	}

	@Override
	protected void onDetach() {
		membershipModel.detach();
		super.onDetach();
	}
	
}
