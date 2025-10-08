package io.onedev.server.web.page.project.commits;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.codequality.LineCoverageContribution;
import io.onedev.server.service.*;
import io.onedev.server.entityreference.LinkTransformer;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.job.JobAuthorizationContextAware;
import io.onedev.server.model.*;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.component.AjaxLazyLoadPanel;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.branch.create.CreateBranchPanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.createtag.CreateTagPanel;
import io.onedev.server.web.component.diff.revision.RevisionAnnotationSupport;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.gitsignature.SignatureStatusPanel;
import io.onedev.server.web.component.job.jobinfo.JobInfoButton;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.editbean.CommitMessageBean;
import io.onedev.server.xodus.CommitInfoService;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import static io.onedev.server.entityreference.ReferenceUtils.transformReferences;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

public class CommitDetailPage extends ProjectPage implements RevisionAnnotationSupport, JobAuthorizationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(CommitDetailPage.class);

	public static final String PARAM_COMMIT = "commit";
	
	// make sure to use a different value from wicket:id according to wicket bug:
	// https://issues.apache.org/jira/browse/WICKET-6069
	private static final String PARAM_COMPARE_WITH = "compare-with";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_PULL_REQUEST = "request";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private State state;
	
	private ObjectId resolvedRevision;
	
	private ObjectId resolvedCompareWith;

	private List<String> operateBranches;

	private final IModel<List<RefFacade>> refsModel = new LoadableDetachableModel<List<RefFacade>>() {
		@Override
		protected List<RefFacade> load() {
			Collection<ObjectId> descendants = getDescendants();
			List<RefFacade> refs = new ArrayList<>();
			refs.addAll(getProject().getBranchRefs());
			refs.addAll(getProject().getTagRefs());
			return refs.stream().filter(ref -> descendants.contains(ref.getPeeledObj())).collect(toList());
		}
	};

	private final IModel<Collection<CodeComment>> commentsModel =
			new LoadableDetachableModel<>() {

				@Override
				protected Collection<CodeComment> load() {
					CodeCommentService manager = OneDev.getInstance(CodeCommentService.class);
					return manager.query(projectModel.getObject(), getCompareWith(), resolvedRevision);
				}

			};

	private WebMarkupContainer refsContainer;
	
	private WebMarkupContainer revisionDiff;
	
	public CommitDetailPage(PageParameters params) {
		super(params);

		List<String> revisionSegments = new ArrayList<>();
		String segment = params.get(PARAM_COMMIT).toString();
		if (segment.contains(".."))
			throw new ExplicitException(_T("Invalid request path"));
		if (segment.length() != 0)
			revisionSegments.add(segment);
		for (int i=0; i<params.getIndexedCount(); i++) {
			segment = params.get(i).toString();
			if (segment.contains(".."))
				throw new ExplicitException(_T("Invalid request path"));
			if (segment.length() != 0)
				revisionSegments.add(segment);
		}

		if (revisionSegments.isEmpty())
			throw new RestartResponseException(ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(getProject()));
		
		state = new State();
		state.revision = Joiner.on("/").join(revisionSegments);
		
		state.compareWith = params.get(PARAM_COMPARE_WITH).toString();
		state.whitespaceOption = WhitespaceOption.ofName(
				params.get(PARAM_WHITESPACE_OPTION).toString(WhitespaceOption.IGNORE_TRAILING.name()));
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = Mark.fromString(params.get(PARAM_MARK).toString());
		state.requestId = params.get(PARAM_PULL_REQUEST).toOptionalLong();
		
		resolvedRevision = getProject().getRevCommit(state.revision, true).copy();
		if (state.compareWith != null)
			resolvedCompareWith = getProject().getRevCommit(state.compareWith, true).copy();
	}

	private RevCommit getCommit() {
		return getProject().getRevCommit(state.revision, true);
	}
	
	private List<RevCommit> getParents() {
		List<RevCommit> parents = new ArrayList<>();
		for (RevCommit parent: getCommit().getParents())
			parents.add(parent);
		return parents;
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Emojis emojis = Emojis.getInstance();
		var transformed = transformReferences(
				getCommit().getShortMessage(), getProject(), new LinkTransformer(null));
		add(new Label("title", emojis.apply(transformed)).setEscapeModelStrings(false));

		BlobIdent blobIdent = new BlobIdent(getCommit().name(), null, FileMode.TYPE_TREE);
		ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
		browseState.requestId = state.requestId;
		PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), browseState);
		add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
		
		add(new MenuLink("commitOperations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				var menuItems = new ArrayList<MenuItem>();
				if (SecurityUtils.canCreateBranch(getProject(), Constants.R_HEADS)) {
					menuItems.add(new MenuItem() {
						@Override
						public String getLabel() {
							return _T("Create Branch");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new ModalLink(id) {
								@Override
								protected Component newContent(String id, ModalPanel modal) {
									dropdown.close();
									return new CreateBranchPanel(id, projectModel, state.revision) {

										@Override
										protected void onCreate(AjaxRequestTarget target, String branch) {
											modal.close();
											target.add(refsContainer);
										}

										@Override
										protected void onCancel(AjaxRequestTarget target) {
											modal.close();
										}

									};
								}
							};
						}
					});
				}
				if (SecurityUtils.canCreateTag(getProject(), Constants.R_TAGS)) {
					menuItems.add(new MenuItem() {
						@Override
						public String getLabel() {
							return _T("Create Tag");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new ModalLink(id) {
								@Override
								protected Component newContent(String id, ModalPanel modal) {
									dropdown.close();
									return new CreateTagPanel(id, projectModel, null, state.revision) {

										@Override
										protected void onCreate(AjaxRequestTarget target, String tag) {
											modal.close();
											target.add(refsContainer);
										}

										@Override
										protected void onCancel(AjaxRequestTarget target) {
											modal.close();
										}

									};
								}

							};
						}
					});
				}

				if (getCommit().getParentCount() == 1) {
					menuItems.add(new MenuItem() {
						@Override
						public String getLabel() {
							return _T("Cherry-Pick");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									operateBranches = getProject().getBranchRefs().stream()
											.filter(it -> !getDescendants().contains(it.getPeeledObj()))
											.map(it -> GitUtils.ref2branch(it.getName()))
											.collect(toList());
									if (operateBranches.isEmpty()) {
										Session.get().error(_T("No branch to cherry-pick to"));
									} else if (operateBranches.size() == 1) {
										var branch = operateBranches.get(0);
										cherryPickOrRevert(target, branch, true);
									} else {
										new BeanEditModalPanel<>(target, new BranchChoiceBean(), _T("Select Branch to Cherry Pick to")) {

											@Override
											protected String onSave(AjaxRequestTarget target, BranchChoiceBean bean) {
												close();
												cherryPickOrRevert(target, bean.getBranch(), true);
												return null;
											}
										};
									}
								}
							};
						}
					});
					menuItems.add(new MenuItem() {
						@Override
						public String getLabel() {
							return _T("Revert");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									operateBranches = getProject().getBranchRefs().stream()
											.filter(it -> getDescendants().contains(it.getPeeledObj()))
											.map(it -> GitUtils.ref2branch(it.getName()))
											.collect(toList());
									if (operateBranches.isEmpty()) {
										Session.get().error(_T("No branch to revert on"));
									} else if (operateBranches.size() == 1) {
										var branch = operateBranches.get(0);
										cherryPickOrRevert(target, branch, false);
									} else {
										new BeanEditModalPanel<>(target, new BranchChoiceBean(), _T("Select Branch to Revert on")) {

											@Override
											protected String onSave(AjaxRequestTarget target, BranchChoiceBean bean) {
												close();
												cherryPickOrRevert(target, bean.getBranch(), false);
												return null;
											}
										};
									}
								}
							};
						}
					});
				}
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canWriteCode(getProject()));
			}

		});

		String message = GitUtils.getDetailMessage(getCommit());
		if (message != null) {
			transformed = transformReferences(
					message, getProject(), new LinkTransformer(null));
			add(new Label("detail", emojis.apply(transformed)).setEscapeModelStrings(false));
		} else { 
			add(new WebMarkupContainer("detail").setVisible(false));
		}

		add(refsContainer = new AjaxLazyLoadPanel("refs") {

			@Override
			public Component getLazyLoadComponent(String markupId) {
				Fragment fragment = new Fragment(markupId, "refsFrag", CommitDetailPage.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format("onedev.server.commitDetail.initRefs('%s');", getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				fragment.add(new ListView<>("refs", refsModel) {

					@Override
					protected void populateItem(ListItem<RefFacade> item) {
						String refName = item.getModelObject().getName();
						String branch = GitUtils.ref2branch(refName);
						if (branch != null) {
							BlobIdent blobIdent = new BlobIdent(branch, null, FileMode.TREE.getBits());
							ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
							Link<Void> link = new ViewStateAwarePageLink<Void>("link", ProjectBlobPage.class,
									ProjectBlobPage.paramsOf(projectModel.getObject(), state));
							link.add(new SpriteImage("icon", "branch"));
							link.add(new Label("label", branch));
							item.add(link);
							item.add(AttributeAppender.append("class", "branch ref"));
						} else {
							String tag = Preconditions.checkNotNull(GitUtils.ref2tag(refName));
							BlobIdent blobIdent = new BlobIdent(tag, null, FileMode.TREE.getBits());
							ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
							Link<Void> link = new ViewStateAwarePageLink<Void>("link", ProjectBlobPage.class,
									ProjectBlobPage.paramsOf(projectModel.getObject(), state));
							link.add(new SpriteImage("icon", "tag"));
							link.add(new Label("label", tag));
							item.add(link);
							item.add(AttributeAppender.append("class", "tag ref"));
						}
					}

				});
				return fragment;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!refsModel.getObject().isEmpty());
			}

		});
		refsContainer.setOutputMarkupPlaceholderTag(true);
		
		add(new ContributorAvatars("contributorAvatars", getCommit().getAuthorIdent(), getCommit().getCommitterIdent()));
		add(new ContributorPanel("contribution", getCommit().getAuthorIdent(), getCommit().getCommitterIdent()));
		
		add(new SignatureStatusPanel("signature") {
			
			@Override
			protected RevObject getRevObject() {
				return getCommit();
			}
			
		});

		add(new Label("hash", GitUtils.abbreviateSHA(getCommit().name())));
		add(new CopyToClipboardLink("copyHash", Model.of(getCommit().name())));
		
		newParentsContainer(null);

		add(new ListView<Job>("jobs", new LoadableDetachableModel<>() {

			@Override
			protected List<Job> load() {
				List<Job> jobs = new ArrayList<>();
				try {
					BuildSpec buildSpec = getProject().getBuildSpec(getCommit().copy());
					if (buildSpec != null) {
						for (Job job : buildSpec.getJobMap().values()) {
							if (SecurityUtils.canAccessJob(getProject(), job.getName()))
								jobs.add(job);
						}
					}
				} catch (Exception e) {
					logger.error("Error retrieving build spec (project: {}, commit: {})",
							getProject().getPath(), getCommit().name(), e);
				}
				return jobs;
			}

		}) {

			@Override
			protected void populateItem(ListItem<Job> item) {
				ObjectId commitId = getCommit().copy();
				Job job = item.getModelObject();
				
				item.add(new JobInfoButton("jobInfo") {

					@Override
					protected Project getProject() {
						return CommitDetailPage.this.getProject();
					}

					@Override
					protected ObjectId getCommitId() {
						return commitId;
					}

					@Override
					protected String getJobName() {
						return job.getName();
					}

				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		newRevisionDiff(null);
	}
	
	private void newParentsContainer(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer parents;
		if (getParents().size() == 0) {
			parents = new WebMarkupContainer("parents");
		} else if (getParents().size() == 1) {
			parents = new Fragment("parents", "singleParentFrag", this);
			RevCommit parent = getParents().get(0);
			State newState = new State();
			newState.revision = parent.name();
			newState.whitespaceOption = state.whitespaceOption;
			newState.pathFilter = state.pathFilter;
			Link<Void> link = new ViewStateAwarePageLink<Void>("parent", CommitDetailPage.class, 
					paramsOf(projectModel.getObject(), newState));
			link.add(new Label("label", GitUtils.abbreviateSHA(parent.name())));
			parents.add(link);
			parents.add(new WebMarkupContainer("parents").setVisible(false));
		} else {
			parents = new Fragment("parents", "multiParentsFrag", this);
			parents.add(new WebMarkupContainer("parent").setVisible(false));
			parents.add(new Label("count", getParents().size() + " parents"));
			parents.add(new ListView<>("parents", new LoadableDetachableModel<List<RevCommit>>() {

				@Override
				protected List<RevCommit> load() {
					return getParents();
				}

			}) {

				@Override
				protected void populateItem(ListItem<RevCommit> item) {
					RevCommit parent = item.getModelObject();

					State newState = new State();
					newState.revision = parent.name();
					newState.whitespaceOption = state.whitespaceOption;
					newState.pathFilter = state.pathFilter;

					Link<Void> link = new ViewStateAwarePageLink<Void>("link", CommitDetailPage.class,
							paramsOf(projectModel.getObject(), newState));
					link.add(new Label("label", GitUtils.abbreviateSHA(parent.name())));
					item.add(link);

					item.add(new AjaxLink<Void>("diff") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							state.compareWith = item.getModelObject().name();
							resolvedCompareWith = item.getModelObject().copy();

							target.add(parents);
							newRevisionDiff(target);
							pushState(target);
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							if (item.getModelObject().equals(getCompareWith()))
								add(AttributeAppender.append("class", "active"));
						}

					});
				}

			});
		}		
		parents.setOutputMarkupId(true);
		if (target != null) {
			replace(parents);
			target.add(parents);
		} else {
			add(parents);
		}
	}
	
	private ObjectId getCompareWith() {
		List<RevCommit> parents = getParents();
		if (parents.size() == 0) {
			return ObjectId.zeroId();
		} else if (resolvedCompareWith != null) {
			if (parents.contains(resolvedCompareWith)) 
				return resolvedCompareWith;
			else
				return parents.get(0);
		} else {
			return parents.get(0);
		}
	}
	
	private void newRevisionDiff(@Nullable AjaxRequestTarget target) {
		IModel<String> blameModel = new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.blameFile;
			}

			@Override
			public void setObject(String object) {
				state.blameFile = object;
				pushState(RequestCycle.get().find(AjaxRequestTarget.class));
			}

		};
		IModel<String> pathFilterModel = new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.pathFilter;
			}

			@Override
			public void setObject(String object) {
				state.pathFilter = object;
				pushState(RequestCycle.get().find(AjaxRequestTarget.class));
			}

		};
		IModel<WhitespaceOption> whitespaceOptionModel = new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public WhitespaceOption getObject() {
				return state.whitespaceOption;
			}

			@Override
			public void setObject(WhitespaceOption object) {
				state.whitespaceOption = object;
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				newParentsContainer(target);
				pushState(target);
			}

		};
		revisionDiff = new RevisionDiffPanel("revisionDiff", getCompareWith().name(), 
				state.revision, pathFilterModel, whitespaceOptionModel, 
				blameModel, this) {
			
			@Override
			protected Project getProject() {
				return projectModel.getObject();
			}

			@Override
			protected PullRequest getPullRequest() {
				if (state.requestId != null)
					return OneDev.getInstance(PullRequestService.class).load(state.requestId);
				else
					return null;
			}

			@Override
			protected boolean isContextDifferent(CompareContext compareContext) {
				return !compareContext.getOldCommitHash().equals(getCompareWith().name()) 
						|| !compareContext.getNewCommitHash().equals(resolvedRevision.name());
			}
			
		};
		revisionDiff.setOutputMarkupId(true);
		if (target != null) {
			replace(revisionDiff);
			target.add(revisionDiff);
		} else {
			add(revisionDiff);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CommitDetailResourceReference()));
	}

	public static State getState(CodeComment comment, CompareContext compareContext) {
		State state = new State();
		state.commentId = comment.getId();
		state.mark = comment.getMark();
		state.revision = compareContext.getNewCommitHash();
		if (!compareContext.getOldCommitHash().equals(ObjectId.zeroId().name()))
			state.compareWith = compareContext.getOldCommitHash();
		state.whitespaceOption = compareContext.getWhitespaceOption();
		state.pathFilter = compareContext.getPathFilter();
		return state;
	}
	
	public static PageParameters paramsOf(Project project, State state) {
		PageParameters params = paramsOf(project);
		fillParams(params, state);
		return params;
	}
	
	public static void fillParams(PageParameters params, State state) {
		if (state.revision != null)
			params.set(PARAM_COMMIT, state.revision);
		if (state.compareWith != null)
			params.set(PARAM_COMPARE_WITH, state.compareWith);
		if (state.whitespaceOption != WhitespaceOption.IGNORE_TRAILING)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.blameFile != null)
			params.set(PARAM_BLAME_FILE, state.blameFile);
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		if (state.requestId != null)
			params.set(PARAM_PULL_REQUEST, state.requestId);
	}
	
	public static PageParameters paramsOf(Project project, String revision) {
		State state = new State();
		state.revision = revision;
		return paramsOf(project, state);
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getProject(), state);
		CharSequence url = RequestCycle.get().urlFor(CommitDetailPage.class, params);
		pushState(target, url.toString(), state);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		newRevisionDiff(target);
	}
	
	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public String revision;
		
		@Nullable
		public String compareWith;
		
		@Nullable
		public Long commentId;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.IGNORE_TRAILING;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public Mark mark;
		
		@Nullable
		public Long requestId;
	}

	@Override
	public Mark getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(Mark mark) {
		State markState = new State();
		markState.mark = mark;
		markState.whitespaceOption = state.whitespaceOption;
		markState.compareWith = state.compareWith;
		markState.pathFilter = state.pathFilter;
		markState.revision = resolvedRevision.name();
		return urlFor(CommitDetailPage.class, paramsOf(getProject(), markState)).toString();
	}

	private Collection<ObjectId> getDescendants() {
		Collection<ObjectId> descendants = OneDev.getInstance(CommitInfoService.class)
				.getDescendants(getProject().getId(), Sets.newHashSet(getCommit().getId()));
		descendants.add(getCommit().getId());
		return descendants;
	}

	@Override
	public CodeComment getOpenComment() {
		if (state.commentId != null)
			return OneDev.getInstance(CodeCommentService.class).load(state.commentId);
		else
			return null;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		state.commentId = comment.getId();
		state.mark = comment.getMark();
		pushState(target);
	}
	
	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		state.commentId = null;
		state.mark = null;
		pushState(target);
	}
	
	@Override
	public void onMark(AjaxRequestTarget target, Mark mark) {
		state.mark = mark;
		pushState(target);
	}

	@Override
	public void onUnmark(AjaxRequestTarget target) {
		state.mark = null;
		pushState(target);
	}
	
	@Override
	public void onAddComment(AjaxRequestTarget target, Mark mark) {
		state.commentId = null;
		state.mark = mark;
		pushState(target);
	}
	
	@Override
	protected void onDetach() {
		refsModel.detach();
		commentsModel.detach();
		super.onDetach();
	}

	@Override
	protected Map<String, ObjectId> getObjectIdCache() {
		Map<String, ObjectId> objectIdCache = new HashMap<>();
		objectIdCache.put(state.revision, resolvedRevision);
		if (state.compareWith != null)
			objectIdCache.put(state.compareWith, resolvedCompareWith);
		return objectIdCache;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	public void onSaveComment(CodeComment comment) {
		if (comment.isNew())
			OneDev.getInstance(CodeCommentService.class).create(comment);
		else
			OneDev.getInstance(CodeCommentService.class).update(comment);
	}
	
	@Override
	public void onSaveCommentReply(CodeCommentReply reply) {
		if (reply.isNew())
			OneDev.getInstance(CodeCommentReplyService.class).create(reply);
		else
			OneDev.getInstance(CodeCommentReplyService.class).update(reply);
	}

	@Override
	public void onSaveCommentStatusChange(CodeCommentStatusChange change, String note) {
		OneDev.getInstance(CodeCommentStatusChangeService.class).create(change, note);
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("commits", ProjectCommitsPage.class, 
				ProjectCommitsPage.paramsOf(getProject(), null)));
		fragment.add(new Label("commitHash", GitUtils.abbreviateSHA(getCommit().name())));
		return fragment;
	}

	@Override
	public Map<CodeComment, PlanarRange> getOldComments(String blobPath) {
		Map<CodeComment, PlanarRange> oldComments = new HashMap<>();
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getMark().getPath().equals(blobPath) 
					&& comment.getMark().getCommitHash().equals(getCompareWith().name())) {
				oldComments.put(comment, comment.getMark().getRange());
			}
		}
		return oldComments;
	}

	@Override
	public Map<CodeComment, PlanarRange> getNewComments(String blobPath) {
		Map<CodeComment, PlanarRange> newComments = new HashMap<>();
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getMark().getPath().equals(blobPath) 
					&& comment.getMark().getCommitHash().equals(getCommit().name())) {
				newComments.put(comment, comment.getMark().getRange());
			}
		}
		return newComments;
	}
	
	@Override
	public Collection<CodeProblem> getOldProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		for (Build build: getProject().getBuilds(getCompareWith())) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Collection<CodeProblem> getNewProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		for (Build build: getProject().getBuilds(getCommit())) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Map<Integer, CoverageStatus> getOldCoverages(String blobPath) {
		Map<Integer, CoverageStatus> coverages = new HashMap<>();
		for (Build build: getProject().getBuilds(getCompareWith())) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1.mergeWith(v2));
				});
			}
		}
		return coverages;
	}

	@Override
	public Map<Integer, CoverageStatus> getNewCoverages(String blobPath) {
		Map<Integer, CoverageStatus> coverages = new HashMap<>();
		for (Build build: getProject().getBuilds(getCommit())) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1.mergeWith(v2));
				});
			}
		}
		return coverages;
	}
	
	@Override
	protected String getPageTitle() {
		return getCommit().getShortMessage() 
				+ " - Commit " +  GitUtils.abbreviateSHA(getCommit().getName()) 
				+ " - " + getProject().getPath();
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement() && SecurityUtils.canReadCode(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(project.getId()));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
	@Nullable
	private PullRequest getPullRequest() {
		if (state.requestId != null)
			return OneDev.getInstance(PullRequestService.class).load(state.requestId);
		else
			return null;
	}

	@Override
	public JobAuthorizationContext getJobAuthorizationContext() {
		return new JobAuthorizationContext(getProject(), getCommit(), getPullRequest());
	}

	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}

	@Nullable
	private String checkUpdateBranch(BranchProtection protection, ObjectId oldCommitId, ObjectId newCommitId) {
		var project = getProject();
		if (protection.isReviewRequiredForPush(project, oldCommitId, newCommitId, new HashMap<>()))
			return _T("Review required for this change. Submit pull request instead");
		var buildRequirement = protection.getBuildRequirement(project, oldCommitId, newCommitId, new HashMap<>());
		if (!buildRequirement.getRequiredJobs().isEmpty())
			return _T("This change needs to be verified by some jobs. Submit pull request instead");
		if (protection.isCommitSignatureRequired()
				&& OneDev.getInstance(SettingService.class).getGpgSetting().getSigningKey() == null) {
			return _T("Commit signature required but no GPG signing key specified");
		}
		return null;
	}

	public void cherryPickOrRevert(AjaxRequestTarget target, String branch, boolean cherryPick) {
		var user = getLoginUser();
		var targetCommit = getProject().getRevCommit(GitUtils.branch2ref(branch), true);
		ObjectId resultCommitId;
		var bean = new CommitMessageBean();
		if (cherryPick) {
			bean.setCommitMessage(getCommit().getFullMessage());
			resultCommitId = getGitService().cherryPick(getProject(), getCommit().copy(),
					targetCommit.copy(), bean.getCommitMessage(), user.asPerson());
		} else {
			bean.setCommitMessage("Revert \"" + getCommit().getShortMessage() + "\"\n\nThis reverts commit " + getCommit().name());
			resultCommitId = getGitService().revert(getProject(), getCommit().copy(),
					targetCommit.copy(), bean.getCommitMessage(), user.asPerson());
		}
		if (resultCommitId != null) {
			var protection = getProject().getBranchProtection(branch, user);
			var errorMessage = checkUpdateBranch(protection, targetCommit.copy(), resultCommitId);
			if (errorMessage != null) {
				if (cherryPick)
					Session.get().error(MessageFormat.format(_T("Error cherry-picking to {0}: {1}"), branch, errorMessage));
				else
					Session.get().error(MessageFormat.format(_T("Error reverting on {0}: {1}"), branch, errorMessage));
			} else {
				new BeanEditModalPanel<>(target, bean, _T("Specify Commit Message")) {
					@Override
					protected boolean isDirtyAware() {
						return false;
					}

					@Override
					protected String getCssClass() {
						return "modal-lg commit-message no-autosize";
					}

					@Override
					protected String onSave(AjaxRequestTarget target, CommitMessageBean bean) {
						var errorMessage = protection.checkCommitMessage(bean.getCommitMessage(), false);
						if (errorMessage != null)
							return errorMessage;
						var amendedCommitId = getGitService().amendCommit(getProject(), resultCommitId,
								getCommit().getAuthorIdent(), getLoginUser().asPerson(), bean.getCommitMessage());
						getGitService().updateRef(getProject(), GitUtils.branch2ref(branch), amendedCommitId, targetCommit.copy());
						target.add(refsContainer);
						close();
						if (cherryPick)
							getSession().success(_T("Cherry-picked successfully"));
						else
							getSession().success(_T("Reverted successfully"));
						return null;
					}
				};
			}
		} else {
			if (cherryPick)
				Session.get().error(MessageFormat.format(_T("Error cherry-picking to {0}: Merge conflicts detected"), branch));
			else
				Session.get().error(MessageFormat.format(_T("Error reverting on {0}: Merge conflicts detected"), branch));
		}
	}

	public List<String> getOperateBranches() {
		return operateBranches;
	}

}
