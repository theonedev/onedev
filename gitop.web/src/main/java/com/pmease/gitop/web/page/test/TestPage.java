package com.pmease.gitop.web.page.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String info;
	
	public TestPage(PageParameters params) {
		info = params.get(0).toString();
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new AjaxLink<Void>("test") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				System.out.println(info);
			}
			
		});
	}

}
