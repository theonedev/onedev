package com.pmease.gitplex.web.component.entityselector;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.component.avatar.Avatar;

@SuppressWarnings("serial")
public class AccountItemPanel extends GenericPanel<Account> {

	public AccountItemPanel(String id, IModel<Account> accountModel) {
		super(id, accountModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Avatar("avatar", getModelObject()));
		add(new Label("name", getModelObject().getDisplayName()));
	}

}
