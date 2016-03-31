package com.pmease.gitplex.web.page.account.overview;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.core.entity.Account;

@SuppressWarnings("serial")
public class OrganizationOverviewPanel extends GenericPanel<Account> {

	public OrganizationOverviewPanel(String id, IModel<Account> model) {
		super(id, model);
	}

	private Account getOrganization() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account user = getOrganization();
		add(new Label("title", user.getDisplayName()));
		add(new Label("loginName", user.getName()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(user.getFullName()!=null);				
			}
			
		});
		
		if (getOrganization().getDescription() != null) {
			add(new MarkdownViewer("description", Model.of(getOrganization().getDescription()), false));
		} else {
			add(new WebMarkupContainer("description").setVisible(false));
		}

		add(new DepotListPanel("depots", getModel()));
	}

}