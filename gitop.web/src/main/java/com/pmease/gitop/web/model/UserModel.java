package com.pmease.gitop.web.model;

import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;

public class UserModel extends EntityModel<User> {

	private static final long serialVersionUID = 1L;

	public UserModel(User entity) {
		super(entity);
	}

	@Override
	protected GenericDao<User> getDao() {
		return AppLoader.getInstance(UserManager.class);
	}

}
