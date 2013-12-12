package com.pmease.gitop.web.page.project.source.blob;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.web.common.wicket.bootstrap.CollapseBehavior;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitUserAvatarLink;
import com.pmease.gitop.web.component.link.GitUserLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.AbstractFilePage;
import com.pmease.gitop.web.page.project.source.component.SourceBreadcrumbPanel;
import com.pmease.gitop.web.util.UrlUtils;

@SuppressWarnings("serial")
public class SourceBlobPage extends AbstractFilePage {

	private final IModel<Commit> lastCommitModel;
	private final IModel<List<GitPerson>> committersModel;
	
	private final static int MAX_DISPLAYED_COMMITTERS = 20;
	
	public SourceBlobPage(PageParameters params) {
		super(params);
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = getProject().code();
				List<String> paths = getPaths();
				List<Commit> commits = git.log(null, getRevision(), Joiner.on("/").join(paths), 1);
				return Iterables.getFirst(commits, null);
			}
		};
		
		committersModel = new LoadableDetachableModel<List<GitPerson>>() {

			@Override
			protected List<GitPerson> load() {
				Git git = getProject().code();
				List<Commit> commits = git.log(null, getRevision(), getFilePath(), 0);
				Set<GitPerson> users = Sets.newHashSet();
				for (Commit each : commits) {
					GitPerson person = new GitPerson(each.getAuthor().getName(), each.getAuthor().getEmail());
					users.add(person);
				}
				
				return Lists.newArrayList(users);
			}
			
		};
		
		add(new SourceBlobPanel("source", projectModel, revisionModel, pathsModel));
	}

	@Override
	public void onPageInitialize() {
		super.onPageInitialize();
		
		add(new SourceBreadcrumbPanel("breadcrumb", projectModel, revisionModel, pathsModel));
		
		add(new Label("shortMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getLastCommit().getSubject();
			}
		}));
		
		Label detailedMsg = new Label("detailedMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getLastCommit().getMessage();
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (Objects.equal(getLastCommit().getSubject(), getLastCommit().getMessage())) {
					this.setVisibilityAllowed(false);
				}
			}
		};
		
		add(detailedMsg);
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("detailed-toggle");
		detailedToggle.add(new CollapseBehavior(detailedMsg));
		add(detailedToggle);
		
		add(new GitUserLink("author", new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getLastCommit().getAuthor());
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
			}
		});
		
		add(new AgeLabel("author-date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getLastCommit().getAuthor().getDate();
			}
			
		}));
		
		
		add(new Label("contributorStat", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return committersModel.getObject().size();
			}
			
		}));
		
		ListView<GitPerson> contributorsView = new ListView<GitPerson>("contributors", 
				new AbstractReadOnlyModel<List<GitPerson>>() {

			@Override
			public List<GitPerson> getObject() {
				List<GitPerson> committers = committersModel.getObject();
				if (committers.size() > MAX_DISPLAYED_COMMITTERS) {
					return Lists.newArrayList(committers.subList(0, MAX_DISPLAYED_COMMITTERS));
				} else {
					return committers;
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<GitPerson> item) {
				GitPerson person = item.getModelObject();
				item.add(new GitUserAvatarLink("link", Model.of(person)));
			}
		};
		
		add(contributorsView);
		
		WebMarkupContainer moreContainer = new WebMarkupContainer("more") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				this.setVisibilityAllowed(committersModel.getObject().size() > MAX_DISPLAYED_COMMITTERS);
			}
		};
		
		add(moreContainer);
		DropdownPanel panel = new DropdownPanel("moreDropdown", true) {

			@Override
			protected Component newContent(String id) {
				Fragment frag = new Fragment(id, "committers-dropdown", SourceBlobPage.this);
				frag.add(new ListView<GitPerson>("committers", committersModel) {

					@Override
					protected void populateItem(ListItem<GitPerson> item) {
						item.add(new GitUserLink("committer", item.getModel()));
					}
				});
				
				return frag;
			}
		};
		
		add(panel);
		moreContainer.add(new DropdownBehavior(panel).clickMode(true));
	}

	protected String getFilePath() {
		List<String> paths = getPaths();
		return UrlUtils.removeRedundantSlashes(Joiner.on("/").join(paths));
	}
	
	@Override
	protected String getPageTitle() {
		return getFilePath() + " at " + getRevision() + " " + getProject().getPathName();
	}
	
	protected Commit getLastCommit() {
		return lastCommitModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (lastCommitModel != null) {
			lastCommitModel.detach();
		}
		
		if (committersModel != null) {
			committersModel.detach();
		}
		
		super.onDetach();
	}
}
