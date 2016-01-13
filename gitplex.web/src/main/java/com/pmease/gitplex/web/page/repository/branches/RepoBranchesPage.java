package com.pmease.gitplex.web.page.repository.branches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.validator.routines.PercentValidator;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.Ref;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.AheadBehind;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ConfirmOnClick;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.model.BranchWatch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchSingleChoice;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.branches.compare.BranchComparePage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.utils.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class RepoBranchesPage extends RepositoryPage {

	private String baseBranch;
	
	private PageableListView<String> branchesView;
	
	private Component pagingNavigator;
	
	private WebMarkupContainer branchesContainer; 
	
	private TextField<String> searchInput;
	
	private String searchFor;
	
	private final IModel<Map<String, BriefCommit>> lastCommitsModel = 
			new LoadableDetachableModel<Map<String, BriefCommit>>() {

		@Override
		protected Map<String, BriefCommit> load() {
			return getRepository().git().listHeadCommits();
		}
		
	};
	
	private final IModel<Map<String, AheadBehind>> aheadBehindsModel = 
			new LoadableDetachableModel<Map<String, AheadBehind>>() {

		@Override
		protected Map<String, AheadBehind> load() {
			List<String> compareHashes = new ArrayList<>(); 
			List<String> branchesInView = branchesView.getModelObject();
			for (long i=branchesView.getFirstItemOffset(); i<branchesInView.size(); i++) {
				if (i-branchesView.getFirstItemOffset() >= branchesView.getItemsPerPage())
					break;
				String branch = branchesInView.get((int)i); 
				if (!branch.equals(getBaseBranch()))
					compareHashes.add(lastCommitsModel.getObject().get(branch).getHash());
			}
			
			String baseHash = lastCommitsModel.getObject().get(getBaseBranch()).getHash();
			Map<String, AheadBehind> aheadBehinds = getRepository().git().getAheadBehinds(
					baseHash, 
					compareHashes.toArray(new String[compareHashes.size()]));
			aheadBehinds.put(baseHash, new AheadBehind());
			
			return aheadBehinds;
		}
	};
	
	private final IModel<Map<Integer, String>> aheadBehindWidthModel = new LoadableDetachableModel<Map<Integer, String>>() {

		@Override
		protected Map<Integer, String> load() {
			/* 
			 * Normalize ahead behind bar width in order not to make most bar very narrow if there 
			 * is a vary large value.
			 */
			Map<Integer, String> map = new HashMap<>();
			for (AheadBehind ab: aheadBehindsModel.getObject().values()) {
				map.put(ab.getAhead(), "0");
				map.put(ab.getBehind(), "0");
			}
			List<Integer> abValues = new ArrayList<>(map.keySet());
			for (Iterator<Integer> it = abValues.iterator(); it.hasNext();) {
				if (it.next().equals(0))
					it.remove();
			}
			Collections.sort(abValues);
			for (int i=0; i<abValues.size(); i++) {
				double percent = (i+1.0d)/abValues.size();
				map.put(abValues.get(i), PercentValidator.getInstance().format(percent, "0.00000%", Locale.US));
			}
			return map;
		}
		
	};
	
	private final IModel<Map<String, PullRequest>> aheadOpenRequestsModel = 
			new LoadableDetachableModel<Map<String, PullRequest>>() {

		@Override
		protected Map<String, PullRequest> load() {
			Map<String, PullRequest> requests = new HashMap<>();
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			RepoAndBranch repoAndBranch = new RepoAndBranch(getRepository(), getBaseBranch());
			for (PullRequest request: pullRequestManager.queryOpenTo(repoAndBranch, getRepository())) 
				requests.put(request.getSource().getBranch(), request);
			return requests;
		}
		
	};
	
	private final IModel<Map<String, PullRequest>> behindOpenRequestsModel = 
			new LoadableDetachableModel<Map<String, PullRequest>>() {

		@Override
		protected Map<String, PullRequest> load() {
			Map<String, PullRequest> requests = new HashMap<>();
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			RepoAndBranch repoAndBranch = new RepoAndBranch(getRepository(), getBaseBranch());
			for (PullRequest request: pullRequestManager.queryOpenFrom(repoAndBranch, getRepository())) 
				requests.put(request.getTarget().getBranch(), request);
			return requests;
		}
		
	};

	private final IModel<Map<String, BranchWatch>> branchWatchesModel = 
			new LoadableDetachableModel<Map<String, BranchWatch>>() {

		@Override
		protected Map<String, BranchWatch> load() {
			Map<String, BranchWatch> requestWatches = new HashMap<>();
			for (BranchWatch watch: GitPlex.getInstance(BranchWatchManager.class).findBy(
					Preconditions.checkNotNull(getCurrentUser()), getRepository()))
				requestWatches.put(watch.getBranch(), watch);
			return requestWatches;
		}
		
	};

	public RepoBranchesPage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}
	
	@Override
	protected String getPageTitle() {
		return getRepository() + " - Branches";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BranchSingleChoice("baseBranch", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getBaseBranch();
			}

			@Override
			public void setObject(String object) {
				baseBranch = object;
			}
			
		}, new BranchChoiceProvider(repoModel), false).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(RepoBranchesPage.this);
			}
			
		}));
		
		add(searchInput = new ClearableTextField<String>("searchBranches", Model.of("")));
		searchInput.add(new OnSearchingBehavior());
		
		branchesContainer = new WebMarkupContainer("branchesContainer");
		branchesContainer.setOutputMarkupId(true);
		add(branchesContainer);
		
		branchesContainer.add(branchesView = new PageableListView<String>("branches", new AbstractReadOnlyModel<List<String>>() {

			@Override
			public List<String> getObject() {
				List<String> branches = new ArrayList<>();
				for (Ref ref: getRepository().getBranchRefs())
					branches.add(GitUtils.ref2branch(ref.getName()));
				searchFor = searchInput.getInput();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<String> it = branches.iterator(); it.hasNext();) {
						String branch = it.next();
						if (!branch.toLowerCase().contains(searchFor))
							it.remove();
					}
				} else {
					searchFor = null;
				}
				Collections.sort(branches, new Comparator<String>() {

					@Override
					public int compare(String branch1, String branch2) {
						if (getRepository().getDefaultBranch().equals(branch1)) {
							return -1;
						} else if (getRepository().getDefaultBranch().equals(branch2)) {
							return 1;
						} else { 
							BriefCommit commit1 = lastCommitsModel.getObject().get(branch1);
							BriefCommit commit2 = lastCommitsModel.getObject().get(branch2);
							Preconditions.checkState(commit1 != null && commit2 != null);
							return commit2.getAuthor().getWhen().compareTo(commit1.getAuthor().getWhen());
						}
					}
					
				});
				return branches;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<String> item) {
				final String branch = item.getModelObject();
				
				RepoFileState state = new RepoFileState();
				state.blobIdent.revision = branch;
				AbstractLink link = new BookmarkablePageLink<Void>("branchLink", 
						RepoFilePage.class, RepoFilePage.paramsOf(getRepository(), state));
				link.add(new Label("name", branch));
				item.add(link);
				
				BriefCommit lastCommit = Preconditions.checkNotNull(lastCommitsModel.getObject().get(branch));

				item.add(new Label("lastUpdateTime", DateUtils.formatAge(lastCommit.getAuthor().getWhen())));
				item.add(new UserLink("lastAuthor", lastCommit.getAuthor()));
				
				item.add(new WebMarkupContainer("default") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getRepository().getDefaultBranch().equals(item.getModelObject()));
					}
					
				});
				
				String branchHash = lastCommitsModel.getObject().get(branch).getHash();
				final AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(branchHash));
				
				item.add(new Link<Void>("behindLink") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setEnabled(ab.getBehind() != 0);
					}

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("count", ab.getBehind()));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						if (behindOpenRequestsModel.getObject().get(item.getModelObject()) != null) { 
							tag.put("title", "" + ab.getBehind() + " commits behind of base branch, and there is an "
									+ "open pull request fetching those commits");
						} else {
							tag.put("title", "" + ab.getBehind() + " commits ahead of base branch");
						}
					}

					@Override
					public void onClick() {
						String branch = item.getModelObject();
						PullRequest request = behindOpenRequestsModel.getObject().get(branch);
						if (request != null) {
							setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						} else {
							PageParameters params = BranchComparePage.paramsOf(
									getRepository(),
									new RepoAndBranch(getRepository(), getBaseBranch()), 
									new RepoAndBranch(getRepository(), branch)); 
							setResponsePage(BranchComparePage.class, params);
						}
					}
					
				});
				item.add(new Label("behindBar") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						if (behindOpenRequestsModel.getObject().get(item.getModelObject()) != null)
							add(AttributeAppender.append("class", " request"));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("style", "width: " + aheadBehindWidthModel.getObject().get(ab.getBehind()));
					}
					
				});
				
				item.add(new Link<Void>("aheadLink") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setEnabled(ab.getAhead() != 0);
					}

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("count", ab.getAhead()));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						if (aheadOpenRequestsModel.getObject().get(item.getModelObject()) != null) { 
							tag.put("title", "" + ab.getAhead() + " commits ahead of base branch, and there is an "
									+ "open pull request sending these commits to base branch");
						} else {
							tag.put("title", "" + ab.getAhead() + " commits ahead of base branch");
						}
					}

					@Override
					public void onClick() {
						String branch = item.getModelObject();
						PullRequest request = aheadOpenRequestsModel.getObject().get(branch);
						if (request != null) {
							setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						} else {
							PageParameters params = BranchComparePage.paramsOf(
									getRepository(), 
									new RepoAndBranch(getRepository(), branch), 
									new RepoAndBranch(getRepository(), getBaseBranch()));
							setResponsePage(BranchComparePage.class, params);
						}
					}
					
				});
				item.add(new Label("aheadBar") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						if (aheadOpenRequestsModel.getObject().get(item.getModelObject()) != null)
							add(AttributeAppender.append("class", " request"));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("style", "width: " + aheadBehindWidthModel.getObject().get(ab.getAhead()));
					}
					
				});

				final WebMarkupContainer actionsContainer = new WebMarkupContainer("actions");
				item.add(actionsContainer.setOutputMarkupId(true));
				
				actionsContainer.add(new AjaxLink<Void>("unwatchRequests") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String branch = item.getModelObject();
						BranchWatch watch = branchWatchesModel.getObject().get(branch);
						if (watch != null) {
							GitPlex.getInstance(Dao.class).remove(watch);
							branchWatchesModel.getObject().remove(branch);
						}
						target.add(actionsContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getCurrentUser() != null 
								&& branchWatchesModel.getObject().containsKey(item.getModelObject()));
					}

				});
				actionsContainer.add(new AjaxLink<Void>("watchRequests") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						BranchWatch watch = new BranchWatch();
						watch.setRepository(getRepository());
						watch.setBranch(item.getModelObject());
						watch.setUser(getCurrentUser());
						GitPlex.getInstance(Dao.class).persist(watch);
						target.add(actionsContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getCurrentUser() != null 
								&& !branchWatchesModel.getObject().containsKey(item.getModelObject()));
					}

				});

				Link<Void> deleteLink;
				actionsContainer.add(deleteLink = new Link<Void>("delete") {

					@Override
					public void onClick() {
						getRepository().deleteBranch(branch);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						String branch = item.getModelObject();
						if (!getRepository().getDefaultBranch().equals(branch) && SecurityUtils.canModify(getRepository(), branch)) {
							User currentUser = getCurrentUser();
							if (currentUser != null) {
								GateKeeper gateKeeper = getRepository().getGateKeeper();
								CheckResult checkResult = gateKeeper.checkFile(currentUser, getRepository(), branch, null);
								if (checkResult instanceof Passed) {
									setVisible(true);
									PullRequest aheadOpen = aheadOpenRequestsModel.getObject().get(branch);
									PullRequest behindOpen = behindOpenRequestsModel.getObject().get(branch);
									setEnabled(aheadOpen == null && behindOpen == null);
								} else {
									setVisible(false);
								}
							} else {
								setVisible(false);
							}
						} else {
							setVisible(false);
						}
					}
					
				});
				deleteLink.add(new ConfirmOnClick("Do you really want to delete this branch?"));				
				PullRequest aheadOpen = aheadOpenRequestsModel.getObject().get(branch);
				PullRequest behindOpen = behindOpenRequestsModel.getObject().get(branch);
				if (aheadOpen != null || behindOpen != null) {
					String hint = "This branch can not be deleted as there are <br>pull request opening against it."; 
					deleteLink.add(new TooltipBehavior(Model.of(hint), new TooltipConfig().withPlacement(Placement.left)));
					deleteLink.add(AttributeAppender.append("data-html", "true"));
				}
			}
			
		});

		add(pagingNavigator = new BootstrapPagingNavigator("branchesPageNav", branchesView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(branchesView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	private String getBaseBranch() {
		if (baseBranch != null)
			return baseBranch;
		else
			return getRepository().getDefaultBranch();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepoBranchesPage.class, "repo-branches.css")));
	}

	@Override
	public void onDetach() {
		lastCommitsModel.detach();
		aheadOpenRequestsModel.detach();
		behindOpenRequestsModel.detach();
		branchWatchesModel.detach();
		aheadBehindsModel.detach();
		aheadBehindWidthModel.detach();
		
		super.onDetach();
	}
	
	private class OnSearchingBehavior extends OnTypingDoneBehavior implements IAjaxIndicatorAware {

		public OnSearchingBehavior() {
			super(1000);
		}

		@Override
		protected void onTypingDone(AjaxRequestTarget target) {
			target.add(branchesContainer);
			target.add(pagingNavigator);
		}

		@Override
		public String getAjaxIndicatorMarkupId() {
			return "searching-branches";
		}
		
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoBranchesPage.class, paramsOf(repository));
	}
}
