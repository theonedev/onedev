package com.pmease.gitop.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.loader.AbstractPlugin;

public class Product extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(Product.class);
	
	public static final String NAME = "Gitop";
	
	@Override
	public void postStart() {
		logger.info(NAME + " has been started successfully.");
	}

}
