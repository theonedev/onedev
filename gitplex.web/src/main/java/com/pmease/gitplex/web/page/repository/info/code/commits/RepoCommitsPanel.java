package com.pmease.gitplex.web.page.repository.info.code.commits;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.pmease.gitplex.core.GitPlex;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.pmease.commons.git.Commit;
import com.pmease.gitplex.core.manager.CommitCommentManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.common.datatype.DataTypes;
import com.pmease.gitplex.web.component.commit.CommitMessagePanel;
import com.pmease.gitplex.web.component.commit.CommitMetaPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.info.code.tree.RepoTreePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class RepoCommitsPanel extends Panel {
	
	private final IModel<Repository> repositoryModel;
	private final IModel<Multimap<Date, Commit>> groupsModel;
	private final IModel<Map<String, Integer>> commentStatsModel;
	
	public RepoCommitsPanel(String id, 
			IModel<List<Commit>> commitsModel, 
			IModel<Repository> repositoryModel) {
		super(id, commitsModel);
		
		this.repositoryModel = repositoryModel;
		
		groupsModel = new LoadableDetachableModel<Multimap<Date, Commit>>() {

			@Override
			protected Multimap<Date, Commit> load() {
				List<Commit> commits = getCommits();
				
				LinkedListMultimap<Date, Commit> groups = LinkedListMultimap.<Date, Commit>create();
				for (Commit commit : commits) {
					Date date = commit.getAuthor().getWhen();
					date = DateUtils.round(date, Calendar.DAY_OF_MONTH);
					groups.put(date, commit);
				}
				
				return groups;
			}
		};
		
		commentStatsModel = new LoadableDetachableModel<Map<String, Integer>>() {

			@Override
			protected Map<String, Integer> load() {
				// XXX: this may slow, performance tuning later
				return GitPlex.getInstance(CommitCommentManager.class).getCommitCommentStats(getRepository());
			}
			
		};
	}

	private Multimap<Date, Commit> getGroups() {
		return groupsModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Date>("groups", new AbstractReadOnlyModel<List<Date>>() {

			@Override
			public List<Date> getObject() {
				List<Date> dates = Lists.newArrayList(getGroups().keySet());
				Collections.sort(dates);
				Collections.reverse(dates);
				return dates;
			}
		}) {

			@Override
			protected void populateItem(ListItem<Date> item) {
				final Date date = item.getModelObject();
				item.add(new Label("date", DataTypes.DATE.asString(date, "MMM dd, yyyy")));
				item.add(new ListView<Commit>("rows", new AbstractReadOnlyModel<List<Commit>>() {

					@Override
					public List<Commit> getObject() {
						Multimap<Date, Commit> groups = getGroups();
						return Lists.newArrayList(groups.get(date));
					}
				}) {

					@Override
					protected void populateItem(ListItem<Commit> item) {
						Commit commit = item.getModelObject();
						
						item.add(new PersonLink("avatar", Model.of(commit.getAuthor()), AvatarMode.AVATAR)
									.withTooltipConfig(new TooltipConfig().withPlacement(Placement.right)));
						
						item.add(new CommitMessagePanel("message", item.getModel()));
						item.add(new CommitMetaPanel("meta", item.getModel()));
						
						AbstractLink shalink =new BookmarkablePageLink<Void>(
								"shalink", RepoCommitPage.class, 
								RepoCommitPage.paramsOf(getRepository(), commit.getHash(), null));
						shalink.add(new Label("sha", GitUtils.abbreviateSHA(commit.getHash())));
						int commentsCount = getCommentCount(commit.getHash());
						shalink.add(new Label("commentsCount", commentsCount).setVisibilityAllowed(commentsCount > 0));
						
						item.add(shalink);
						item.add(new BookmarkablePageLink<Void>("treelink", 
								RepoTreePage.class, 
								RepoTreePage.paramsOf(getRepository(), commit.getHash(), null)));
					}
					
				});
			}
			
		});
	}
	
	private int getCommentCount(String sha) {
		Map<String, Integer> map = commentStatsModel.getObject();
		if (map.containsKey(sha)) {
			return map.get(sha);
		} else {
			return 0;
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript("gitplex.commitMessage.toggle('.title-message .detailed-toggle');"));
				
	}
	
	private Repository getRepository() {
		return repositoryModel.getObject();
	}
	
	@SuppressWarnings("unchecked")
	private List<Commit> getCommits() {
		return (List<Commit>) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		if (repositoryModel != null) {
			repositoryModel.detach();
		}
		
		if (groupsModel != null) {
			groupsModel.detach();
		}
		
		if (commentStatsModel != null) {
			commentStatsModel.detach();
		}
		
		super.onDetach();
	}
}
