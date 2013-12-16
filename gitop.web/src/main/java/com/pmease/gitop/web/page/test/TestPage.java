package com.pmease.gitop.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

@SuppressWarnings("serial")
public class TestPage extends WebPage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(BootstrapHeaderItem.get());
	}
	
}
