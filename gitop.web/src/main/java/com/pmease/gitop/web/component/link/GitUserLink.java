package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Optional;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.avatar.GravatarImage;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.project.api.GitPerson;

public class GitUserLink extends Panel {
	private static final long serialVersionUID = 1L;

	public GitUserLink(String id, IModel<GitPerson> person) {
		super(id, person);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Optional<User> user = getPerson().asUser();
		if (user.isPresent()) {
			add(new UserAvatarLink("link", new UserModel(user.get())));
		} else {
			Fragment frag = new Fragment("link", "frag", this);
			frag.add(new GravatarImage("avatar", new AbstractReadOnlyModel<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getObject() {
					return getPerson().getEmailAddress();
				}
				
			}));
			
			frag.add(new Label("name", getPerson().getName()).setEscapeModelStrings(true));
			add(frag);
		}
	}
	
	private GitPerson getPerson() {
		return (GitPerson) getDefaultModelObject();
	}
}
