package com.pmease.commons.wicket.page;

import org.apache.wicket.markup.html.WebPage;

import com.pmease.commons.wicket.asset.Asset;

public class CommonPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		/*
		 * Contribute common javascript and css via a separate component to make sure these header items appears 
		 * before any other header items.
		 */
		add(new Asset("asset"));
	}

}
