package io.onedev.server.security;

import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import io.onedev.server.OneDev;

public class DefaultWebEnvironment extends org.apache.shiro.web.env.DefaultWebEnvironment implements Initializable, Destroyable {

	@Override
	public void init() throws ShiroException {
		setWebSecurityManager(OneDev.getInstance(WebSecurityManager.class));
		setFilterChainResolver(OneDev.getInstance(FilterChainResolver.class));
	}

}
