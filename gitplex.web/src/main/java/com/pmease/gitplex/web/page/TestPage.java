package com.pmease.gitplex.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.markup.html.link.Link;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				SecurityUtils.getSubject().login(new UsernamePasswordToken("System", ""));
			}
			
		});
	}
	
}
