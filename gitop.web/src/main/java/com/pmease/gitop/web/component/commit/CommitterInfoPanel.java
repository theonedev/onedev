package com.pmease.gitop.web.component.commit;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.Commit;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.project.api.GitPerson;

@SuppressWarnings("serial")
public class CommitterInfoPanel extends Panel {

	public CommitterInfoPanel(String id, IModel<Commit> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		

		add(new GitPersonLink("committer", new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getCommit().getCommitter());
			}
			
		}, Mode.NAME_ONLY));
		
		add(new AgeLabel("committerdate", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getCommit().getCommitter().getDate();
			}
		}));
	}
	
	private Commit getCommit() {
		return (Commit) getDefaultModelObject();
	}
}
