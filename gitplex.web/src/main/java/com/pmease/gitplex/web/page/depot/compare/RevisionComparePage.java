package com.pmease.gitplex.web.page.depot.compare;

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
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.diff.revision.option.DiffOptionPanel;
import com.pmease.gitplex.web.component.revisionpicker.AffinalRevisionPicker;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;

import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class RevisionComparePage extends DepotPage {

	private static final String PARAM_LEFT = "left";
	
	private static final String PARAM_RIGHT = "right";
	
	private static final String PARAM_COMPARE_WITH_MERGE_BASE = "compareWithMergeBase";
	
	private static final String PARAM_PATH = "path";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;

	private DepotAndRevision rightSide;
	
	private DepotAndRevision leftSide;
	
	private DiffOptionPanel diffOption;
	
	private AjaxActionTab commitsTab;
	
	private AjaxActionTab filesTab;
	
	private boolean compareWithMergeBase; 
	
	private String path;
	
	public static PageParameters paramsOf(Depot depot, DepotAndRevision target, 
			DepotAndRevision source, boolean compareWithMergeBase, @Nullable String path) {
		PageParameters params = paramsOf(depot);
		params.set(PARAM_LEFT, target.toString());
		params.set(PARAM_RIGHT, source.toString());
		params.set(PARAM_COMPARE_WITH_MERGE_BASE, compareWithMergeBase);
		if (path != null)
			params.set(PARAM_PATH, path);
		return params;
	}

	public RevisionComparePage(final PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasRefs()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));

		String str = params.get(PARAM_RIGHT).toString();
		if (str != null) {
			leftSide = new DepotAndRevision(str);
		} else {
			leftSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		
		str = params.get(PARAM_LEFT).toString();
		if (str != null) {
			rightSide = new DepotAndRevision(str);
		} else {
			rightSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		
		compareWithMergeBase = params.get(PARAM_COMPARE_WITH_MERGE_BASE).toBoolean(false);
		
		path = params.get(PARAM_PATH).toString();
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				DepotAndBranch target = new DepotAndBranch(rightSide.toString());
				DepotAndBranch source = new DepotAndBranch(leftSide.toString());
				return GitPlex.getInstance(PullRequestManager.class).findOpen(target, source);
			}
			
		};

		mergeBaseModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Depot targetDepot = rightSide.getDepot();
				Depot sourceDepot = leftSide.getDepot();
				if (!targetDepot.equals(sourceDepot)) {
					Git sandbox = new Git(FileUtils.createTempDir());
					try {
						sandbox.clone(targetDepot.git(), false, true, true, rightSide.getRevision());
						sandbox.reset(null, null);
						sandbox.fetch(sourceDepot.git(), leftSide.getRevision());
						return sandbox.calcMergeBase(rightSide.getCommit().name(), leftSide.getCommit().name());
					} finally {
						FileUtils.deleteDir(sandbox.depotDir());
					}
				} else {
					return targetDepot.getMergeBase(rightSide.getRevision(), leftSide.getRevision()).name();
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Depot sourceDepot = leftSide.getDepot();
				LogCommand log = new LogCommand(sourceDepot.git().depotDir());
				List<Commit> commits;
				if (!compareWithMergeBase) {
					String revisions = rightSide.getRevision() + "..." + leftSide.getRevision();
					commits = log.revisions(Lists.newArrayList(revisions)).call();
					commits.add(sourceDepot.git().showRevision(mergeBaseModel.getObject()));
				} else {
					commits = sourceDepot.git().log(rightSide.getRevision(), leftSide.getRevision(), null, 0, 0, false);
				}
				
				return commits;
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalRevisionPicker("target", rightSide.getDepotId(), rightSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				PageParameters params = paramsOf(getDepot(), new DepotAndRevision(depot, revision), 
						leftSide, compareWithMergeBase, path);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});

		add(new AffinalRevisionPicker("source", leftSide.getDepotId(), leftSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				PageParameters params = paramsOf(getDepot(), rightSide, 
						new DepotAndRevision(depot, revision), compareWithMergeBase, path);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				setResponsePage(RevisionComparePage.class,paramsOf(getDepot(), 
						leftSide, rightSide, compareWithMergeBase, path));
			}

		});
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				DepotAndBranch target = new DepotAndBranch(rightSide.toString());
				DepotAndBranch source = new DepotAndBranch(leftSide.toString());
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(target.getDepot(), target, source));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (rightSide.getBranch()!=null && leftSide.getBranch()!=null && compareWithMergeBase) {
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
				setVisible(leftSide.equals(rightSide));
			}
			
		});
		add(new WebMarkupContainer("openedRequest") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (compareWithMergeBase) {
					PullRequest request = requestModel.getObject();
					setVisible(request != null);
				} else {
					setVisible(false);
				}
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
				
				setVisible(!leftSide.equals(rightSide) 
						&& compareWithMergeBase 
						&& mergeBaseModel.getObject().equals(leftSide.getCommit().name()));
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
		return mergeBaseModel.getObject().equals(leftSide.getCommit().name());
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
					return getDepot();
				}
				
			}, leftSide.getRevision()) {

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
				compareWithMergeBase?mergeBaseModel.getObject():rightSide.getRevision(), 
				leftSide.getRevision(), 
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
		PageParameters params = paramsOf(getDepot(), rightSide, leftSide, 
				compareWithMergeBase, path);
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
		setResponsePage(DepotBranchesPage.class, paramsOf(depot));
	}

	
}
