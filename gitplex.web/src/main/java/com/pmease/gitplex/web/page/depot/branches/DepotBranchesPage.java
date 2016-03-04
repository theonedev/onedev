package com.pmease.gitplex.web.page.depot.branches;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.validator.routines.PercentValidator;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.BranchWatch;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.manager.BranchWatchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.branchchoice.BranchChoiceProvider;
import com.pmease.gitplex.web.component.branchchoice.BranchSingleChoice;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.revisionpicker.RevisionPicker;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotBranchesPage extends DepotPage {

	private static final String PARAM_BASE = "base";
	
	// use a small page size to load page quickly
	private static final int PAGE_SIZE = 10;
	
	private String baseBranch;
	
	private IModel<List<Ref>> branchesModel = new AbstractReadOnlyModel<List<Ref>>() {

		@Override
		public List<Ref> getObject() {
			List<Ref> refs = getDepot().getBranchRefs();
			String searchFor = searchInput.getModelObject();
			if (StringUtils.isNotBlank(searchFor)) {
				searchFor = searchFor.trim().toLowerCase();
				for (Iterator<Ref> it = refs.iterator(); it.hasNext();) {
					String branch = GitUtils.ref2branch(it.next().getName());
					if (!branch.toLowerCase().contains(searchFor))
						it.remove();
				}
			}
			return refs;
		}
		
	};
	
	private BranchSingleChoice baseChoice;
	
	private PageableListView<Ref> branchesView;
	
	private Component pagingNavigator;
	
	private WebMarkupContainer branchesContainer; 
	
	private TextField<String> searchInput;
	
	private final IModel<Map<ObjectId, AheadBehind>> aheadBehindsModel = 
			new LoadableDetachableModel<Map<ObjectId, AheadBehind>>() {

		@SuppressWarnings("unused")
		@Override
		protected Map<ObjectId, AheadBehind> load() {
			List<ObjectId> compareIds = new ArrayList<>(); 
			List<Ref> branches = branchesModel.getObject();
			for (long i=branchesView.getFirstItemOffset(); i<branches.size(); i++) {
				if (i-branchesView.getFirstItemOffset() >= branchesView.getItemsPerPage())
					break;
				Ref ref = branches.get((int)i); 
				compareIds.add(ref.getObjectId());
			}

			Ref baseRef = getDepot().getRefs(Constants.R_HEADS).get(getBaseBranch());
			Preconditions.checkNotNull(baseRef);
			Map<ObjectId, AheadBehind> aheadBehinds = new HashMap<>();
			try (	Repository repository = getDepot().openRepository();
					RevWalk revWalk = new RevWalk(repository);) {

				RevCommit baseCommit = revWalk.lookupCommit(baseRef.getObjectId());
				for (ObjectId compareId: compareIds) {
					RevCommit compareCommit = revWalk.lookupCommit(compareId);
					revWalk.markUninteresting(baseCommit);
					revWalk.markStart(compareCommit);
					int ahead = 0;
					for (RevCommit commit: revWalk)
						ahead++;
					revWalk.reset();
					
					revWalk.markUninteresting(compareCommit);
					revWalk.markStart(baseCommit);
					int behind = 0;
					for (RevCommit commit: revWalk)
						behind++;
					revWalk.reset();
					
					aheadBehinds.put(compareId, new AheadBehind(ahead, behind));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
			DepotAndBranch depotAndBranch = new DepotAndBranch(getDepot(), getBaseBranch());
			for (PullRequest request: pullRequestManager.queryOpenTo(depotAndBranch, getDepot())) 
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
			DepotAndBranch depotAndBranch = new DepotAndBranch(getDepot(), getBaseBranch());
			for (PullRequest request: pullRequestManager.queryOpenFrom(depotAndBranch, getDepot())) 
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
					Preconditions.checkNotNull(getCurrentUser()), getDepot()))
				requestWatches.put(watch.getBranch(), watch);
			return requestWatches;
		}
		
	};

	public static PageParameters paramsOf(Depot depot, @Nullable String baseBranch) {
		PageParameters params = paramsOf(depot);
		if (baseBranch != null)
			params.add(PARAM_BASE, baseBranch);
		return params;
	}
	
	public DepotBranchesPage(PageParameters params) {
		super(params);
		
		baseBranch = params.get(PARAM_BASE).toString();
		
		if (!getDepot().git().hasRefs()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));
	}
	
	@Override
	protected String getPageTitle() {
		return getDepot() + " - Branches";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(baseChoice = new BranchSingleChoice("baseBranch", new IModel<String>() {

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
			
		}, new BranchChoiceProvider(depotModel), false));
		
		baseChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchesContainer);
				target.add(pagingNavigator);
				searchInput.setModelObject(null);
				target.add(searchInput);
				
				pushState(target);
			}
			
		});
		
		add(searchInput = new ClearableTextField<String>("searchBranches", Model.of("")));
		searchInput.add(new OnSearchingBehavior());

		add(new ModalLink("createBranch") {

			private String branchName;
			
			private String branchRevision = getDepot().getDefaultBranch();
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectId commit = getDepot().getObjectId(branchRevision);
				setVisible(SecurityUtils.canPushRef(getDepot(), Constants.R_HEADS, ObjectId.zeroId(), commit));
			}

			private RevisionPicker newRevisionPicker() {
				return new RevisionPicker("revision", depotModel, branchRevision) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String revision) {
						branchRevision = revision; 
						RevisionPicker revisionPicker = newRevisionPicker();
						getParent().replace(revisionPicker);
						target.add(revisionPicker);
					}
					
				};
			}
			
			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "createBranchFrag", DepotBranchesPage.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				form.add(new NotificationPanel("feedback", form));
				branchName = null;
				
				final TextField<String> nameInput;
				form.add(nameInput = new TextField<String>("name", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return branchName;
					}

					@Override
					public void setObject(String object) {
						branchName = object;
					}
					
				}));
				nameInput.setOutputMarkupId(true);
				
				form.add(newRevisionPicker());
				form.add(new AjaxButton("create") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						
						if (branchName == null) {
							form.error("Branch name is required.");
							target.focusComponent(nameInput);
							target.add(form);
						} else if (!Repository.isValidRefName(Constants.R_HEADS + branchName)) {
							form.error("Invalid branch name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else if (getDepot().getObjectId(GitUtils.branch2ref(branchName), false) != null) {
							form.error("Branch '" + branchName + "' already exists, please choose a different name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else {
							depotModel.getObject().createBranch(branchName, branchRevision);
							close(target);
							target.add(branchesContainer);
							target.add(pagingNavigator);
							searchInput.setModelObject(null);
							target.add(searchInput);
						}
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						close(target);
					}
					
				});
				fragment.add(form);
				return fragment;
			}
			
		});
		
		branchesContainer = new WebMarkupContainer("branchesContainer");
		branchesContainer.setOutputMarkupId(true);
		add(branchesContainer);
		
		branchesContainer.add(branchesView = new PageableListView<Ref>("branches", branchesModel, PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Ref> item) {
				Ref ref = item.getModelObject();
				final String branch = GitUtils.ref2branch(ref.getName());
				
				HistoryState state = new HistoryState();
				state.blobIdent.revision = branch;
				AbstractLink link = new BookmarkablePageLink<Void>("branchLink", 
						DepotFilePage.class, DepotFilePage.paramsOf(getDepot(), state));
				link.add(new Label("name", branch));
				item.add(link);
				
				RevCommit lastCommit = getDepot().getRevCommit(ref.getObjectId());

				PageParameters params = CommitDetailPage.paramsOf(getDepot(), lastCommit.name());
				link = new BookmarkablePageLink<Void>("commitLink", CommitDetailPage.class, params);
				link.add(new Label("shortMessage", lastCommit.getShortMessage()));
				item.add(link);
				
				item.add(new CommitHashPanel("commitHash", lastCommit.name()));
				item.add(new Label("age", DateUtils.formatAge(lastCommit.getCommitterIdent().getWhen())));
				
				item.add(new WebMarkupContainer("default") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getDepot().getDefaultBranch().equals(branch));
					}
					
				});
				
				final AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(lastCommit));
				
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
						PullRequest request = behindOpenRequestsModel.getObject().get(branch);
						if (request != null) {
							setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						} else {
							PageParameters params = RevisionComparePage.paramsOf(
									getDepot(),
									new DepotAndBranch(getDepot(), branch),
									new DepotAndBranch(getDepot(), getBaseBranch()), 
									null); 
							setResponsePage(RevisionComparePage.class, params);
						}
					}
					
				});
				item.add(new Label("behindBar") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						if (behindOpenRequestsModel.getObject().get(branch) != null)
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
						
						if (aheadOpenRequestsModel.getObject().get(branch) != null) { 
							tag.put("title", "" + ab.getAhead() + " commits ahead of base branch, and there is an "
									+ "open pull request sending these commits to base branch");
						} else {
							tag.put("title", "" + ab.getAhead() + " commits ahead of base branch");
						}
					}

					@Override
					public void onClick() {
						PullRequest request = aheadOpenRequestsModel.getObject().get(branch);
						if (request != null) {
							setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						} else {
							PageParameters params = RevisionComparePage.paramsOf(
									getDepot(), 
									new DepotAndBranch(getDepot(), getBaseBranch()),
									new DepotAndBranch(getDepot(), branch), 
									null);
							setResponsePage(RevisionComparePage.class, params);
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
						watch.setDepot(getDepot());
						watch.setBranch(branch);
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

				AjaxLink<Void> deleteLink;
				actionsContainer.add(deleteLink = new AjaxLink<Void>("delete") {

					
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this branch?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getDepot().deleteBranch(branch);
						if (branch.equals(baseBranch)) {
							baseBranch = getDepot().getDefaultBranch();
							target.add(baseChoice);
						}
						target.add(pagingNavigator);
						target.add(branchesContainer);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						Ref ref = item.getModelObject();
						if (!getDepot().getDefaultBranch().equals(branch) 
								&& SecurityUtils.canPushRef(getDepot(), ref.getName(), ref.getObjectId(), ObjectId.zeroId())) {
							setVisible(true);
							PullRequest aheadOpen = aheadOpenRequestsModel.getObject().get(branch);
							PullRequest behindOpen = behindOpenRequestsModel.getObject().get(branch);
							setEnabled(aheadOpen == null && behindOpen == null);
						} else {
							setVisible(false);
						}
					}
					
				});
				PullRequest aheadOpen = aheadOpenRequestsModel.getObject().get(branch);
				PullRequest behindOpen = behindOpenRequestsModel.getObject().get(branch);
				if (aheadOpen != null || behindOpen != null) {
					String hint = "This branch can not be deleted as there are <br>pull request opening against it."; 
					deleteLink.add(new TooltipBehavior(Model.of(hint), new TooltipConfig().withPlacement(Placement.left)));
					deleteLink.add(AttributeAppender.append("data-html", "true"));
				}
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("branchesPageNav", branchesView) {

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
			return getDepot().getDefaultBranch();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				DepotBranchesPage.class, "depot-branches.css")));
	}

	@Override
	public void onDetach() {
		branchesModel.detach();
		aheadOpenRequestsModel.detach();
		behindOpenRequestsModel.detach();
		branchWatchesModel.detach();
		aheadBehindsModel.detach();
		aheadBehindWidthModel.detach();
		
		super.onDetach();
	}
	
	private class OnSearchingBehavior extends OnTypingDoneBehavior implements IAjaxIndicatorAware {

		public OnSearchingBehavior() {
			super(500);
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

	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), baseBranch);
		CharSequence url = RequestCycle.get().urlFor(RevisionComparePage.class, params);
		pushState(target, url.toString(), baseBranch);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		baseBranch = (String) data;
		target.add(baseChoice);
		target.add(branchesContainer);
		target.add(pagingNavigator);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotBranchesPage.class, paramsOf(depot));
	}
}
