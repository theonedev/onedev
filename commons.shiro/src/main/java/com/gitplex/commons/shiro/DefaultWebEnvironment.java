package com.gitplex.commons.shiro;

import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.gitplex.commons.loader.AppLoader;

public class DefaultWebEnvironment extends org.apache.shiro.web.env.DefaultWebEnvironment 
		implements Initializable, Destroyable {

	@Override
	public void init() throws ShiroException {
		setWebSecurityManager(AppLoader.getInstance(WebSecurityManager.class));
		setFilterChainResolver(AppLoader.getInstance(FilterChainResolver.class));
	}

}
