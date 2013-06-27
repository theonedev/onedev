package com.pmease.commons.product;

import javax.inject.Singleton;

import org.apache.wicket.Page;

import com.pmease.commons.product.web.HomePage;
import com.pmease.commons.web.AbstractWicketConfig;

@Singleton
public class WicketConfig extends AbstractWicketConfig {

	@Override
	protected void init() {
		super.init();
		
		mountPage("/test", HomePage.class);
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
