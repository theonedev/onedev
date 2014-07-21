package com.pmease.gitplex.web.model;

import com.pmease.gitplex.core.model.User;

/** shortcut to {@link EntityModel}<User> */
public class UserModel extends EntityModel<User> {

	private static final long serialVersionUID = 1L;

	public UserModel(User entity) {
		super(entity);
	}

}
