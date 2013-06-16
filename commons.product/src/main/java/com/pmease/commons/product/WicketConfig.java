package com.pmease.commons.product;

import javax.inject.Singleton;

import org.apache.wicket.Page;

import com.pmease.commons.product.web.HomePage;
import com.pmease.commons.product.web.TestPage;
import com.pmease.commons.wicket.AbstractWicketConfig;

@Singleton
public class WicketConfig extends AbstractWicketConfig {

	@Override
	protected void init() {
		super.init();
		
		mountPage("/test", TestPage.class);
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
