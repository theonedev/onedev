package com.pmease.gitplex.web.component.commit;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Objects;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.web.component.user.AvatarMode;

@SuppressWarnings("serial")
public class CommitMetaPanel extends Panel {

	private AuthorInfoPanel authorInfo;
	
	public CommitMetaPanel(String id, IModel<Commit> model) {
		super(id, model);
		
		add(authorInfo = new AuthorInfoPanel("author", model));
		add(new CommitterInfoPanel("committer", model) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisibilityAllowed(!Objects.equal(getCommit().getAuthor(), 
													getCommit().getCommitter()));
			}
		});
	}

	public CommitMetaPanel setAuthorMode(AvatarMode mode) {
		authorInfo.setAuthorMode(mode);
		return this;
	}
	
	private Commit getCommit() {
		return (Commit) getDefaultModelObject();
	}
}
