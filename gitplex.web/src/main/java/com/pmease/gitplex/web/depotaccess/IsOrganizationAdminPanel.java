package com.pmease.gitplex.web.depotaccess;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.OrganizationMembership;

@SuppressWarnings("serial")
public class IsOrganizationAdminPanel extends Panel {

	private final IModel<OrganizationMembership> membershipModel;
	
	public IsOrganizationAdminPanel(String id, IModel<OrganizationMembership> membershipModel) {
		super(id);
		this.membershipModel = membershipModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("organizationName", membershipModel.getObject().getOrganization().getName()));
	}

	@Override
	protected void onDetach() {
		membershipModel.detach();
		
		super.onDetach();
	}

}
