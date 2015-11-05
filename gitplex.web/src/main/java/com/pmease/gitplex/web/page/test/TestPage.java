package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.wicket.ConfirmOnClick;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				// TODO Auto-generated method stub
				return super.getOnClickScript(url);
			}

			@Override
			public void onClick() {
				System.out.println("clicked");
			}
			
		}.add(new ConfirmOnClick("'lalala'")));
	}

}
