package com.gitplex.server.security;

import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.gitplex.server.GitPlex;

public class GitPlexWebEnvironment extends DefaultWebEnvironment implements Initializable, Destroyable {

	@Override
	public void init() throws ShiroException {
		setWebSecurityManager(GitPlex.getInstance(WebSecurityManager.class));
		setFilterChainResolver(GitPlex.getInstance(FilterChainResolver.class));
	}

}
