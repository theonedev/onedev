package com.pmease.gitop.web.page.project.pullrequest;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class CommitListPanel extends Panel {

	public CommitListPanel(String id, IModel<List<Commit>> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Commit>("commits", new AbstractReadOnlyModel<List<Commit>>() {

			@Override
			public List<Commit> getObject() {
				return getCommits();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				item.add(new GitPersonLink("author", 
						Model.of(GitPerson.of(commit.getAuthor())), GitPersonLink.Mode.NAME_AND_AVATAR));

				item.add(new Label("message", commit.getSubject()));
				
				item.add(new Label("date", DateUtils.formatAge(commit.getAuthor().getDate())));
				
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				AbstractLink link = new BookmarkablePageLink<Void>("shaLink",
						SourceCommitPage.class,
						SourceCommitPage.newParams(page.getProject(), commit.getHash()));
				link.add(new Label("sha", GitUtils.abbreviateSHA(commit.getHash())));
				
				item.add(link);
			}
			
		});
	}

	@SuppressWarnings("unchecked")
	private List<Commit> getCommits() {
		return (List<Commit>) getDefaultModelObject();
	}
	
}
