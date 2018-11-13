package io.onedev.server.web.page.project.commits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.CodeCommentManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.search.code.CommitIndexed;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.branch.create.CreateBranchLink;
import io.onedev.server.web.component.build.status.BuildsStatusPanel;
import io.onedev.server.web.component.commit.message.CommitMessageLabel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.createtag.CreateTagLink;
import io.onedev.server.web.component.diff.revision.CommentSupport;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;

@SuppressWarnings("serial")
public class CommitDetailPage extends ProjectPage implements CommentSupport {

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
	
	private WebMarkupContainer revisionDiff;
	
	public CommitDetailPage(PageParameters params) {
		super(params);
		
		state = new State(params);
		resolvedRevision = getProject().getRevCommit(state.revision).copy();
		if (state.compareWith != null)
			resolvedCompareWith = getProject().getRevCommit(state.compareWith).copy();
	}

	private RevCommit getCommit() {
		return getProject().getRevCommit(state.revision);
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
		
		add(new CommitMessageLabel("text", projectModel, Model.of(getCommit().getShortMessage())));

		BlobIdent blobIdent = new BlobIdent(getCommit().name(), null, FileMode.TYPE_TREE);
		ProjectBlobPage.State browseState = new ProjectBlobPage.State(blobIdent);
		PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), browseState);
		add(new ViewStateAwarePageLink<Void>("browseCode", ProjectBlobPage.class, params));
		
		add(new CreateBranchLink("createBranch", projectModel, state.revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String branch) {
				setResponsePage(ProjectBranchesPage.class, ProjectBranchesPage.paramsOf(getProject()));
			}
			
		});
		
		add(new CreateTagLink("createTag", projectModel, state.revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String tag) {
				setResponsePage(ProjectTagsPage.class, ProjectTagsPage.paramsOf(getProject()));
			}
			
		});
		
		String message = GitUtils.getDetailMessage(getCommit());
		if (message != null) {
			add(new CommitMessageLabel("detail", projectModel, Model.of(message)));
		} else {
			add(new WebMarkupContainer("detail").setVisible(false));
		}
		
		add(new AjaxLazyLoadPanel("refs") {

			@Override
			public Component getLazyLoadComponent(String markupId) {
				Fragment fragment = new Fragment(markupId, "refsFrag", CommitDetailPage.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format("onedev.server.commitdetail.initRefs('%s');", getMarkupId());
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
						refs.addAll(getProject().getBranches());
						refs.addAll(getProject().getTags());
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
							link.add(new Label("label", branch));
							item.add(link);
							item.add(AttributeAppender.append("class", "branch"));
						} else {
							String tag = Preconditions.checkNotNull(GitUtils.ref2tag(ref));
							BlobIdent blobIdent = new BlobIdent(tag, null, FileMode.TREE.getBits());
							ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
							Link<Void> link = new ViewStateAwarePageLink<Void>("link", ProjectBlobPage.class, 
									ProjectBlobPage.paramsOf(projectModel.getObject(), state));
							link.add(new Label("label", tag));
							item.add(link);
							item.add(AttributeAppender.append("class", "tag"));
						}
					}
					
				});
				return fragment;
			}
			
		});
		
		add(new ContributorAvatars("contributorAvatars", getCommit().getAuthorIdent(), getCommit().getCommitterIdent()));
		add(new ContributorPanel("contribution", getCommit().getAuthorIdent(), getCommit().getCommitterIdent(), true));

		add(new BuildsStatusPanel("buildStatus", new LoadableDetachableModel<List<Build>>() {

			@Override
			protected List<Build> load() {
				return OneDev.getInstance(BuildManager.class).query(getProject(), getCommit().name());
			}
			
		}));
		
		add(new Label("hash", GitUtils.abbreviateSHA(getCommit().name())));
		add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(getCommit().name()))));
		
		newParentsContainer(null);

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
		revisionDiff = new RevisionDiffPanel("revisionDiff", projectModel,  
				Model.of((PullRequest)null), getCompareWith().name(), state.revision, 
				pathFilterModel, whitespaceOptionModel, blameModel, this);
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
		public MarkPos mark;
		
		public State() {
		}
		
		public State(PageParameters params) {
			List<String> revisionSegments = new ArrayList<>();
			String segment = params.get(PARAM_REVISION).toString();
			if (segment.length() != 0)
				revisionSegments.add(segment);
			for (int i=0; i<params.getIndexedCount(); i++) {
				segment = params.get(i).toString();
				if (segment.length() != 0)
					revisionSegments.add(segment);
			}
			
			revision = Joiner.on("/").join(revisionSegments);
			compareWith = params.get(PARAM_COMPARE_WITH).toString();
			whitespaceOption = WhitespaceOption.ofNullableName(params.get(PARAM_WHITESPACE_OPTION).toString());
			pathFilter = params.get(PARAM_PATH_FILTER).toString();
			blameFile = params.get(PARAM_BLAME_FILE).toString();
			commentId = params.get(PARAM_COMMENT).toOptionalLong();
			mark = MarkPos.fromString(params.get(PARAM_MARK).toString());
		}
		
	}

	@Override
	public MarkPos getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(MarkPos mark) {
		State markState = new State();
		markState.mark = mark;
		markState.whitespaceOption = state.whitespaceOption;
		markState.compareWith = state.compareWith;
		markState.pathFilter = state.pathFilter;
		markState.revision = resolvedRevision.name();
		return urlFor(CommitDetailPage.class, paramsOf(getProject(), markState)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State commentState = new State();
		String compareWith = getCompareWith().name();
		commentState.mark = comment.getMarkPos();
		commentState.commentId = comment.getId();
		commentState.whitespaceOption = state.whitespaceOption;
		commentState.compareWith = compareWith;
		commentState.pathFilter = state.pathFilter;
		commentState.revision = resolvedRevision.name();
		return urlFor(CommitDetailPage.class, paramsOf(getProject(), commentState)).toString();
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
		if (comment != null) {
			state.commentId = comment.getId();
			state.mark = comment.getMarkPos();
		} else {
			state.commentId = null;
			state.mark = null;
		}
		pushState(target);
	}
	
	@Override
	public void onMark(AjaxRequestTarget target, MarkPos mark) {
		state.mark = mark;
		pushState(target);
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, MarkPos mark) {
		state.commentId = null;
		state.mark = mark;
		pushState(target);
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
	public Collection<String> getWebSocketObservables() {
		Collection<String> observables = super.getWebSocketObservables();
		observables.add(CommitIndexed.getWebSocketObservable(resolvedRevision.name()));
		observables.add(CommitIndexed.getWebSocketObservable(getCompareWith().name()));
		return observables;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
}
