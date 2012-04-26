package com.pmease.commons.product;

import org.apache.wicket.Page;

import com.pmease.commons.wicket.AbstractWicketConfig;
import com.pmease.commons.product.web.HomePage;

public class WicketConfig extends AbstractWicketConfig {

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
