package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.pullrequest.CommitListPanel;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class UpdateActivityPanel extends Panel {

	public UpdateActivityPanel(String id, IModel<PullRequestUpdate> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequestUpdate update = getPullRequestUpdate();
		User updatedBy = update.getUser();
		if (updatedBy != null) {
			GitPerson person = new GitPerson(updatedBy.getName(), updatedBy.getEmail());
			add(new GitPersonLink("user", Model.of(person), GitPersonLink.Mode.NAME_AND_AVATAR));
		} else {
			add(new Label("<i>Unknown</i>").setEscapeModelStrings(false));
		}
		
		add(new Label("date", DateUtils.formatAge(update.getDate())));
		
		add(new CommitListPanel("commits", new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequestUpdate update = getPullRequestUpdate();
				Git git = update.getRequest().getTarget().getProject().git();
				
				int index = update.getRequest().getSortedUpdates().indexOf(update);
				return git.log(update.getRequest().getSortedUpdates().get(index+1).getHeadCommit(), 
						update.getHeadCommit(), null, 0, 0); 
			}
			
		}));
	}

	private PullRequestUpdate getPullRequestUpdate() {
		return (PullRequestUpdate) getDefaultModelObject();
	}
	
}
