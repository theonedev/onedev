package com.pmease.gitop.web.page.project.source.commits;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.project.source.tree.SourceTreePage;
import com.pmease.gitop.web.util.GitUtils;

@SuppressWarnings("serial")
public class CommitsPanel extends Panel {
	
	private final IModel<Project> projectModel;
	public CommitsPanel(String id, IModel<List<Commit>> commitsModel, IModel<Project> projectModel) {
		super(id, commitsModel);
		
		this.projectModel = projectModel;
	}

	class CommitGroup {
		final Date date;
		List<Commit> commits = Lists.newArrayList();
		
		CommitGroup(Date date) {
			this.date = date;
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<CommitGroup>> model = new LoadableDetachableModel<List<CommitGroup>>() {

			@Override
			protected List<CommitGroup> load() {
				List<CommitGroup> groups = Lists.newArrayList();
				CommitGroup current = null;
				List<Commit> commits = getCommits();
				for (int i = 0; i < commits.size() && i < CommitsPage.COMMITS_PER_PAGE; i++) {
					Commit each = commits.get(i);
					if (current == null ||
							!DateUtils.isSameDay(current.date, each.getCommitter().getDate())) {
						current = new CommitGroup(each.getCommitter().getDate());
						groups.add(current);
					}
					
					current.commits.add(each);
				}
				
				return groups;
			}
		};
		
		add(new ListView<CommitGroup>("groups", model) {

			@Override
			protected void populateItem(ListItem<CommitGroup> item) {
				CommitGroup group = item.getModelObject();
				item.add(new Label("date", DataTypes.DATE.asString(group.date, "MMMMM dd, yyyy")));
				item.add(new ListView<Commit>("commits", group.commits) {

					@Override
					protected void populateItem(ListItem<Commit> item) {
						Commit commit = item.getModelObject();
						
						GitPerson author = new GitPerson(
								commit.getAuthor().getName(), 
								commit.getAuthor().getEmail());
						
						item.add(new GitPersonLink("authoravatar", Model.of(author), Mode.AVATAR_ONLY));
						item.add(new Label("shortmessage", commit.getSubject()));
						item.add(new GitPersonLink("author", Model.of(author), Mode.NAME_ONLY));
						item.add(new AgeLabel("authordate", Model.of(commit.getAuthor().getDate())));
						item.add(new Label("detailedmessage", commit.getMessage()).setVisibilityAllowed(!Objects.equal(commit.getSubject(), commit.getMessage())));
						
						WebMarkupContainer detailedToggle = new WebMarkupContainer("detailedToggle");
						item.add(detailedToggle);
						detailedToggle.setVisibilityAllowed(!Objects.equal(commit.getSubject(), commit.getMessage()));
						
						GitPerson committer = new GitPerson(
								commit.getCommitter().getName(), 
								commit.getCommitter().getEmail());
						item.add(new GitPersonLink("committer", Model.of(committer), Mode.NAME_ONLY)
							.setVisibilityAllowed(!Objects.equal(commit.getAuthor(), commit.getCommitter())));
						item.add(new AgeLabel("committerdate", Model.of(commit.getCommitter().getDate())));
						
						AbstractLink link =new BookmarkablePageLink<Void>("commitlink", SourceCommitPage.class, 
								SourceCommitPage.newParams(getProject(), commit.getHash()));
						link.add(new Label("sha", GitUtils.abbreviateSHA(commit.getHash())));
						item.add(link);
						item.add(new BookmarkablePageLink<Void>("treelink", SourceTreePage.class, 
								SourceTreePage.newParams(getProject(), 
														 commit.getHash(), 
														 Collections.<String>emptyList())));
					}
					
				});
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript("$('.short-message .detailed-toggle').click(function(e) { \nvar $self = $(this); $self.toggleClass('collapsed'); \n$self.parent().siblings('.detailed-message').toggle(); });"));
				
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	@SuppressWarnings("unchecked")
	private List<Commit> getCommits() {
		return (List<Commit>) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
		}
		
		super.onDetach();
	}
}
