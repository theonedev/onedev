package com.pmease.gitop.web.page.account;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public abstract class AbstractAccountPage extends AbstractLayoutPage {

	protected final IModel<User> accountModel;
	
	public AbstractAccountPage(PageParameters params) {
		String name = params.get(PageSpec.USER).toString();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		
		User user = Gitop.getInstance(UserManager.class).findByName(name);
		if (user == null) {
			throw (new EntityNotFoundException("User " + name + " not found"));
		}
		
		this.accountModel = new UserModel(user);
	}

	@Override
	public void onDetach() {
		if (accountModel != null) {
			accountModel.detach();
		}
		
		super.onDetach();
	}
	
	public User getAccount() {
		return accountModel.getObject();
	}
}
