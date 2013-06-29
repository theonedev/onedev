package com.pmease.gitop.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.pmease.commons.security.SecurityHelper;
import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
public class HomePage extends WebPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("user", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return SecurityHelper.getUserDisplayName(User.class, "Guest");
			}
			
		}));
		
		add(new Link<Void>("login") {

			@Override
			public void onClick() {
				SecurityUtils.getSubject().login(new UsernamePasswordToken("admin", "12345"));
			}
			
		});
		
		add(new Link<Void>("logout") {

			@Override
			public void onClick() {
				SecurityUtils.getSubject().logout();
			}
			
		});
		
		add(new Link<Void>("check") {

			@Override
			public void onClick() {
				SecurityUtils.getSubject().checkPermission("write");
			}
			
		});
		
	}
	
}