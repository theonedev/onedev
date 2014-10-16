package com.pmease.commons.wicket.component.navigator;

import org.apache.wicket.markup.html.navigation.paging.IPageable;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class PagingNavigator extends BootstrapPagingNavigator {

	public PagingNavigator(String markupId, IPageable pageable) {
		super(markupId, pageable);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

}
