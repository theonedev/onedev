package com.pmease.gitop.web.page;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.user.UserInfoSnippet;
import com.pmease.gitop.web.model.UserModel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserInfoSnippet("userActivity", new UserModel(Gitop.getInstance(Dao.class).load(User.class, 1L))) {
			
			@Override
			protected Component newInfoLine(String componentId) {
				return new Label(componentId, "hello world");
			}
		});
	}
	
}
