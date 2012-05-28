package com.pmease.commons.product.pages;

import java.util.Date;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;

import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.product.model.User;

public class Index {
	
	@Inject
	private GeneralDao generalDao;

	@SessionState
	@Property
	private User user;
	
	public void onActionFromBtn() {
		user.setName(new Date().toString());
		user.setEmail("robin@pmease.com");
		generalDao.save(user);
	}
	
	public void onActivate(User user) {
		this.user = user;
	}
}
