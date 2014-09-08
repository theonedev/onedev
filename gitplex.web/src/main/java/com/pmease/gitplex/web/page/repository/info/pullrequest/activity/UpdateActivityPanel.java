package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commit.CommitHashLink;
import com.pmease.gitplex.web.component.commit.CommitMessagePanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.repository.info.pullrequest.CommitStatusPanel;

@SuppressWarnings("serial")
public class UpdateActivityPanel extends Panel {

	private IModel<PullRequestUpdate> updateModel;
	
	public UpdateActivityPanel(String id, IModel<PullRequestUpdate> model) {
		super(id);
		
		this.updateModel = model;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("user", new UserModel(updateModel.getObject().getUser()), AvatarMode.NAME));
		add(new Label("age", Model.of(updateModel.getObject().getDate())));
		
		add(new ListView<Commit>("commits", new AbstractReadOnlyModel<List<Commit>>() {

			@Override
			public List<Commit> getObject() {
				return updateModel.getObject().getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				
				item.add(new PersonLink("author", Model.of(commit.getAuthor()), AvatarMode.AVATAR));

				IModel<Repository> repoModel = new AbstractReadOnlyModel<Repository>() {

					@Override
					public Repository getObject() {
						return updateModel.getObject().getRequest().getTarget().getRepository();
					}
					
				};
				item.add(new CommitMessagePanel("message", repoModel, new AbstractReadOnlyModel<Commit>() {

					@Override
					public Commit getObject() {
						return item.getModelObject();
					}
					
				}));
				
				item.add(new CommitStatusPanel("status", updateModel, commit.getHash()));
				item.add(new CommitHashLink("hashLink", repoModel, commit.getHash()));
			}
			
		});
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		
		updateModel.detach();
	}

}
