package com.pmease.gitplex.web.page.organization;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

@SuppressWarnings("serial")
public abstract class PrivilegeSelectionPanel extends Panel {

	public PrivilegeSelectionPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("read") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, DepotPrivilege.READ);
			}
			
		});
		add(new AjaxLink<Void>("write") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, DepotPrivilege.WRITE);
			}
			
		});
		add(new AjaxLink<Void>("admin") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelect(target, DepotPrivilege.ADMIN);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	protected abstract void onSelect(AjaxRequestTarget target, DepotPrivilege privilege);
	
}
