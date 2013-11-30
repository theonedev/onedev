package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.model.UserModel;

public class CommitUserLink extends Panel {
	private static final long serialVersionUID = 1L;

	public CommitUserLink(String id, IModel<String> name) {
		super(id, name);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		User user = Gitop.getInstance(UserManager.class).find(getName());
		if (user != null) {
			add(new UserAvatarLink("link", new UserModel(user)));
		} else {
			add(new Label("link", getDefaultModel()).setEscapeModelStrings(true));
		}
	}
	
	private String getName() {
		return (String) getDefaultModelObject();
	}
}
