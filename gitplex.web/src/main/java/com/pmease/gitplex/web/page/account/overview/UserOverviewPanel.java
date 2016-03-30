package com.pmease.gitplex.web.page.account.overview;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.depots.NewDepotPage;
import com.pmease.gitplex.web.page.account.organizations.NewOrganizationPage;

@SuppressWarnings("serial")
public class UserOverviewPanel extends GenericPanel<Account> {

	public UserOverviewPanel(String id, IModel<Account> model) {
		super(id, model);
	}

	private Account getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account user = getUser();
		add(new Label("title", user.getDisplayName()));
		add(new Label("loginName", user.getName()).setVisible(user.getFullName()!=null));
		add(new Label("email", user.getEmail())
				.add(AttributeAppender.append("href", "mailto:" + user.getEmail())));
		
		Link<Void> link = new BookmarkablePageLink<Void>("addOrganization", 
				NewOrganizationPage.class, NewOrganizationPage.paramsOf(getUser()));
		link.setVisible(SecurityUtils.canManage(user));
		add(link);
		
		link = new BookmarkablePageLink<Void>("addDepot", 
				NewDepotPage.class, NewDepotPage.paramsOf(getUser()));
		link.setVisible(SecurityUtils.canManage(user));
		add(link);
	}

}
