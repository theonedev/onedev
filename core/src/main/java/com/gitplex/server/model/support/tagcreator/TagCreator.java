package com.gitplex.server.model.support.tagcreator;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable
public interface TagCreator extends Serializable {
	
	@Nullable
	String getNotMatchMessage(Depot depot, Account user);
	
}
