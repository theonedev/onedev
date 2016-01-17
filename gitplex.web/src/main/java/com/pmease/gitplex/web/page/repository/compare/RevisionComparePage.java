package com.pmease.gitplex.web.page.repository.compare;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.RepoAndRevision;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.diff.revision.option.DiffOptionPanel;
import com.pmease.gitplex.web.component.revisionpicker.AffinalRevisionPicker;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;

@SuppressWarnings("serial")
public class RevisionComparePage extends RepositoryPage {

	private static final String PARAM_TARGET = "target";
	
	private static final String PARAM_SOURCE = "source";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;

	private RepoAndRevision target;
	
	private RepoAndRevision source;
	
	private DiffOptionPanel diffOption;
	
	private String path;
	
	public static PageParameters paramsOf(Repository repository, RepoAndRevision target, RepoAndRevision source) {
		PageParameters params = paramsOf(repository);
		params.set(PARAM_TARGET, target.toString());
		params.set(PARAM_SOURCE, source.toString());
		return params;
	}

	public RevisionComparePage(final PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));

		String str = params.get(PARAM_SOURCE).toString();
		if (str != null) {
			source = new RepoAndRevision(str);
		} else {
			source = new RepoAndRevision(getRepository(), getRepository().getDefaultBranch());
		}
		
		str = params.get(PARAM_TARGET).toString();
		if (str != null) {
			target = new RepoAndRevision(str);
		} else {
			target = new RepoAndRevision(getRepository(), getRepository().getDefaultBranch());
		}
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				RepoAndBranch target = new RepoAndBranch(RevisionComparePage.this.target.toString());
				RepoAndBranch source = new RepoAndBranch(RevisionComparePage.this.source.toString());
				return GitPlex.getInstance(PullRequestManager.class).findOpen(target, source);
			}
			
		};

		mergeBaseModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Repository targetRepo = target.getRepository();
				Repository sourceRepo = source.getRepository();
				if (!targetRepo.equals(sourceRepo)) {
					Git sandbox = new Git(FileUtils.createTempDir());
					try {
						sandbox.clone(targetRepo.git(), false, true, true, target.getRevision());
						sandbox.reset(null, null);
						sandbox.fetch(sourceRepo.git(), source.getRevision());
						return sandbox.calcMergeBase(target.getCommit().name(), source.getCommit().name());
					} finally {
						FileUtils.deleteDir(sandbox.repoDir());
					}
				} else {
					return targetRepo.getMergeBase(target.getRevision(), source.getRevision()).name();
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Repository sourceRepo = source.getRepository();
				return sourceRepo.git().log(mergeBaseModel.getObject(), 
						source.getCommit().name(), null, 0, 0, false);
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalRevisionPicker("target", target.getRepoId(), target.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Repository repository, String revision) {
				PageParameters params = paramsOf(getRepository(), new RepoAndRevision(repository, revision), source);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});

		add(new AffinalRevisionPicker("source", source.getRepoId(), source.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Repository repository, String revision) {
				PageParameters params = paramsOf(getRepository(), RevisionComparePage.this.target, 
						new RepoAndRevision(repository, revision));
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(RevisionComparePage.class,paramsOf(getRepository(), source, target));
			}

		});
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				RepoAndBranch target = new RepoAndBranch(RevisionComparePage.this.target.toString());
				RepoAndBranch source = new RepoAndBranch(RevisionComparePage.this.source.toString());
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(target.getRepository(), target, source));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (target.isBranch() && source.isBranch()) {
					PullRequest request = requestModel.getObject();
					setVisible(request == null && !commitsModel.getObject().isEmpty());
				} else {
					setVisible(false);
				}
			}
			
		});
		
		add(new WebMarkupContainer("sameRevision") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(source.equals(target));
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
				
				setVisible(!source.equals(target) && mergeBaseModel.getObject().equals(source.getCommit().name()));
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
				Component panel = newComparePanel();
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

		add(newCommitsPanel());
		
		add(new BackToTop("backToTop"));
	}
	
	private Component newCommitsPanel() {
		return new CommitListPanel(TAB_PANEL_ID, repoModel, commitsModel){

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!commitsModel.getObject().isEmpty());
			}
			
		}.setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionComparePage.class, "revision-compare.css")));
	}

	private Component newComparePanel() {
		final Fragment fragment = new Fragment(TAB_PANEL_ID, "compareFrag", this);
		
		diffOption = new DiffOptionPanel("diffOption", new AbstractReadOnlyModel<Repository>() {

			@Override
			public Repository getObject() {
				return target.getRepository();
			}
			
		}, target.getRevision()) {

			@Override
			protected void onSelectPath(AjaxRequestTarget target, String path) {
				RevisionComparePage.this.path = path;
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				fragment.replace(diffPanel);
				target.add(diffPanel);
			}

			@Override
			protected void onLineProcessorChange(AjaxRequestTarget target) {
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				fragment.replace(diffPanel);
				target.add(diffPanel);
			}

			@Override
			protected void onDiffModeChange(AjaxRequestTarget target) {
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				fragment.replace(diffPanel);
				target.add(diffPanel);
			}
			
		};
		diffOption.add(new StickyBehavior());
		fragment.add(diffOption);
		fragment.add(newRevDiffPanel());
		
		return fragment;
	}
	
	protected RevisionDiffPanel newRevDiffPanel() {
		RevisionDiffPanel diffPanel = new RevisionDiffPanel("revisionDiff", repoModel, 
				new Model<PullRequest>(null), new Model<Comment>(null), 
				target.getRevision(), source.getRevision(), path, null, diffOption.getLineProcessor(), 
				diffOption.getDiffMode()) {

			@Override
			protected void onClearPath(AjaxRequestTarget target) {
				path = null;
				RevisionDiffPanel diffPanel = newRevDiffPanel();
				replaceWith(diffPanel);
				target.add(diffPanel);
			}
			
		};
		diffPanel.setOutputMarkupId(true);
		return diffPanel;
	}

	@Override
	protected void onDetach() {
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
