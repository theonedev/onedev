package com.pmease.gitplex.web.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.jackson.JsonOptions;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		JsonOptions options = new JsonOptions();
		options.put("offset_top", "$('.head').height();");
		System.out.println(options);
	}

}
