package com.pmease.gitplex.web.page.repository.code.branches;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.validator.routines.PercentValidator;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.navigator.PagingNavigator;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.gatekeeper.checkresult.Approved;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.CommitWatchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestWatchManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.CommitWatch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestWatch;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.branch.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branch.BranchSingleChoice;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.git.command.AheadBehind;
import com.pmease.gitplex.web.git.command.AheadBehindCommand;
import com.pmease.gitplex.web.git.command.BranchForEachRefCommand;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.code.compare.BranchComparePage;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestActivitiesPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class BranchesPage extends RepositoryPage {

	private Long baseBranchId;
	
	private PagingNavigator pagingNavigator;
	
	private WebMarkupContainer branchesContainer; 
	
	private TextField<String> searchInput;
	
	private final IModel<Map<String, BriefCommit>> lastCommitsModel = 
			new LoadableDetachableModel<Map<String, BriefCommit>>() {

		@Override
		protected Map<String, BriefCommit> load() {
			BranchForEachRefCommand cmd = new BranchForEachRefCommand(getRepository().git().repoDir());
			return cmd.call();
		}
		
	};
	
	private final IModel<Map<String, AheadBehind>> aheadBehindsModel = 
			new LoadableDetachableModel<Map<String, AheadBehind>>() {

		@Override
		protected Map<String, AheadBehind> load() {
			Map<String, AheadBehind> map = Maps.newHashMap();
			File repoDir = getRepository().git().repoDir();
			for (Branch branch: getRepository().getBranches()) {
				if (branch.equals(getBaseBranch())) {
					map.put(branch.getName(), new AheadBehind());
				} else {
					AheadBehindCommand command = new AheadBehindCommand(repoDir);
					AheadBehind ab = command.leftBranch(branch.getName()).rightBranch(getBaseBranch().getName()).call();
					map.put(branch.getName(), ab);
				}
			}
			
			return map;
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
			abValues.remove(0);
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
			for (PullRequest request: GitPlex.getInstance(PullRequestManager.class).findOpenTo(getBaseBranch(), getRepository())) 
				requests.put(request.getSource().getName(), request);
			return requests;
		}
		
	};
	
	private final IModel<Map<String, PullRequest>> behindOpenRequestsModel = 
			new LoadableDetachableModel<Map<String, PullRequest>>() {

		@Override
		protected Map<String, PullRequest> load() {
			Map<String, PullRequest> requests = new HashMap<>();
			for (PullRequest request: GitPlex.getInstance(PullRequestManager.class).findOpenFrom(getBaseBranch(), getRepository())) 
				requests.put(request.getTarget().getName(), request);
			return requests;
		}
		
	};

	private final IModel<Map<String, CommitWatch>> commitWatchesModel = 
			new LoadableDetachableModel<Map<String, CommitWatch>>() {

		@Override
		protected Map<String, CommitWatch> load() {
			Map<String, CommitWatch> commitWatches = new HashMap<>();
			for (CommitWatch watch: GitPlex.getInstance(CommitWatchManager.class).findBy(
					Preconditions.checkNotNull(getCurrentUser()), getRepository()))
				commitWatches.put(watch.getBranch().getName(), watch);
			return commitWatches;
		}
		
	};

	private final IModel<Map<String, PullRequestWatch>> requestWatchesModel = 
			new LoadableDetachableModel<Map<String, PullRequestWatch>>() {

		@Override
		protected Map<String, PullRequestWatch> load() {
			Map<String, PullRequestWatch> requestWatches = new HashMap<>();
			for (PullRequestWatch watch: GitPlex.getInstance(PullRequestWatchManager.class).findBy(
					Preconditions.checkNotNull(getCurrentUser()), getRepository()))
				requestWatches.put(watch.getBranch().getName(), watch);
			return requestWatches;
		}
		
	};

	public BranchesPage(PageParameters params) {
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
		
		add(new BranchSingleChoice("baseBranch", new IModel<Branch>() {

			@Override
			public void detach() {
			}

			@Override
			public Branch getObject() {
				return getBaseBranch();
			}

			@Override
			public void setObject(Branch object) {
				baseBranchId = object.getId();
			}
			
		}, new BranchChoiceProvider(repoModel)).add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(BranchesPage.this);
			}
			
		}));
		
		add(searchInput = new ClearableTextField<String>("searchBranches", Model.of("")));
		searchInput.add(new OnSearchingBehavior());
		
		branchesContainer = new WebMarkupContainer("branchesContainer");
		branchesContainer.setOutputMarkupId(true);
		add(branchesContainer);
		
		PageableListView<Branch> branchesView;
		branchesContainer.add(branchesView = new PageableListView<Branch>("branches", new AbstractReadOnlyModel<List<Branch>>() {

			@Override
			public List<Branch> getObject() {
				List<Branch> branches = new ArrayList<>(getRepository().getBranches());
				String searchFor = searchInput.getInput();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<Branch> it = branches.iterator(); it.hasNext();) {
						Branch branch = it.next();
						if (!branch.getName().toLowerCase().contains(searchFor))
							it.remove();
					}
				}
				Collections.sort(branches, new Comparator<Branch>() {

					@Override
					public int compare(Branch branch1, Branch branch2) {
						if (branch1.isDefault()) {
							return -1;
						} else if (branch2.isDefault()) {
							return 1;
						} else { 
							BriefCommit commit1 = lastCommitsModel.getObject().get(branch1.getName());
							BriefCommit commit2 = lastCommitsModel.getObject().get(branch2.getName());
							Preconditions.checkState(commit1 != null && commit2 != null);
							return commit2.getAuthor().getWhen().compareTo(commit1.getAuthor().getWhen());
						}
					}
					
				});
				return branches;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Branch> item) {
				Branch branch = item.getModelObject();
				
				AbstractLink link = new BookmarkablePageLink<Void>("branchLink", 
						RepoTreePage.class,
						RepoTreePage.paramsOf(getRepository(), branch.getName(), null));
				link.add(new Label("name", branch.getName()));
				item.add(link);
				
				BriefCommit lastCommit = Preconditions.checkNotNull(lastCommitsModel.getObject().get(branch.getName()));

				item.add(new AgeLabel("lastUpdateTime", Model.of(lastCommit.getAuthor().getWhen())));
				item.add(new PersonLink("lastAuthor", Model.of(lastCommit.getAuthor()), AvatarMode.NAME));
				
				item.add(new WebMarkupContainer("default") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(item.getModelObject().isDefault());
					}
					
				});
				
				final AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(branch.getName()));
				
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
						add(new Label("status", new LoadableDetachableModel<String>() {

							@Override
							protected String load() {
								PullRequest request = behindOpenRequestsModel.getObject().get(item.getModelObject().getName());
								if (request != null)
									return "- " + request.getStatus().toString().toLowerCase();
								else
									return "";
							}
							
						}));
					}

					@Override
					public void onClick() {
						Branch branch = item.getModelObject();
						PullRequest request = behindOpenRequestsModel.getObject().get(branch.getName());
						if (request != null) {
							setResponsePage(RequestActivitiesPage.class, RequestActivitiesPage.paramsOf(request));
						} else {
							setResponsePage(BranchComparePage.class, 
									BranchComparePage.paramsOf(getRepository(), getBaseBranch(), branch));
						}
					}
					
				});
				item.add(new Label("behindBar") {

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
						add(new Label("status", new LoadableDetachableModel<String>() {

							@Override
							protected String load() {
								PullRequest request = aheadOpenRequestsModel.getObject().get(item.getModelObject().getName());
								if (request != null)
									return "- " + request.getStatus().toString().toLowerCase();
								else
									return "";
							}
							
						}).setEscapeModelStrings(false));
					}

					@Override
					public void onClick() {
						Branch branch = item.getModelObject();
						PullRequest request = aheadOpenRequestsModel.getObject().get(branch.getName());
						if (request != null) {
							setResponsePage(RequestActivitiesPage.class, RequestActivitiesPage.paramsOf(request));
						} else {
							setResponsePage(BranchComparePage.class, 
									BranchComparePage.paramsOf(getRepository(), branch, getBaseBranch()));
						}
					}
					
				});
				item.add(new Label("aheadBar") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("style", "width: " + aheadBehindWidthModel.getObject().get(ab.getAhead()));
					}
					
				});

				final WebMarkupContainer watchContainer = new WebMarkupContainer("watch");
				item.add(watchContainer.setOutputMarkupId(true));
				
				watchContainer.add(new AjaxLink<Void>("unwatchCommits") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String branchName = item.getModelObject().getName();
						CommitWatch watch = commitWatchesModel.getObject().get(branchName);
						if (watch != null) {
							GitPlex.getInstance(Dao.class).remove(watch);
							commitWatchesModel.getObject().remove(branchName);
						}
						target.add(watchContainer);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getCurrentUser() != null 
								&& commitWatchesModel.getObject().containsKey(item.getModelObject().getName()));
					}
					
				});
				watchContainer.add(new AjaxLink<Void>("unwatchRequests") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						String branchName = item.getModelObject().getName();
						PullRequestWatch watch = requestWatchesModel.getObject().get(branchName);
						if (watch != null) {
							GitPlex.getInstance(Dao.class).remove(watch);
							requestWatchesModel.getObject().remove(branchName);
						}
						target.add(watchContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getCurrentUser() != null 
								&& requestWatchesModel.getObject().containsKey(item.getModelObject().getName()));
					}

				});
				watchContainer.add(new AjaxLink<Void>("watchCommits") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						CommitWatch watch = new CommitWatch();
						watch.setBranch(item.getModelObject());
						watch.setUser(getCurrentUser());
						GitPlex.getInstance(Dao.class).persist(watch);
						target.add(watchContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getCurrentUser() != null 
								&& !commitWatchesModel.getObject().containsKey(item.getModelObject().getName()));
					}

				});
				watchContainer.add(new AjaxLink<Void>("watchRequests") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequestWatch watch = new PullRequestWatch();
						watch.setBranch(item.getModelObject());
						watch.setUser(getCurrentUser());
						GitPlex.getInstance(Dao.class).persist(watch);
						target.add(watchContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getCurrentUser() != null 
								&& !requestWatchesModel.getObject().containsKey(item.getModelObject().getName()));
					}

				});

				Link<Void> deleteLink;
				item.add(deleteLink = new Link<Void>("delete") {

					@Override
					public void onClick() {
						GitPlex.getInstance(BranchManager.class).delete(item.getModelObject());
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						Branch branch = item.getModelObject();
						if (!branch.isDefault() && SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(getRepository()))) {
							User currentUser = getCurrentUser();
							if (currentUser != null) {
								GateKeeper gateKeeper = getRepository().getGateKeeper();
								CheckResult checkResult = gateKeeper.checkBranch(currentUser, branch);
								if (checkResult instanceof Approved) {
									setVisible(true);
									PullRequest aheadOpen = aheadOpenRequestsModel.getObject().get(branch.getName());
									PullRequest behindOpen = behindOpenRequestsModel.getObject().get(branch.getName());
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
				deleteLink.add(new ConfirmBehavior("Do you really want to delete this branch?"));				
				PullRequest aheadOpen = aheadOpenRequestsModel.getObject().get(branch.getName());
				PullRequest behindOpen = behindOpenRequestsModel.getObject().get(branch.getName());
				if (aheadOpen != null || behindOpen != null) {
					String hint = "This branch can not be deleted as there are <br>pull request openning against it."; 
					deleteLink.add(new TooltipBehavior(Model.of(hint), new TooltipConfig().withPlacement(Placement.left)));
					deleteLink.add(AttributeAppender.append("data-html", "true"));
				}
			}
			
		});

		add(pagingNavigator = new PagingNavigator("branchesPageNav", branchesView));
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	private User getCurrentUser() {
		return GitPlex.getInstance(UserManager.class).getCurrent();
	}

	private Branch getBaseBranch() {
		if (baseBranchId != null)
			return GitPlex.getInstance(Dao.class).load(Branch.class, baseBranchId);
		else
			return getRepository().getDefaultBranch();
	}
	
	@Override
	public void onDetach() {
		lastCommitsModel.detach();
		aheadOpenRequestsModel.detach();
		behindOpenRequestsModel.detach();
		requestWatchesModel.detach();
		commitWatchesModel.detach();
		aheadBehindsModel.detach();
		aheadBehindWidthModel.detach();
		
		super.onDetach();
	}
	
	private class OnSearchingBehavior extends OnChangeAjaxBehavior implements IAjaxIndicatorAware {

		@Override
		protected void onUpdate(AjaxRequestTarget target) {
			target.add(branchesContainer);
			target.add(pagingNavigator);
		}

		@Override
		public String getAjaxIndicatorMarkupId() {
			return "searching-branches";
		}
		
	}
}
