package com.gitplex.server.web.page.depot.branches;

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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.RefInfo;
import com.gitplex.server.manager.BranchWatchManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.BranchWatch;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.DepotAndBranch;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.gitplex.server.web.component.branchchoice.BranchChoiceProvider;
import com.gitplex.server.web.component.branchchoice.BranchSingleChoice;
import com.gitplex.server.web.component.contributorpanel.ContributorPanel;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.component.revisionpicker.RevisionPicker;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.NoBranchesPage;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.commit.CommitDetailPage;
import com.gitplex.server.web.page.depot.compare.RevisionComparePage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.SearchOption;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.SearchOption.Status;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.SortOption;
import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotBranchesPage extends DepotPage {

	private static final String PARAM_BASE = "base";
	
	// use a small page size to load page quickly
	private static final int PAGE_SIZE = 10;
	
	private String baseBranch;
	
	private IModel<List<RefInfo>> branchesModel = new LoadableDetachableModel<List<RefInfo>>() {

		@Override
		protected List<RefInfo> load() {
			List<RefInfo> refs = getDepot().getBranches();
			String searchFor = searchField.getModelObject();
			if (StringUtils.isNotBlank(searchFor)) {
				searchFor = searchFor.trim().toLowerCase();
				for (Iterator<RefInfo> it = refs.iterator(); it.hasNext();) {
					String branch = GitUtils.ref2branch(it.next().getRef().getName());
					if (!branch.toLowerCase().contains(searchFor))
						it.remove();
				}
			}
			return refs;
		}
		
	};
	
	private BranchSingleChoice baseChoice;
	
	private PageableListView<RefInfo> branchesView;
	
	private Component pagingNavigator;
	
	private WebMarkupContainer branchesContainer; 
	
	private WebMarkupContainer noBranchesContainer;
	
	private TextField<String> searchField;
	
	private final IModel<Map<ObjectId, AheadBehind>> aheadBehindsModel = 
			new LoadableDetachableModel<Map<ObjectId, AheadBehind>>() {

		@SuppressWarnings("unused")
		@Override
		protected Map<ObjectId, AheadBehind> load() {
			List<ObjectId> compareIds = new ArrayList<>(); 
			List<RefInfo> branches = branchesModel.getObject();
			for (long i=branchesView.getFirstItemOffset(); i<branches.size(); i++) {
				if (i-branchesView.getFirstItemOffset() >= branchesView.getItemsPerPage())
					break;
				RefInfo ref = branches.get((int)i); 
				compareIds.add(ref.getRef().getObjectId());
			}

			Ref baseRef = getDepot().getRefs(Constants.R_HEADS).get(baseBranch);
			Preconditions.checkNotNull(baseRef);
			Map<ObjectId, AheadBehind> aheadBehinds = new HashMap<>();
			try (RevWalk revWalk = new RevWalk(getDepot().getRepository())) {
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
			DepotAndBranch depotAndBranch = new DepotAndBranch(getDepot(), baseBranch);
			for (PullRequest request: pullRequestManager.findAllOpenTo(depotAndBranch, getDepot())) 
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
			DepotAndBranch depotAndBranch = new DepotAndBranch(getDepot(), baseBranch);
			for (PullRequest request: pullRequestManager.findAllOpenFrom(depotAndBranch, getDepot())) 
				requests.put(request.getTarget().getBranch(), request);
			return requests;
		}
		
	};

	private final IModel<Map<String, BranchWatch>> branchWatchesModel = 
			new LoadableDetachableModel<Map<String, BranchWatch>>() {

		@Override
		protected Map<String, BranchWatch> load() {
			Map<String, BranchWatch> watches = new HashMap<>();
			for (BranchWatch watch: GitPlex.getInstance(BranchWatchManager.class).find(
					Preconditions.checkNotNull(getLoginUser()), getDepot()))
				watches.put(watch.getBranch(), watch);
			return watches;
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
		if (baseBranch == null)
			baseBranch = Preconditions.checkNotNull(getDepot().getDefaultBranch());
		
		if (getDepot().getDefaultBranch() == null) 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getDepot()));
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
				return baseBranch;
			}

			@Override
			public void setObject(String object) {
				baseBranch = object;
			}
			
		}, new BranchChoiceProvider(depotModel)));
		
		baseChoice.setRequired(true);
		
		baseChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(branchesContainer);
				target.add(pagingNavigator);
				target.add(noBranchesContainer);
				searchField.setModelObject(null);
				target.add(searchField);
				
				pushState(target);
			}
			
		});
		
		add(searchField = new TextField<String>("searchBranches", Model.of("")));
		searchField.add(new OnSearchingBehavior());

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
							closeModal();
							target.add(branchesContainer);
							target.add(pagingNavigator);
							target.add(noBranchesContainer);
							searchField.setModelObject(null);
							target.add(searchField);
						}
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						closeModal();
					}
					
				});
				fragment.add(form);
				return fragment;
			}
			
		});
		
		add(branchesContainer = new WebMarkupContainer("branches") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!branchesModel.getObject().isEmpty());
			}
			
		});
		branchesContainer.setOutputMarkupPlaceholderTag(true);
		
		branchesContainer.add(branchesView = new PageableListView<RefInfo>("branches", branchesModel, PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<RefInfo> item) {
				RefInfo ref = item.getModelObject();
				String branch = GitUtils.ref2branch(ref.getRef().getName());
				
				DepotBlobPage.State state = new DepotBlobPage.State();
				state.blobIdent.revision = branch;
				AbstractLink link = new ViewStateAwarePageLink<Void>("branchLink", 
						DepotBlobPage.class, DepotBlobPage.paramsOf(getDepot(), state));
				link.add(new Label("name", branch));
				item.add(link);
				
				item.add(new AjaxLink<Void>("makeDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getDepot().setDefaultBranch(branch);
						target.add(branchesContainer);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(getDepot()) 
								&& !branch.equals(getDepot().getDefaultBranch()));
					}
					
				});
				
				item.add(new WebMarkupContainer("default") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getDepot().getDefaultBranch().equals(branch));
					}
					
				});
				
				RevCommit lastCommit = getDepot().getRevCommit(ref.getRef().getObjectId());
				PageParameters params = CommitDetailPage.paramsOf(getDepot(), lastCommit.name());

				link = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				link.add(new Label("hash", GitUtils.abbreviateSHA(lastCommit.name())));
				item.add(link);
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(lastCommit.name()))));
				
				item.add(new ContributorPanel("contributor", lastCommit.getAuthorIdent(), lastCommit.getCommitterIdent(), true));
				link = new ViewStateAwarePageLink<Void>("messageLink", CommitDetailPage.class, params);
				link.add(new Label("message", lastCommit.getShortMessage()));
				item.add(link);
				
				AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(lastCommit));
				
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
						if (ab.getBehind() == 0)
							tag.setName("span");
					}

					@Override
					public void onClick() {
						PullRequest request = behindOpenRequestsModel.getObject().get(branch);
						if (request != null) {
							setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						} else {
							RevisionComparePage.State state = new RevisionComparePage.State();
							state.leftSide = new DepotAndBranch(getDepot(), branch);
							state.rightSide = new DepotAndBranch(getDepot(), baseBranch);
							PageParameters params = RevisionComparePage.paramsOf(getDepot(), state); 
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
						if (ab.getAhead() == 0)
							tag.setName("span");
					}

					@Override
					public void onClick() {
						PullRequest request = aheadOpenRequestsModel.getObject().get(branch);
						if (request != null) {
							setResponsePage(RequestOverviewPage.class, RequestOverviewPage.paramsOf(request));
						} else {
							RevisionComparePage.State state = new RevisionComparePage.State();
							state.leftSide = new DepotAndBranch(getDepot(), baseBranch);
							state.rightSide = new DepotAndBranch(getDepot(), branch);
							PageParameters params = RevisionComparePage.paramsOf(getDepot(), state);
							setResponsePage(RevisionComparePage.class, params);
						}
					}
					
				});
				item.add(new Label("aheadBar") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						if (aheadOpenRequestsModel.getObject().get(branch) != null)
							add(AttributeAppender.append("class", " request"));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("style", "width: " + aheadBehindWidthModel.getObject().get(ab.getAhead()));
					}
					
				});
				
				WebMarkupContainer actionsContainer = new WebMarkupContainer("actions");
				item.add(actionsContainer.setOutputMarkupId(true));
				
				actionsContainer.add(new AjaxLink<Void>("unwatchRequests") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						BranchWatch watch = branchWatchesModel.getObject().get(branch);
						if (watch != null) {
							GitPlex.getInstance(BranchWatchManager.class).delete(watch);
							branchWatchesModel.getObject().remove(branch);
						}
						target.add(actionsContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getLoginUser() != null && branchWatchesModel.getObject().containsKey(branch));
					}

				});
				actionsContainer.add(new AjaxLink<Void>("watchRequests") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						BranchWatch watch = new BranchWatch();
						watch.setDepot(getDepot());
						watch.setBranch(branch);
						watch.setUser(getLoginUser());
						GitPlex.getInstance(BranchWatchManager.class).save(watch);
						target.add(actionsContainer);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getLoginUser() != null && !branchWatchesModel.getObject().containsKey(branch));
					}

				});

				actionsContainer.add(new ModalLink("delete") {

					@Override
					protected Component newContent(String id) {
						Fragment fragment = new Fragment(id, "confirmDeleteBranchFrag", DepotBranchesPage.this);
						PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
						if (!pullRequestManager.findAllOpen(new DepotAndBranch(getDepot(), branch)).isEmpty()) {
							Fragment bodyFrag = new Fragment("body", "openRequestsFrag", DepotBranchesPage.this);
							SearchOption searchOption = new SearchOption();
							searchOption.setStatus(Status.OPEN);
							searchOption.setBranch(branch);
							PageParameters params = RequestListPage.paramsOf(getDepot(), searchOption, new SortOption());
							bodyFrag.add(new ViewStateAwarePageLink<Void>("openRequests", RequestListPage.class, params));
							bodyFrag.add(new Label("branch", branch));
							fragment.add(bodyFrag);
						} else {
							fragment.add(new Label("body", "You selected to delete branch " + branch));
						}
						fragment.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								getDepot().deleteBranch(branch);
								if (branch.equals(baseBranch)) {
									baseBranch = getDepot().getDefaultBranch();
									target.add(baseChoice);
								}
								target.add(pagingNavigator);
								target.add(branchesContainer);
								target.add(noBranchesContainer);
								closeModal();
							}
							
						});
						fragment.add(new AjaxLink<Void>("cancel") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								closeModal();
							}
							
						});
						return fragment;
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						RefInfo ref = item.getModelObject();
						if (!getDepot().getDefaultBranch().equals(branch) 
								&& SecurityUtils.canPushRef(getDepot(), ref.getRef().getName(), ref.getRef().getObjectId(), ObjectId.zeroId())) {
							setVisible(true);
						} else {
							setVisible(false);
						}
					}

				});
								
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
		
		add(noBranchesContainer = new WebMarkupContainer("noBranches") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(branchesModel.getObject().isEmpty());
			}
			
		});
		noBranchesContainer.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DepotBranchesResourceReference()));
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
			target.add(noBranchesContainer);
		}

		@Override
		public String getAjaxIndicatorMarkupId() {
			return "searching-branches";
		}
		
	}

	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), baseBranch);
		CharSequence url = RequestCycle.get().urlFor(DepotBranchesPage.class, params);
		pushState(target, url.toString(), baseBranch);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		baseBranch = (String) data;
		target.add(baseChoice);
		target.add(branchesContainer);
		target.add(noBranchesContainer);
		target.add(pagingNavigator);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotBranchesPage.class, paramsOf(depot));
	}
}
