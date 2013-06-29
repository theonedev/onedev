package com.pmease.gitop;

import javax.inject.Singleton;

import org.apache.wicket.Page;

import com.pmease.commons.web.AbstractWicketConfig;
import com.pmease.gitop.web.HomePage;

@Singleton
public class WicketConfig extends AbstractWicketConfig {

	@Override
	protected void init() {
		super.init();
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
