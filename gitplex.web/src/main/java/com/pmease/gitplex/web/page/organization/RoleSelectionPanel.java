package com.pmease.gitplex.web.page.organization;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class RoleSelectionPanel extends Panel {

	public RoleSelectionPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("admin") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelectAdmin(target);
			}
			
		});
		add(new AjaxLink<Void>("member") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onSelectMember(target);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	protected abstract void onSelectAdmin(AjaxRequestTarget target);
	
	protected abstract void onSelectMember(AjaxRequestTarget target);
}
