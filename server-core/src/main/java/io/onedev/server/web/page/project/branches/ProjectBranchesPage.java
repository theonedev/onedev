package io.onedev.server.web.page.project.branches;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.pullrequest.OpenCriteria;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.branch.choice.BranchSingleChoice;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.link.ArchiveMenuLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public class ProjectBranchesPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_BASE = "base";
	
	private static final String PARAM_QUERY = "query";
	
	// use a small page size to load page quickly
	private static final int PAGE_SIZE = 10;
	
	private String baseBranch;
	
	private IModel<Map<String, RefInfo>> branchesModel = new LoadableDetachableModel<Map<String, RefInfo>>() {

		@Override
		protected Map<String, RefInfo> load() {
			Map<String, RefInfo> refInfos = new LinkedHashMap<>();
			for (RefInfo refInfo: getProject().getBranchRefInfos()) {
				String branch = GitUtils.ref2branch(refInfo.getRef().getName());
				if (query == null || branch.toLowerCase().contains(query.trim().toLowerCase()))
					refInfos.put(branch, refInfo);
			}
			return refInfos;
		}
		
	};
	
	private IModel<Map<ProjectAndBranch, PullRequest>> effectiveRequestsModel = 
			new LoadableDetachableModel<Map<ProjectAndBranch, PullRequest>>() {

		@Override
		protected Map<ProjectAndBranch, PullRequest> load() {
			ProjectAndBranch target = new ProjectAndBranch(getProject(), baseBranch);
			
			Collection<ProjectAndBranch> sources = new ArrayList<>();
			
			List<RefInfo> branches = new ArrayList<>(branchesModel.getObject().values());
			long firstItemOffset = branchesTable.getCurrentPage() * branchesTable.getItemsPerPage();
			for (long i=firstItemOffset; i<branches.size(); i++) {
				if (i-firstItemOffset >= branchesTable.getItemsPerPage())
					break;
				RefInfo refInfo = branches.get((int)i);
				String branchName = GitUtils.ref2branch(refInfo.getRef().getName());
				sources.add(new ProjectAndBranch(getProject(), branchName)); 
			}
			
			return OneDev.getInstance(PullRequestManager.class).findEffectives(target, sources);
		}
		
	};
	
	private BranchSingleChoice baseChoice;

	private TextField<String> searchField;
	
	private DataTable<RefInfo, Void> branchesTable;
	
	private String query;
	
	private boolean typing;
	
	private final IModel<Map<ObjectId, AheadBehind>> aheadBehindsModel = 
			new LoadableDetachableModel<Map<ObjectId, AheadBehind>>() {

		@SuppressWarnings("unused")
		@Override
		protected Map<ObjectId, AheadBehind> load() {
			List<ObjectId> compareIds = getCommitIdsToDisplay();

			Ref baseRef = Preconditions.checkNotNull(getProject().getBranchRef(baseBranch));
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
	
	private final PagingHistorySupport pagingHistorySupport;
	
	public static PageParameters paramsOf(Project project, @Nullable String baseBranch, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (baseBranch != null)
			params.add(PARAM_BASE, baseBranch);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
	public ProjectBranchesPage(PageParameters params) {
		super(params);
		
		baseBranch = params.get(PARAM_BASE).toString();
		if (baseBranch == null)
			baseBranch = getProject().getDefaultBranch();
		query = params.get(PARAM_QUERY).toString();
		
		pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), baseBranch, query);
				params.add(PARAM_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(searchField);
		target.add(branchesTable);
	}
	
	@Override
	protected void onBeforeRender() {
		typing = false;
		super.onBeforeRender();
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
			
		}, new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> branches = new LinkedHashMap<>();
				for (RefInfo ref: getProject().getBranchRefInfos()) {
					String branch = GitUtils.ref2branch(ref.getRef().getName());
					branches.put(branch, branch);
				}
				return branches;
			}
			
		}));
		
		baseChoice.setRequired(true);
		
		baseChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(ProjectBranchesPage.class, paramsOf(getProject(), baseBranch, query));
			}
			
		});
		
		add(searchField = new TextField<String>("filterBranches", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				
				String url = RequestCycle.get().urlFor(ProjectBranchesPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				branchesTable.setCurrentPage(0);
				target.add(branchesTable);
				
				typing = true;
			}
			
		}));
		searchField.add(new OnSearchingBehavior());
		
		add(new ModalLink("createBranch") {

			private BranchBeanWithRevision helperBean = new BranchBeanWithRevision();
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canCreateBranch(getProject(), Constants.R_HEADS));
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "createBranchFrag", ProjectBranchesPage.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				form.add(new FencedFeedbackPanel("feedback", form));

				helperBean.setName(null);
				helperBean.setRevision(null);
				
				BeanEditor editor;
				form.add(editor = BeanContext.edit("editor", helperBean));
				
				form.add(new AjaxButton("create") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						
						String branchName = helperBean.getName();
						User user = Preconditions.checkNotNull(getLoginUser());
						if (getProject().getObjectId(GitUtils.branch2ref(branchName), false) != null) {
							editor.error(new Path(new PathNode.Named("name")), 
									"Branch '" + branchName + "' already exists, please choose a different name");
							target.add(form);
						} else if (getProject().getBranchProtection(branchName, user).isPreventCreation()) {
							editor.error(new Path(new PathNode.Named("name")), "Unable to create protected branch");
							target.add(form);
						} else {
							getProject().createBranch(branchName, helperBean.getRevision());
							modal.close();
							target.add(branchesTable);
							
							getSession().success("Branch '" + branchName + "' created");
						}
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				fragment.add(form);
				return fragment;
			}
			
		});		

		List<IColumn<RefInfo, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<RefInfo, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<RefInfo>> cellItem, String componentId,
					IModel<RefInfo> rowModel) {
				Fragment fragment = new Fragment(componentId, "branchFrag", ProjectBranchesPage.this);
				fragment.setRenderBodyOnly(true);
				RefInfo ref = rowModel.getObject();
				String branch = GitUtils.ref2branch(ref.getRef().getName());
				
				BlobIdent blobIdent = new BlobIdent(branch, null, FileMode.TREE.getBits());
				ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
				AbstractLink link = new ViewStateAwarePageLink<Void>("branchLink", 
						ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject(), state));
				link.add(new Label("name", branch));
				fragment.add(link);
				
				fragment.add(new CommitStatusPanel("buildStatus", ref.getRef().getObjectId(), ref.getRef().getName()) {

					@Override
					protected Project getProject() {
						return ProjectBranchesPage.this.getProject();
					}

					@Override
					protected PullRequest getPullRequest() {
						return null;
					}
					
				});
				
				fragment.add(new WebMarkupContainer("default") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getProject().getDefaultBranch().equals(branch));
					}
					
				});
				
				RevCommit lastCommit = getProject().getRevCommit(ref.getRef().getObjectId(), true);
				fragment.add(new ContributorPanel("contributor", lastCommit.getAuthorIdent(), lastCommit.getCommitterIdent()));
				
				fragment.add(new Label("message", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						RevCommit lastCommit = getProject().getRevCommit(rowModel.getObject().getRef().getObjectId(), true);
						PageParameters params = CommitDetailPage.paramsOf(getProject(), lastCommit.name()); 
						String commitUrl = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
						ReferenceTransformer transformer = new ReferenceTransformer(getProject(), commitUrl);
						return transformer.apply(lastCommit.getShortMessage());
					}
					
				}).setEscapeModelStrings(false));
								
				WebMarkupContainer actionsContainer = new WebMarkupContainer("actions");
				fragment.add(actionsContainer.setOutputMarkupId(true));

				ProjectAndBranch source = new ProjectAndBranch(getProject(), branch);
				PullRequest effectiveRequest = effectiveRequestsModel.getObject().get(source);
				WebMarkupContainer requestLink;
				AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(lastCommit));
				if (effectiveRequest != null && ab.getAhead() != 0) {
					requestLink = new BookmarkablePageLink<Void>("effectiveRequest", 
							PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(effectiveRequest)); 
					if (effectiveRequest.isOpen()) {
						requestLink.add(new Label("label", "Open"));
						requestLink.add(AttributeAppender.append("class", "btn-warning"));
						requestLink.add(AttributeAppender.append("title", "A pull request is open for this change"));
					} else {
						requestLink.add(new Label("label", "Merged"));
						requestLink.add(AttributeAppender.append("class", "btn-success"));
						requestLink.add(AttributeAppender.append("title", 
								"This change is squashed/rebased onto base branch via a pull request"));
					}
				} else {
					requestLink = new WebMarkupContainer("effectiveRequest");
					requestLink.setVisible(false);
					requestLink.add(new WebMarkupContainer("label"));
				}
				actionsContainer.add(requestLink);
				
				actionsContainer.add(new ArchiveMenuLink("download", projectModel) {

					@Override
					protected String getRevision() {
						return branch;
					}
					
				});
				
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
						if (!pullRequestManager.queryOpen(new ProjectAndBranch(getProject(), branch)).isEmpty()) {
							Fragment bodyFrag = new Fragment("body", "openRequestsFrag", ProjectBranchesPage.this);
							String query = String.format("\"%s\" %s \"%s\" %s %s", 
									PullRequest.NAME_TARGET_BRANCH, PullRequestQuery.getRuleName(PullRequestQueryLexer.Is), 
									branch, PullRequestQuery.getRuleName(PullRequestQueryLexer.And), 
									new OpenCriteria().toString());
							PageParameters params = ProjectPullRequestsPage.paramsOf(getProject(), query, 0);
							bodyFrag.add(new ViewStateAwarePageLink<Void>("openRequests", ProjectPullRequestsPage.class, params));
							bodyFrag.add(new Label("branch", branch));
							fragment.add(bodyFrag);
						} else {
							fragment.add(new Label("body", "You selected to delete branch " + branch));
						}
						fragment.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								OneDev.getInstance(ProjectManager.class).deleteBranch(getProject(), branch);
								getSession().success("Branch '" + branch + "' deleted");
								if (branch.equals(baseBranch)) {
									baseBranch = getProject().getDefaultBranch();
									target.add(baseChoice);
								}
								target.add(branchesTable);
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
						fragment.setOutputMarkupId(true);
						return fragment;
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();

						Project project = getProject();
						if (SecurityUtils.canWriteCode(project)) {
							if (project.getDefaultBranch().equals(branch)) 
								setEnabled(false);
							else 
								setEnabled(!project.getBranchProtection(branch, getLoginUser()).isPreventDeletion());
						} else {
							setVisible(false);
						}
						
					}

				});
				
				actionsContainer.add(new AjaxLink<Void>("makeDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getProject().setDefaultBranch(branch);
						target.add(branchesTable);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(getProject()) 
								&& !branch.equals(getProject().getDefaultBranch()));
					}
					
				});

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "branch";
			}
			
		});
		columns.add(new AbstractColumn<RefInfo, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<RefInfo>> cellItem, String componentId,
					IModel<RefInfo> rowModel) {
				RefInfo ref = rowModel.getObject();
				RevCommit lastCommit = getProject().getRevCommit(ref.getRef().getObjectId(), true);
				AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(lastCommit));
				cellItem.add(newAheadBehindFrag(componentId, ref, ab.getBehind(), false));
			}

			@Override
			public String getCssClass() {
				return "behind behind-ahead d-none d-lg-table-cell";
			}
			
		});
		columns.add(new AbstractColumn<RefInfo, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<RefInfo>> cellItem, String componentId,
					IModel<RefInfo> rowModel) {
				RefInfo ref = rowModel.getObject();
				RevCommit lastCommit = getProject().getRevCommit(ref.getRef().getObjectId(), true);
				AheadBehind ab = Preconditions.checkNotNull(aheadBehindsModel.getObject().get(lastCommit));
				cellItem.add(newAheadBehindFrag(componentId, ref, ab.getAhead(), true));
			}

			@Override
			public String getCssClass() {
				return "ahead behind-ahead d-none d-lg-table-cell";
			}
			
		});
		
		SortableDataProvider<RefInfo, Void> dataProvider = new LoadableDetachableDataProvider<RefInfo, Void>() {

			@Override
			public Iterator<? extends RefInfo> iterator(long first, long count) {
				List<RefInfo> branches = new ArrayList<>(branchesModel.getObject().values());
				if (first + count > branches.size())
					return branches.subList((int)first, branches.size()).iterator();
				else
					return branches.subList((int)first, (int)(first+count)).iterator();
			}

			@Override
			public long calcSize() {
				return branchesModel.getObject().size();
			}

			@Override
			public IModel<RefInfo> model(RefInfo object) {
				String branch = GitUtils.ref2branch(object.getRef().getName());
				return new AbstractReadOnlyModel<RefInfo>() {

					@Override
					public RefInfo getObject() {
						return branchesModel.getObject().get(branch);
					}
					
				};
			}
		};		
		
		add(branchesTable = new OneDataTable<RefInfo, Void>("branches", columns, dataProvider, 
				PAGE_SIZE, pagingHistorySupport) {
			
			@Override
			protected void onBeforeRender() {
				BuildManager buildManager = OneDev.getInstance(BuildManager.class);
				getProject().cacheCommitStatus(buildManager.queryStatus(getProject(), getCommitIdsToDisplay()));
				super.onBeforeRender();
			}
			
		});
		
	}
	
	private Fragment newAheadBehindFrag(String componentId, RefInfo ref, int count, boolean ahead) {
		Fragment fragment = new Fragment(componentId, "aheadBehindFrag", ProjectBranchesPage.this);
		String branch = GitUtils.ref2branch(ref.getRef().getName());
		fragment.add(new Link<Void>("link") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(count != 0);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("count", count));
				add(new Label("label", ahead?"ahead":"behind"));
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (ahead)
					tag.put("title", "" + count + " commits ahead of base branch");
				else
					tag.put("title", "" + count + " commits behind of base branch");
					
				if (count == 0)
					tag.setName("span");
			}

			@Override
			public void onClick() {
				RevisionComparePage.State state = new RevisionComparePage.State();
				if (ahead) {
					state.leftSide = new ProjectAndBranch(getProject(), baseBranch);
					state.rightSide = new ProjectAndBranch(getProject(), branch);
				} else {
					state.leftSide = new ProjectAndBranch(getProject(), branch);
					state.rightSide = new ProjectAndBranch(getProject(), baseBranch);
				}
				PageParameters params = RevisionComparePage.paramsOf(getProject(), state);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		fragment.add(new Label("bar") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				tag.put("style", "width: " + aheadBehindWidthModel.getObject().get(count));
			}
			
		});	
		return fragment;
	}
	
	private List<ObjectId> getCommitIdsToDisplay() {
		List<RefInfo> branches = new ArrayList<>(branchesModel.getObject().values());
		long firstItemOffset = branchesTable.getCurrentPage() * branchesTable.getItemsPerPage();
		List<ObjectId> commitIdsToDisplay = new ArrayList<>();
		for (long i=firstItemOffset; i<branches.size(); i++) {
			if (i-firstItemOffset >= branchesTable.getItemsPerPage())
				break;
			RefInfo ref = branches.get((int)i); 
			commitIdsToDisplay.add(ref.getRef().getObjectId());
		}
		return commitIdsToDisplay;
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
		effectiveRequestsModel.detach();
		
		super.onDetach();
	}
	
	private class OnSearchingBehavior extends OnTypingDoneBehavior implements IAjaxIndicatorAware {

		public OnSearchingBehavior() {
			super(500);
		}

		@Override
		protected void onTypingDone(AjaxRequestTarget target) {
		}

		@Override
		public String getAjaxIndicatorMarkupId() {
			return "searching-branches";
		}
		
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Branches");
	}

	@Override
	protected String getPageTitle() {
		return "Branches - " + getProject().getName();
	}
	
}
