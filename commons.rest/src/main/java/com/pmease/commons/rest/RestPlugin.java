package com.pmease.commons.rest;

import java.util.Arrays;
import java.util.Collection;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.security.extensionpoint.FilterChainConfigurator;

public class RestPlugin extends AbstractPlugin {

	public static final String REST_PATH = "rest";
	
	@Override
	public Collection<?> getExtensions() {
		return Arrays.asList(
				new FilterChainConfigurator() {

					@Override
					public void configure(FilterChainManager filterChainManager) {
						filterChainManager.createChain("/" + REST_PATH + "/**", "noSessionCreation, authcBasic");
					}
					
				}
			);
	}

}
