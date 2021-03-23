package io.onedev.server.web.page.project.commits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.ComponentTag;
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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.CodeProblemContribution;
import io.onedev.server.code.LineCoverageContribution;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.branch.create.CreateBranchLink;
import io.onedev.server.web.component.build.simplelist.SimpleBuildListPanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.createtag.CreateTagLink;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.util.ReferenceTransformer;
import io.onedev.server.web.util.RevisionDiff;

@SuppressWarnings("serial")
public class CommitDetailPage extends ProjectPage implements RevisionDiff.AnnotationSupport {

	private static final Logger logger = LoggerFactory.getLogger(CommitDetailPage.class);
	
	private static final String PARAM_REVISION = "revision";
	
	// make sure to use a different value from wicket:id according to wicket bug:
	// https://issues.apache.org/jira/browse/WICKET-6069
	private static final String PARAM_COMPARE_WITH = "compare-with";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private State state;
	
	private ObjectId resolvedRevision;
	
	private ObjectId resolvedCompareWith;
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			CodeCommentManager manager = OneDev.getInstance(CodeCommentManager.class);
			return manager.query(projectModel.getObject(), getCompareWith(), resolvedRevision);
		}
		
	};
	
	private WebMarkupContainer refsContainer;
	
	private WebMarkupContainer revisionDiff;
	
	public CommitDetailPage(PageParameters params) {
		super(params);

		List<String> revisionSegments = new ArrayList<>();
		String segment = params.get(PARAM_REVISION).toString();
		if (segment.length() != 0)
			revisionSegments.add(segment);
		for (int i=0; i<params.getIndexedCount(); i++) {
			segment = params.get(i).toString();
			if (segment.length() != 0)
				revisionSegments.add(segment);
		}

		if (revisionSegments.isEmpty())
			throw new RestartResponseException(ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(getProject()));
		
		state = new State();
		state.revision = Joiner.on("/").join(revisionSegments);
		
		state.compareWith = params.get(PARAM_COMPARE_WITH).toString();
		state.whitespaceOption = WhitespaceOption.ofName(
				params.get(PARAM_WHITESPACE_OPTION).toString(WhitespaceOption.DEFAULT.name()));
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = Mark.fromString(params.get(PARAM_MARK).toString());
		
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
		
		ReferenceTransformer transformer = new ReferenceTransformer(getProject(), null);
		add(new Label("title", transformer.apply(getCommit().getShortMessage())).setEscapeModelStrings(false));

		BlobIdent blobIdent = new BlobIdent(getCommit().name(), null, FileMode.TYPE_TREE);
		ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
		PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), browseState);
		add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
		
		add(new CreateBranchLink("createBranch", projectModel, state.revision) {

			@Override
			protected void onCreated(AjaxRequestTarget target, String branch) {
				target.add(refsContainer);
			}
			
		});
		
		add(new CreateTagLink("createTag", projectModel, state.revision) {

			@Override
			protected void onCreated(AjaxRequestTarget target, String tag) {
				target.add(refsContainer);
			}
			
		});
		
		String message = GitUtils.getDetailMessage(getCommit());
		if (message != null) 
			add(new Label("detail", transformer.apply(message)).setEscapeModelStrings(false));
		else 
			add(new WebMarkupContainer("detail").setVisible(false));
		
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
				fragment.add(new ListView<RefInfo>("refs", new LoadableDetachableModel<List<RefInfo>>() {

					@Override
					protected List<RefInfo> load() {
						Collection<ObjectId> descendants = OneDev.getInstance(CommitInfoManager.class)
								.getDescendants(getProject(), Sets.newHashSet(getCommit().getId()));
						descendants.add(getCommit().getId());
					
						List<RefInfo> refs = new ArrayList<>();
						refs.addAll(getProject().getBranchRefInfos());
						refs.addAll(getProject().getTagRefInfos());
						return refs.stream().filter(ref->descendants.contains(ref.getPeeledObj())).collect(Collectors.toList());
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<RefInfo> item) {
						String ref = item.getModelObject().getRef().getName();
						String branch = GitUtils.ref2branch(ref); 
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
							String tag = Preconditions.checkNotNull(GitUtils.ref2tag(ref));
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
			
		});
		
		add(new ContributorAvatars("contributorAvatars", getCommit().getAuthorIdent(), getCommit().getCommitterIdent()));
		add(new ContributorPanel("contribution", getCommit().getAuthorIdent(), getCommit().getCommitterIdent()));

		add(new Label("hash", GitUtils.abbreviateSHA(getCommit().name())));
		add(new CopyToClipboardLink("copyHash", Model.of(getCommit().name())));
		
		newParentsContainer(null);

		add(new ListView<Job>("jobs", new LoadableDetachableModel<List<Job>>() {

			@Override
			protected List<Job> load() {
				List<Job> jobs = new ArrayList<>();
				try {
					BuildSpec buildSpec = getProject().getBuildSpec(getCommit().copy());
					if (buildSpec != null) {
						for (Job job: buildSpec.getJobs()) {
							if (SecurityUtils.canAccess(getProject(), job.getName()))
								jobs.add(job);
						}
					}
				} catch (Exception e) {
					logger.error("Error retrieving build spec (project: {}, commit: {})", 
							getProject().getName(), getCommit().name(), e);
				}
				return jobs;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Job> item) {
				ObjectId commitId = getCommit().copy();
				Job job = item.getModelObject();
				
				DropdownLink detailLink = new DropdownLink("detail") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						IModel<List<Build>> buildsModel = new LoadableDetachableModel<List<Build>>() {

							@Override
							protected List<Build> load() {
								BuildManager buildManager = OneDev.getInstance(BuildManager.class);
								List<Build> builds = new ArrayList<>(buildManager.query(getProject(), commitId, job.getName()));
								builds.sort(Comparator.comparing(Build::getNumber));
								return builds;
							}
							
						};						
						
						return new SimpleBuildListPanel(id, buildsModel) {

							@Override
							protected Component newListLink(String componentId) {
								return new BookmarkablePageLink<Void>(componentId, ProjectBuildsPage.class, 
										ProjectBuildsPage.paramsOf(getProject(), Job.getBuildQuery(commitId, job.getName(), null, null), 0)) {
									
									@Override
									protected void onConfigure() {
										super.onConfigure();
										setVisible(!getBuilds().isEmpty());
									}
									
								};
							}

						};
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						String cssClasses = "btn btn-outline-secondary";
						Build.Status status = getProject().getCommitStatus(commitId, null, null).get(job.getName());
						String title;
						if (status != null) {
							if (status != Status.SUCCESSFUL)
								title = "Some builds are "; 
							else
								title = "Builds are "; 
							title += status.getDisplayName().toLowerCase() + ", click for details";
						} else {
							title = "No builds";
							cssClasses += " no-builds";
						}
						tag.put("class", cssClasses);
						tag.put("title", title);
					}
					
				};
				detailLink.add(new BuildStatusIcon("status", new LoadableDetachableModel<Status>() {

					@Override
					protected Status load() {
						return getProject().getCommitStatus(commitId, null, null).get(job.getName());
					}
					
				}));
				
				detailLink.add(new Label("name", job.getName()));
				
				detailLink.add(new WebSocketObserver() {
					
					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler) {
						handler.add(component);
					}
					
					@Override
					public Collection<String> getObservables() {
						return Lists.newArrayList("job-status:" + getProject().getId() + ":" + commitId.name() + ":" + job.getName());
					}
					
				});
				
				detailLink.setOutputMarkupId(true);
				item.add(detailLink);
				
				item.add(new RunJobLink("run", commitId, job.getName(), null) {

					@Override
					protected Project getProject() {
						return CommitDetailPage.this.getProject();
					}

					@Override
					protected PullRequest getPullRequest() {
						return null;
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
			parents.add(new ListView<RevCommit>("parents", new LoadableDetachableModel<List<RevCommit>>() {

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
		IModel<String> blameModel = new IModel<String>() {

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
		IModel<String> pathFilterModel = new IModel<String>() {

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
		IModel<WhitespaceOption> whitespaceOptionModel = new IModel<WhitespaceOption>() {

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
				state.revision, pathFilterModel, whitespaceOptionModel, blameModel, this) {
			
			@Override
			protected Project getProject() {
				return projectModel.getObject();
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

	public static PageParameters paramsOf(CodeComment comment) {
		return paramsOf(comment.getProject(), getState(comment));
	}
	
	private static State getState(CodeComment comment) {
		State state = new State();
		state.commentId = comment.getId();
		state.mark = comment.getMark();
		CompareContext compareContext = comment.getCompareContext();
		String compareCommit = compareContext.getCompareCommitHash();
		if (compareContext.isLeftSide()) {
			state.compareWith = compareCommit;
			state.revision = comment.getMark().getCommitHash();
		} else {
			state.compareWith = comment.getMark().getCommitHash();
			state.revision = compareCommit;
		}
		state.whitespaceOption = compareContext.getWhitespaceOption();
		state.pathFilter = compareContext.getPathFilter();
		return state;
	}
	
	public static PageParameters paramsOf(Project project, State state) {
		PageParameters params = paramsOf(project);
		params.set(PARAM_REVISION, state.revision);
		if (state.compareWith != null)
			params.set(PARAM_COMPARE_WITH, state.compareWith);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.blameFile != null)
			params.set(PARAM_BLAME_FILE, state.blameFile);
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		return params;
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
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public Mark mark;
		
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

	@Override
	public CodeComment getOpenComment() {
		if (state.commentId != null)
			return OneDev.getInstance(CodeCommentManager.class).load(state.commentId);
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
		OneDev.getInstance(CodeCommentManager.class).save(comment);
	}
	
	@Override
	public void onSaveCommentReply(CodeCommentReply reply) {
		OneDev.getInstance(CodeCommentReplyManager.class).save(reply);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("commits", ProjectCommitsPage.class, 
				ProjectCommitsPage.paramsOf(getProject())));
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
		for (Build build: getBuilds(getCompareWith())) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Collection<CodeProblem> getNewProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		for (Build build: getBuilds(getCommit())) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Map<Integer, Integer> getOldCoverages(String blobPath) {
		Map<Integer, Integer> coverages = new HashMap<>();
		for (Build build: getBuilds(getCompareWith())) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1+v2);
				});
			}
		}
		return coverages;
	}

	@Override
	public Map<Integer, Integer> getNewCoverages(String blobPath) {
		Map<Integer, Integer> coverages = new HashMap<>();
		for (Build build: getBuilds(getCommit())) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1+v2);
				});
			}
		}
		return coverages;
	}
	
	@Override
	protected String getPageTitle() {
		return getCommit().getShortMessage() 
				+ " - Commit " +  GitUtils.abbreviateSHA(getCommit().getName()) 
				+ " - " + getProject().getName();
	}
	
}
