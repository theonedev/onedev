package com.pmease.commons.product;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;

import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.product.model.User;
import com.pmease.commons.security.AbstractRealm;

@Singleton
public class UserRealm extends AbstractRealm<User> {

	@Inject
	public UserRealm(Provider<GeneralDao> generalDaoProvider, CredentialsMatcher credentialsMatcher) {
		super(generalDaoProvider, credentialsMatcher);
	}

	@Override
	protected Collection<String> doGetPermissions(Long userId) {
		return Arrays.asList("*");
	}

}
