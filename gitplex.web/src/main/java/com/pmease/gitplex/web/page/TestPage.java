package com.pmease.gitplex.web.page;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserInfoSnippet;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserInfoSnippet("userActivity", new UserModel(GitPlex.getInstance(Dao.class).load(User.class, 1L))) {
			
			@Override
			protected Component newInfoLine(String componentId) {
				return new Label(componentId, "hello world");
			}
		});
	}
	
}
