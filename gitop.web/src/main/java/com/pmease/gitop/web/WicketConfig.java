package com.pmease.gitop.web;

import javax.inject.Singleton;

import org.apache.wicket.Page;

import com.pmease.commons.wicket.AbstractWicketConfig;

@Singleton
public class WicketConfig extends AbstractWicketConfig {

	@Override
	protected void init() {
		super.init();
		
		mountPage("/", HomePage.class);
		mountPage("/init", ServerInitPage.class);
		
		mountPage("/test", TestPage.class);
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
