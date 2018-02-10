package com.turbodev.server.security;

import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.turbodev.server.TurboDev;

public class TurboDevWebEnvironment extends DefaultWebEnvironment implements Initializable, Destroyable {

	@Override
	public void init() throws ShiroException {
		setWebSecurityManager(TurboDev.getInstance(WebSecurityManager.class));
		setFilterChainResolver(TurboDev.getInstance(FilterChainResolver.class));
	}

}
