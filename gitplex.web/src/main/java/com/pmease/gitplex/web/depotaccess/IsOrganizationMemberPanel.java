package com.pmease.gitplex.web.depotaccess;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;

@SuppressWarnings("serial")
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
				ProfileEditPage.class, ProfileEditPage.paramsOf(organization)) {

			@Override
			public String getAfterDisabledLink() {
				return "";
			}

			@Override
			public String getBeforeDisabledLink() {
				return "";
			}
			
		};
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
