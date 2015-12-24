package com.pmease.gitplex.web.page.repository.branches.compare;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.BranchLink;
import com.pmease.gitplex.web.component.branchchoice.affinalchoice.AffinalBranchSingleChoice;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;

@SuppressWarnings("serial")
public class BranchComparePage extends RepositoryPage {

	private final IModel<RepoAndBranch> targetModel;
	
	private final IModel<RepoAndBranch> sourceModel;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;
	
	private String targetId;
	
	private String sourceId;
	
	public static PageParameters paramsOf(Repository repository, RepoAndBranch source, RepoAndBranch target) {
		PageParameters params = paramsOf(repository);
		params.set("source", source.getId());
		params.set("target", target.getId());
		return params;
	}

	public BranchComparePage(final PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));

		targetModel = new IModel<RepoAndBranch>() {

			@Override
			public RepoAndBranch getObject() {
				if (targetId != null) 
					return new RepoAndBranch(targetId);
				else if (params.get("target").toString() != null) 
					return new RepoAndBranch(params.get("target").toString());
				else 
					return new RepoAndBranch(getRepository(), getRepository().getDefaultBranch());
			}

			@Override
			public void setObject(RepoAndBranch object) {
				targetId = object.getId();
			}

			@Override
			public void detach() {
			}
			
		};
		
		sourceModel = new IModel<RepoAndBranch>() {

			@Override
			public RepoAndBranch getObject() {
				if (sourceId != null) 
					return new RepoAndBranch(sourceId);
				else if (params.get("source").toString() != null) 
					return new RepoAndBranch(params.get("source").toString());
				else 
					return new RepoAndBranch(getRepository(), getRepository().getDefaultBranch());
			}

			@Override
			public void setObject(RepoAndBranch object) {
				sourceId = object.getId();
			}

			@Override
			public void detach() {
			}
			
		};

		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				return GitPlex.getInstance(PullRequestManager.class).findOpen(
						targetModel.getObject(), sourceModel.getObject());
			}
			
		};

		mergeBaseModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				RepoAndBranch target = targetModel.getObject();
				RepoAndBranch source = sourceModel.getObject();
				if (!target.getRepository().equals(source.getRepository())) {
					Git sandbox = new Git(FileUtils.createTempDir());
					try {
						sandbox.clone(target.getRepository().git(), false, true, true, target.getBranch());
						sandbox.reset(null, null);
						sandbox.fetch(source.getRepository().git(), source.getBranch());
						return sandbox.calcMergeBase(target.getHead(), source.getHead());
					} finally {
						FileUtils.deleteDir(sandbox.repoDir());
					}
				} else {
					return target.getRepository().git().calcMergeBase(
							target.getHead(), source.getHead());					
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				RepoAndBranch source = sourceModel.getObject();
				return source.getRepository().git().log(mergeBaseModel.getObject(), source.getHead(), null, 0, 0, false);
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalBranchSingleChoice("target", repoModel, new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return targetModel.getObject().getId();
			}

			@Override
			public void setObject(String object) {
				targetModel.setObject(new RepoAndBranch(object));
			}
			
		}, false) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				
				PageParameters params = paramsOf(getRepository(), 
						sourceModel.getObject(), targetModel.getObject());
				setResponsePage(BranchComparePage.class, params);
			}
			
		}.setRequired(true));
		
		add(new AffinalBranchSingleChoice("source", repoModel, new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return sourceModel.getObject().getId();
			}

			@Override
			public void setObject(String object) {
				sourceModel.setObject(new RepoAndBranch(object));
			}
			
		}, false) { 

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);

				PageParameters params = paramsOf(getRepository(), 
						sourceModel.getObject(), targetModel.getObject());
				setResponsePage(BranchComparePage.class, params);
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
				RepoAndBranch target = targetModel.getObject();
				RepoAndBranch source = sourceModel.getObject();
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(target.getRepository(), source, target));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = requestModel.getObject();
				setVisible(request == null && !commitsModel.getObject().isEmpty());
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
				setVisible(request != null);
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
						&& mergeBaseModel.getObject().equals(sourceModel.getObject().getHead()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new BranchLink("sourceBranch", sourceModel.getObject()));
				add(new BranchLink("targetBranch", targetModel.getObject()));
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
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!commitsModel.getObject().isEmpty());
			}

		});

		add(new WebMarkupContainer("tabPanel").setOutputMarkupId(true));
		
		add(new BackToTop("backToTop"));
	}
	
	private Component newCommitsPanel() {
		return new WebMarkupContainer("tabPanel").setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(BranchComparePage.class, "branch-compare.css")));
	}

	private Component newChangedFilesPanel() {
		return new WebMarkupContainer("tabPanel").setOutputMarkupId(true);
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

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoBranchesPage.class, paramsOf(repository));
	}

}
