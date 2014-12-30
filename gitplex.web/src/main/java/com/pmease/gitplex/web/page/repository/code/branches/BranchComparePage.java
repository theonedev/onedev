package com.pmease.gitplex.web.page.repository.code.branches;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.branch.AffinalBranchSingleChoice;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.commit.CommitsTablePanel;
import com.pmease.gitplex.web.component.diff.CompareResultPanel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestOverviewPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestDetailPage;

@SuppressWarnings("serial")
public class BranchComparePage extends RepositoryPage {

	private final IModel<Branch> targetModel;
	
	private final IModel<Branch> sourceModel;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;
	
	private Long targetId;
	
	private Long sourceId;
	
	public static PageParameters paramsOf(Repository repository, Branch source, Branch target) {
		PageParameters params = paramsOf(repository);
		params.set("source", source.getId());
		params.set("target", target.getId());
		return params;
	}

	public BranchComparePage(final PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));

		targetModel = new IModel<Branch>() {

			@Override
			public Branch getObject() {
				Dao dao = GitPlex.getInstance(Dao.class);
				if (targetId != null) 
					return dao.load(Branch.class, targetId);
				else if (params.get("target").toString() != null) 
					return dao.load(Branch.class, params.get("target").toLongObject());
				else 
					return getRepository().getDefaultBranch();
			}

			@Override
			public void setObject(Branch object) {
				targetId = object.getId();
			}

			@Override
			public void detach() {
			}
			
		};
		
		sourceModel = new IModel<Branch>() {

			@Override
			public Branch getObject() {
				Dao dao = GitPlex.getInstance(Dao.class);
				if (sourceId != null) 
					return dao.load(Branch.class, sourceId);
				else if (params.get("source").toString() != null) 
					return dao.load(Branch.class, params.get("source").toLongObject());
				else 
					return getRepository().getDefaultBranch();
			}

			@Override
			public void setObject(Branch object) {
				sourceId = object.getId();
			}

			@Override
			public void detach() {
			}
			
		};

		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return GitPlex.getInstance(PullRequestManager.class).findLatest(
						targetModel.getObject(), sourceModel.getObject());
			}
			
		};

		mergeBaseModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Branch target = targetModel.getObject();
				Branch source = sourceModel.getObject();
				if (!target.getRepository().equals(source.getRepository())) {
					Git sandbox = new Git(FileUtils.createTempDir());
					try {
						sandbox.clone(target.getRepository().git(), false, true, true, target.getName());
						sandbox.reset(null, null);
						sandbox.fetch(source.getRepository().git(), source.getName());
						return sandbox.calcMergeBase(target.getHeadCommitHash(), source.getHeadCommitHash());
					} finally {
						FileUtils.deleteDir(sandbox.repoDir());
					}
				} else {
					return target.getRepository().git().calcMergeBase(
							target.getHeadCommitHash(), source.getHeadCommitHash());					
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Branch source = sourceModel.getObject();
				return source.getRepository().git().log(mergeBaseModel.getObject(), source.getHeadCommitHash(), null, 0, 0);
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalBranchSingleChoice("target", repoModel, targetModel) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				setResponsePage(
						BranchComparePage.class, 
						paramsOf(getRepository(), sourceModel.getObject(), targetModel.getObject()));
			}
			
		}.setRequired(true));
		
		add(new AffinalBranchSingleChoice("source", repoModel, sourceModel) { 

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				setResponsePage(
						BranchComparePage.class, 
						paramsOf(getRepository(), sourceModel.getObject(), targetModel.getObject()));
			}
			
		}.setRequired(true));
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(
						BranchComparePage.class, 
						paramsOf(getRepository(), targetModel.getObject(), sourceModel.getObject()));
			}
			
		});
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				Branch target = targetModel.getObject();
				Branch source = sourceModel.getObject();
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(target.getRepository(), source, target));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = requestModel.getObject();
				setVisible((request == null || !request.isOpen()) && !commitsModel.getObject().isEmpty());
			}
			
		});
		
		add(new WebMarkupContainer("sameBranch") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(targetModel.getObject().equals(sourceModel.getObject()));
			}
			
		});
		add(new WebMarkupContainer("openedRequest") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = requestModel.getObject();
				setVisible(request != null && request.isOpen());
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new Link<Void>("view") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("no", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return requestModel.getObject().getId().toString();
							}
						}));
					}

					@Override
					public void onClick() {
						PageParameters params = RequestDetailPage.paramsOf(requestModel.getObject());
						setResponsePage(RequestOverviewPage.class, params);
					}
					
				});
				
				add(new Label("title", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return requestModel.getObject().getTitle();
					}
				}));
			}
			
		});
		add(new WebMarkupContainer("upToDate") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!targetModel.getObject().equals(sourceModel.getObject()) 
						&& mergeBaseModel.getObject().equals(sourceModel.getObject().getHeadCommitHash()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new BranchLink("sourceBranch", sourceModel));
				add(new BranchLink("targetBranch", targetModel));
			}

		});
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new AjaxActionTab(Model.of("Commits")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newCommitsPanel();
				getPage().replace(panel);
				target.add(panel);
			}
			
		});

		tabs.add(new AjaxActionTab(Model.of("Changed Files")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component panel = newChangedFilesPanel();
				getPage().replace(panel);
				target.add(panel);
			}
			
		});

		add(new Tabbable("tabs", tabs) {

			@Override
			protected String getCssClasses() {
				return "nav nav-tabs";
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!commitsModel.getObject().isEmpty());
			}

		});
		
		add(new CommitsTablePanel("tabPanel", commitsModel, repoModel).setOutputMarkupId(true));

		add(new BackToTop("backToTop"));
	}
	
	private Component newCommitsPanel() {
		return new CommitsTablePanel("tabPanel", commitsModel, repoModel).setOutputMarkupId(true);
	}
	
	private Component newChangedFilesPanel() {
		return new CompareResultPanel("tabPanel", repoModel, mergeBaseModel.getObject(), 
				sourceModel.getObject().getHeadCommitHash(), null) {
			
			@Override
			protected void onSelection(AjaxRequestTarget target, Change change) {
			}
			
			@Override
			protected InlineCommentSupport getInlineCommentSupport(Change change) {
				return null;
			}
		}.setOutputMarkupId(true);
	}

	@Override
	protected void onDetach() {
		targetModel.detach();
		sourceModel.detach();
		requestModel.detach();
		mergeBaseModel.detach();
		commitsModel.detach();

		super.onDetach();
	}

}
