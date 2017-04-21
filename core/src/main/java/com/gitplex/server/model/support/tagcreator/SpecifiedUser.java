package com.gitplex.server.model.support.tagcreator;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.util.editable.annotation.Editable;
import com.google.common.base.Preconditions;

@Editable(order=400, name="Specified User")
public class SpecifiedUser implements TagCreator {

	private static final long serialVersionUID = 1L;

	private String userName;

	@Editable(name="User")
	@AccountChoice(type=AccountChoice.Type.DEPOT_WRITER)
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String getNotMatchMessage(Depot depot, Account user) {
		AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
		Account specifiedUser = Preconditions.checkNotNull(accountManager.findByName(userName));
		if (!specifiedUser.equals(user)) 
			return "This operation can only be performed by user: " + userName;
		else 
			return null;
	}

}
