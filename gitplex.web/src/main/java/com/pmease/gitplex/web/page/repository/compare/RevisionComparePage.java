package com.pmease.gitplex.web.page.repository.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.model.DepotAndRevision;
import com.pmease.gitplex.core.model.Depot;
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
	
	private static final String PARAM_PATH = "path";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;

	private DepotAndRevision target;
	
	private DepotAndRevision source;
	
	private DiffOptionPanel diffOption;
	
	private AjaxActionTab commitsTab;
	
	private AjaxActionTab filesTab;
	
	private String path;
	
	public static PageParameters paramsOf(Depot depot, DepotAndRevision target, 
			DepotAndRevision source, @Nullable String path) {
		PageParameters params = paramsOf(depot);
		params.set(PARAM_TARGET, target.toString());
		params.set(PARAM_SOURCE, source.toString());
		if (path != null)
			params.set(PARAM_PATH, path);
		return params;
	}

	public RevisionComparePage(final PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));

		String str = params.get(PARAM_SOURCE).toString();
		if (str != null) {
			source = new DepotAndRevision(str);
		} else {
			source = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		
		str = params.get(PARAM_TARGET).toString();
		if (str != null) {
			target = new DepotAndRevision(str);
		} else {
			target = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		
		path = params.get(PARAM_PATH).toString();
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				DepotAndBranch target = new DepotAndBranch(RevisionComparePage.this.target.toString());
				DepotAndBranch source = new DepotAndBranch(RevisionComparePage.this.source.toString());
				return GitPlex.getInstance(PullRequestManager.class).findOpen(target, source);
			}
			
		};

		mergeBaseModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Depot targetRepo = target.getDepot();
				Depot sourceRepo = source.getDepot();
				if (!targetRepo.equals(sourceRepo)) {
					Git sandbox = new Git(FileUtils.createTempDir());
					try {
						sandbox.clone(targetRepo.git(), false, true, true, target.getRevision());
						sandbox.reset(null, null);
						sandbox.fetch(sourceRepo.git(), source.getRevision());
						return sandbox.calcMergeBase(target.getCommit().name(), source.getCommit().name());
					} finally {
						FileUtils.deleteDir(sandbox.depotDir());
					}
				} else {
					return targetRepo.getMergeBase(target.getRevision(), source.getRevision()).name();
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Depot sourceRepo = source.getDepot();
				List<Commit> commits = sourceRepo.git().log(mergeBaseModel.getObject(), 
						source.getCommit().name(), null, 0, 0, false);
				return commits;
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalRevisionPicker("target", target.getDepotId(), target.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				PageParameters params = paramsOf(getDepot(), new DepotAndRevision(depot, revision), source, path);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});

		add(new AffinalRevisionPicker("source", source.getDepotId(), source.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				PageParameters params = paramsOf(getDepot(), RevisionComparePage.this.target, 
						new DepotAndRevision(depot, revision), path);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(RevisionComparePage.class,paramsOf(getDepot(), source, target, path));
			}

		});
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				DepotAndBranch target = new DepotAndBranch(RevisionComparePage.this.target.toString());
				DepotAndBranch source = new DepotAndBranch(RevisionComparePage.this.source.toString());
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(target.getDepot(), target, source));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (target.getBranch()!=null && source.getBranch()!=null) {
					PullRequest request = requestModel.getObject();
					setVisible(request == null && !isIntegrated());
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
		
		tabs.add(commitsTab = new AjaxActionTab(Model.of("Commits")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				path = null;
				newTabPanel(target);
				pushState(target);
			}
			
		});

		tabs.add(filesTab = new AjaxActionTab(Model.of("Changed Files")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				path = "";
				newTabPanel(target);
				pushState(target);
			}
			
		});

		add(new Tabbable("tabs", tabs) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isIntegrated());
			}

		});

		newTabPanel(null);
		
		add(new BackToTop("backToTop"));
	}
	
	private boolean isIntegrated() {
		return mergeBaseModel.getObject().equals(source.getCommit().name());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionComparePage.class, "revision-compare.css")));
	}

	private void newTabPanel(@Nullable AjaxRequestTarget target) {
		final WebMarkupContainer tabPanel;
		if (path != null) {
			tabPanel = new Fragment(TAB_PANEL_ID, "compareFrag", this) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!isIntegrated());
				}
				
			};
			
			diffOption = new DiffOptionPanel("diffOption", new AbstractReadOnlyModel<Depot>() {

				@Override
				public Depot getObject() {
					return RevisionComparePage.this.getDepot();
				}
				
			}, RevisionComparePage.this.source.getRevision()) {

				@Override
				protected void onSelectPath(AjaxRequestTarget target, String path) {
					RevisionComparePage.this.path = path;
					newRevDiffPanel(tabPanel, target);
					pushState(target);
				}

				@Override
				protected void onLineProcessorChange(AjaxRequestTarget target) {
					newRevDiffPanel(tabPanel, target);
				}

				@Override
				protected void onDiffModeChange(AjaxRequestTarget target) {
					newRevDiffPanel(tabPanel, target);
				}
				
			};
			diffOption.add(new StickyBehavior());
			tabPanel.add(diffOption);
			newRevDiffPanel(tabPanel, null);
			commitsTab.setSelected(false);
			filesTab.setSelected(true);
		} else {
			tabPanel = new CommitListPanel(TAB_PANEL_ID, depotModel, commitsModel){

				@Override
				protected void onConfigure() {
					super.onConfigure();
					
					setVisible(!isIntegrated());
				}
				
			};
			commitsTab.setSelected(true);
			filesTab.setSelected(false);
		}
		tabPanel.setOutputMarkupId(true);
		if (target != null) {
			replace(tabPanel);
			target.add(tabPanel);
		} else {
			add(tabPanel);
		}
	}
	
	private void newRevDiffPanel(final WebMarkupContainer tabPanel, @Nullable AjaxRequestTarget target) {
		RevisionDiffPanel diffPanel = new RevisionDiffPanel("revisionDiff", depotModel, 
				new Model<PullRequest>(null), new Model<Comment>(null), 
				mergeBaseModel.getObject(), source.getRevision(), 
				StringUtils.isBlank(path)?null:path, null, diffOption.getLineProcessor(), 
				diffOption.getDiffMode()) {

			@Override
			protected void onClearPath(AjaxRequestTarget target) {
				path = "";
				newRevDiffPanel(tabPanel, target);
				pushState(target);
			}
			
		};
		diffPanel.setOutputMarkupId(true);
		if (target != null) {
			tabPanel.replace(diffPanel);
			target.add(diffPanel);
		} else {
			tabPanel.add(diffPanel);
		}
	}

	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), RevisionComparePage.this.target, source, path);
		CharSequence url = RequestCycle.get().urlFor(RevisionComparePage.class, params);
		pushState(target, url.toString(), path);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		path = (String) data;
		newTabPanel(target);
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		mergeBaseModel.detach();
		commitsModel.detach();

		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RepoBranchesPage.class, paramsOf(depot));
	}

}
