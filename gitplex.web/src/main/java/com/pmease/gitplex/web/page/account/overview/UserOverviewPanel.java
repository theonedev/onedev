package com.pmease.gitplex.web.page.account.overview;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;

@SuppressWarnings("serial")
public class UserOverviewPanel extends GenericPanel<Account> {

	public UserOverviewPanel(String id, IModel<Account> model) {
		super(id, model);
	}

}
