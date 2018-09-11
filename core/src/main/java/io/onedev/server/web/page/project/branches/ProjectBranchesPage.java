package io.onedev.server.web.page.project.branches;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.validator.routines.PercentValidator;
import org.apache.wicket.Component;
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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.branchchoice.BranchChoiceProvider;
import io.onedev.server.web.component.branchchoice.BranchSingleChoice;
import io.onedev.server.web.component.build.BuildsStatusPanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.revisionpicker.RevisionPicker;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.activities.RequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.requestlist.RequestListPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class ProjectBranchesPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_BASE = "base";
	
	// use a small page size to load page quickly
	private static final int PAGE_SIZE = 10;
	
	private String baseBranch;
	
	private IModel<List<RefInfo>> branchesModel = new LoadableDetachableModel<List<RefInfo>>() {

		@Override
		protected List<RefInfo> load() {
			List<RefInfo> refs = getProject().getBranches();
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

			Ref baseRef = getProject().getRefs(Constants.R_HEADS).get(baseBranch);
			Preconditions.checkNotNull(baseRef);
			Map<ObjectId, AheadBehind> aheadBehinds = new HashMap<>();
			
			try (RevWalk revWalk = new RevWalk(getProject().getRepository())) {
				RevCommit baseCommit = revWalk.lookupCommit(baseRef.getObjectId());
				revWalk.markStart(baseCommit);
				Map<ObjectId, RevCommit> compareCommits = new HashMap<>();
				for (ObjectId compareId: compareIds) {
					RevCommit compareCommit = revWalk.lookupCommit(compareId);
					compareCommits.put(compareId, compareCommit);
					revWalk.markStart(compareCommit);
				}
				revWalk.setRevFilter(RevFilter.MERGE_BASE);
				RevCommit mergeBase = revWalk.next();
				
				revWalk.reset();
				revWalk.setRevFilter(RevFilter.ALL);

				if (mergeBase != null) {
					revWalk.markStart(baseCommit);
					revWalk.markUninteresting(mergeBase);
					Set<ObjectId> baseSet = new HashSet<>();
					for (RevCommit commit: revWalk) 
						baseSet.add(commit.copy());
					revWalk.reset();
					
					for (ObjectId compareId: compareIds) {
						RevCommit compareCommit = Preconditions.checkNotNull(compareCommits.get(compareId));
						revWalk.markStart(compareCommit);
						revWalk.markUninteresting(mergeBase);
						Set<ObjectId> compareSet = new HashSet<>();
						for (RevCommit commit: revWalk) 
							compareSet.add(commit.copy());
						revWalk.reset();
						
						int ahead = 0;
						for (ObjectId each: compareSet) {
							if (!baseSet.contains(each))
								ahead++;
						}
						int behind = 0;
						for (ObjectId each: baseSet) {
							if (!compareSet.contains(each))
								behind++;
						}
						aheadBehinds.put(compareId, new AheadBehind(ahead, behind));
					}					
				} else {
					for (ObjectId compareId: compareIds) {
						RevCommit compareCommit = Preconditions.checkNotNull(compareCommits.get(compareId));
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
	
	public static PageParameters paramsOf(Project project, @Nullable String baseBranch) {
		PageParameters params = paramsOf(project);
		if (baseBranch != null)
			params.add(PARAM_BASE, baseBranch);
		return params;
	}
	
	public ProjectBranchesPage(PageParameters params) {
		super(params);
		
		baseBranch = params.get(PARAM_BASE).toString();
		if (baseBranch == null)
			baseBranch = Preconditions.checkNotNull(getProject().getDefaultBranch());
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
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
			
		}, new BranchChoiceProvider(projectModel)));
		
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
			
			private String branchRevision = getProject().getDefaultBranch();
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canCreateBranch(getProject(), Constants.R_HEADS));
			}

			private RevisionPicker newRevisionPicker() {
				return new RevisionPicker("revision", projectModel, branchRevision) {

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
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "createBranchFrag", ProjectBranchesPage.this);
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
						} else if (getProject().getObjectId(GitUtils.branch2ref(branchName), false) != null) {
							form.error("Branch '" + branchName + "' already exists, please choose a different name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else {
							Project project = getProject();
							BranchProtection protection = project.getBranchProtection(branchName, getLoginUser());
							if (protection != null && protection.isNoCreation()) {
								form.error("Unable to create protected branch");
								target.focusComponent(nameInput);
								target.add(form);
							} else {
								project.createBranch(branchName, branchRevision);
								modal.close();
								target.add(branchesContainer);
								target.add(pagingNavigator);
								target.add(noBranchesContainer);
								searchField.setModelObject(null);
								target.add(searchField);
							}
						}
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
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
				
				BlobIdent blobIdent = new BlobIdent(branch, null, FileMode.TREE.getBits());
				ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
				AbstractLink link = new ViewStateAwarePageLink<Void>("branchLink", 
						ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject(), state));
				link.add(new Label("name", branch));
				item.add(link);
				
				RevCommit lastCommit = getProject().getRevCommit(ref.getRef().getObjectId());
				
				String lastCommitHash = lastCommit.name();
				item.add(new BuildsStatusPanel("buildStatus", new LoadableDetachableModel<List<Build>>() {

					@Override
					protected List<Build> load() {
						return OneDev.getInstance(BuildManager.class).findAll(getProject(), lastCommitHash);
					}
					
				}));
				
				item.add(new AjaxLink<Void>("makeDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getProject().setDefaultBranch(branch);
						target.add(branchesContainer);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canAdministrate(getProject().getFacade()) 
								&& !branch.equals(getProject().getDefaultBranch()));
					}
					
				});
				
				item.add(new WebMarkupContainer("default") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getProject().getDefaultBranch().equals(branch));
					}
					
				});
				
				item.add(new ContributorPanel("contributor", lastCommit.getAuthorIdent(), 
						lastCommit.getCommitterIdent(), true));
				
				PageParameters params = CommitDetailPage.paramsOf(getProject(), lastCommit.name());
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
						
						tag.put("title", "" + ab.getBehind() + " commits ahead of base branch");
						if (ab.getBehind() == 0)
							tag.setName("span");
					}

					@Override
					public void onClick() {
						RevisionComparePage.State state = new RevisionComparePage.State();
						state.leftSide = new ProjectAndBranch(getProject(), branch);
						state.rightSide = new ProjectAndBranch(getProject(), baseBranch);
						PageParameters params = RevisionComparePage.paramsOf(getProject(), state); 
						setResponsePage(RevisionComparePage.class, params);
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
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("title", "" + ab.getAhead() + " commits ahead of base branch");
						if (ab.getAhead() == 0)
							tag.setName("span");
					}

					@Override
					public void onClick() {
						RevisionComparePage.State state = new RevisionComparePage.State();
						state.leftSide = new ProjectAndBranch(getProject(), baseBranch);
						state.rightSide = new ProjectAndBranch(getProject(), branch);
						PageParameters params = RevisionComparePage.paramsOf(getProject(), state);
						setResponsePage(RevisionComparePage.class, params);
					}
					
				});
				item.add(new Label("aheadBar") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						tag.put("style", "width: " + aheadBehindWidthModel.getObject().get(ab.getAhead()));
					}
					
				});
				
				WebMarkupContainer actionsContainer = new WebMarkupContainer("actions");
				item.add(actionsContainer.setOutputMarkupId(true));

				PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
				PullRequest effectiveRequest = pullRequestManager.findEffective(
						new ProjectAndBranch(getProject(), baseBranch), 
						new ProjectAndBranch(getProject(), branch));
				WebMarkupContainer requestLink;
				if (effectiveRequest != null && ab.getAhead() != 0) {
					requestLink = new BookmarkablePageLink<Void>("effectiveRequest", 
							RequestActivitiesPage.class, RequestActivitiesPage.paramsOf(effectiveRequest, null)); 
					if (effectiveRequest.isOpen()) {
						requestLink.add(new Label("label", "Open"));
						requestLink.add(AttributeAppender.append("title", "A pull request is open for this change"));
					} else {
						requestLink.add(new Label("label", "Merged"));
						requestLink.add(AttributeAppender.append("title", 
								"This change is squashed/rebased onto base branch via a pull request"));
					}
				} else {
					requestLink = new WebMarkupContainer("effectiveRequest");
					requestLink.setVisible(false);
					requestLink.add(new WebMarkupContainer("label"));
				}
				actionsContainer.add(requestLink);
				
				actionsContainer.add(new ModalLink("delete") {

					@Override
					protected void disableLink(ComponentTag tag) {
						super.disableLink(tag);
						tag.append("class", "disabled", " ");
						if (getProject().getDefaultBranch().equals(branch)) {
							tag.put("title", "Can not delete default branch");
						} else {
							tag.put("title", "Deletion not allowed due to branch protection rule");
						}
					}

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						Fragment fragment = new Fragment(id, "confirmDeleteBranchFrag", ProjectBranchesPage.this);
						PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
						if (!pullRequestManager.findAllOpen(new ProjectAndBranch(getProject(), branch)).isEmpty()) {
							Fragment bodyFrag = new Fragment("body", "openRequestsFrag", ProjectBranchesPage.this);
							String query = String.format("\"%s\" %s \"%s\" %s %s", 
									PullRequestConstants.FIELD_TARGET_BRANCH, PullRequestQuery.getRuleName(PullRequestQueryLexer.Is), 
									branch, PullRequestQuery.getRuleName(PullRequestQueryLexer.And), 
									PullRequestQuery.getRuleName(PullRequestQueryLexer.Open));
							PageParameters params = RequestListPage.paramsOf(getProject(), query);
							bodyFrag.add(new ViewStateAwarePageLink<Void>("openRequests", RequestListPage.class, params));
							bodyFrag.add(new Label("branch", branch));
							fragment.add(bodyFrag);
						} else {
							fragment.add(new Label("body", "You selected to delete branch " + branch));
						}
						fragment.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								getProject().deleteBranch(branch);
								if (branch.equals(baseBranch)) {
									baseBranch = getProject().getDefaultBranch();
									target.add(baseChoice);
								}
								target.add(pagingNavigator);
								target.add(branchesContainer);
								target.add(noBranchesContainer);
								modal.close();
							}
							
						});
						fragment.add(new AjaxLink<Void>("cancel") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								modal.close();
							}
							
						});
						fragment.add(new AjaxLink<Void>("close") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								modal.close();
							}
							
						});
						return fragment;
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						Project project = getProject();
						if (SecurityUtils.canWriteCode(project.getFacade())) {
							if (project.getDefaultBranch().equals(branch)) {
								setEnabled(false);
							} else {
								BranchProtection protection = project.getBranchProtection(branch, getLoginUser());
								setEnabled(protection == null || !protection.isNoDeletion());
							}
						} else {
							setVisible(false);
						}
						
					}

				});
								
			}
			
		});

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), baseBranch);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		branchesView.setCurrentPage(pagingHistorySupport.getCurrentPage());
		
		add(pagingNavigator = new HistoryAwarePagingNavigator("branchesPageNav", branchesView, pagingHistorySupport) {

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
		response.render(CssHeaderItem.forReference(new ProjectBranchesResourceReference()));
	}

	@Override
	public void onDetach() {
		branchesModel.detach();
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
		PageParameters params = paramsOf(getProject(), baseBranch);
		CharSequence url = RequestCycle.get().urlFor(ProjectBranchesPage.class, params);
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
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}

}
