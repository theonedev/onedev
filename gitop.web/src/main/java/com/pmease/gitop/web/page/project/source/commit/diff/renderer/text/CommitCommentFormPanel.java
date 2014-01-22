package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.web.component.link.UserAvatarLink;
import com.pmease.gitop.web.model.UserModel;

public class CommitCommentFormPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public CommitCommentFormPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form");
		add(form);
		
		form.add(new UserAvatarLink("user", 
				new UserModel(Gitop.getInstance(UserManager.class).getCurrent())));
	}
}
