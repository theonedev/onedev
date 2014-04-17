package com.pmease.gitop.web.component.commit;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.wicket.Component;
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
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.common.wicket.bootstrap.Alert;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.repository.api.GitPerson;
import com.pmease.gitop.web.page.repository.source.commit.SourceCommitPage;

@SuppressWarnings("serial")
public class CommitsTablePanel extends Panel {

	private final IModel<Multimap<Date, Commit>> groupsModel;
	private final IModel<Repository> repositoryModel;
	
	public CommitsTablePanel(String id, IModel<List<Commit>> model, IModel<Repository> repositoryModel) {
		super(id, model);
		
		this.repositoryModel = repositoryModel;
		groupsModel = new LoadableDetachableModel<Multimap<Date, Commit>>() {

			@Override
			protected Multimap<Date, Commit> load() {
				List<Commit> commits = getCommits();
				
				LinkedListMultimap<Date, Commit> groups = LinkedListMultimap.<Date, Commit>create();
				for (Commit commit : commits) {
					Date date = commit.getAuthorDate();
					date = DateUtils.round(date, Calendar.DAY_OF_MONTH);
					groups.put(date, commit);
				}
				
				return groups;
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Alert alert = new Alert("alert", 
				Model.of("The commits are too large to render, only showing most recent 250 commits")) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(getCommits().size() > Constants.MAX_RENDERABLE_COMMITS);
			}
		};
		
		add(alert.type(Alert.Type.Warning));
		
		add(new ListView<Date>("commits", new AbstractReadOnlyModel<List<Date>>() {

			@Override
			public List<Date> getObject() {
				List<Date> dates = Lists.newArrayList(groupsModel.getObject().keySet());
				Collections.sort(dates);
				Collections.reverse(dates);
				return dates;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Date> item) {
				Date date = item.getModelObject();
				item.add(new Label("day", DataTypes.DATE.asString(date, "MMM dd, yyyy")));
				item.add(createCommitsView("rows", date));
			}
		});
	}
	
	private Component createCommitsView(String id, final Date date) {
		return new ListView<Commit>(id, new AbstractReadOnlyModel<List<Commit>>() {

			@Override
			public List<Commit> getObject() {
				return Lists.newArrayList(groupsModel.getObject().get(date));
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Commit> item) {
				Commit commit = item.getModelObject();
				GitPerson person = GitPerson.of(commit.getAuthor());
				item.add(new GitPersonLink("name", Model.of(person), GitPersonLink.Mode.NAME_AND_AVATAR).enableTooltip());
				item.add(new CommitMessagePanel("message", item.getModel(), repositoryModel));
				
				AbstractLink link = new BookmarkablePageLink<Void>("commitlink",
						SourceCommitPage.class,
						SourceCommitPage.newParams(getRepository(), commit.getHash()));
				
				item.add(link);
				link.add(new Label("sha", GitUtils.abbreviateSHA(commit.getHash())));
			}
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript("gitop.commitMessage.toggle('.title-message .detailed-toggle');"));
				
	}
	
	@SuppressWarnings("unchecked")
	private List<Commit> getCommits() {
		return (List<Commit>) getDefaultModelObject();
	}
	
	private Repository getRepository() {
		return repositoryModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (groupsModel != null) {
			groupsModel.detach();
		}

		if (repositoryModel != null) {
			repositoryModel.detach();
		}
		
		super.onDetach();
	}
}
