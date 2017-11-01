package com.gitplex.server.web.component.diff.revision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.codeassist.InputCompletion;
import com.gitplex.codeassist.InputStatus;
import com.gitplex.codeassist.InputSuggestion;
import com.gitplex.jsymbol.util.NoAntiCacheImage;
import com.gitplex.server.GitPlex;
import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobChange;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.CompareContext;
import com.gitplex.server.model.support.MarkPos;
import com.gitplex.server.model.support.ProjectAndRevision;
import com.gitplex.server.search.IndexManager;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.diff.DiffUtils;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.inputassist.InputAssistBehavior;
import com.gitplex.server.web.component.comment.CodeCommentPanel;
import com.gitplex.server.web.component.comment.CommentInput;
import com.gitplex.server.web.component.comment.ProjectAttachmentSupport;
import com.gitplex.server.web.component.diff.blob.BlobDiffPanel;
import com.gitplex.server.web.component.diff.blob.SourceAware;
import com.gitplex.server.web.component.diff.diffstat.DiffStatBar;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.menu.MenuItem;
import com.gitplex.server.web.component.menu.MenuLink;
import com.gitplex.server.web.component.revisionpicker.RevisionSelector;
import com.gitplex.server.web.page.project.compare.RevisionComparePage;
import com.gitplex.server.web.util.SuggestionUtils;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.server.web.websocket.PageDataChanged;
import com.gitplex.utils.PathComparator;
import com.gitplex.utils.Range;
import com.gitplex.utils.StringUtils;
import com.gitplex.utils.stringmatch.WildcardUtils;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Make sure to add only one revision diff panel on a page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "gitplex.server.diff.viewmode";

	private static final String BODY_ID = "body";
	
	private static final String DIFF_ID = "diff";

	private final IModel<Project> projectModel;
	
	private final IModel<PullRequest> requestModel;

	private final String oldRev;
	
	private final String newRev;

	private final IModel<String> blameModel;
	
	private final CommentSupport commentSupport;
	
	private final IModel<String> pathFilterModel;
	
	private final IModel<WhitespaceOption> whitespaceOptionModel;
	
	private DiffViewMode diffMode;
	
	private IModel<List<DiffEntry>> diffEntriesModel = new LoadableDetachableModel<List<DiffEntry>>() {

		@Override
		protected List<DiffEntry> load() {
			AnyObjectId oldRevId = projectModel.getObject().getObjectId(oldRev);
			AnyObjectId newRevId = projectModel.getObject().getObjectId(newRev);
			return GitUtils.diff(projectModel.getObject().getRepository(), oldRevId, newRevId);
		}
		
	};
	
	private IModel<ChangesAndCount> changesAndCountModel = new LoadableDetachableModel<ChangesAndCount>() {

		@Override
		protected ChangesAndCount load() {
			List<DiffEntry> diffEntries = diffEntriesModel.getObject();
			
			Set<String> changedPaths = new HashSet<>();
			List<BlobChange> allChanges = new ArrayList<>();
			for (DiffEntry entry: diffEntries) {
    			BlobChange change = new BlobChange(oldRev, newRev, entry, whitespaceOptionModel.getObject()) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						return projectModel.getObject().getBlob(blobIdent);
					}

	    		};
	    		allChanges.add(change);
	    		changedPaths.addAll(change.getPaths());
			}

			Set<String> markedPaths = new HashSet<>();
			for (CodeComment comment: commentsModel.getObject()) {
				if (!changedPaths.contains(comment.getMarkPos().getPath()) 
						&& !markedPaths.contains(comment.getMarkPos().getPath())) {
					BlobIdent oldBlobIdent = new BlobIdent(oldRev, comment.getMarkPos().getPath(), FileMode.TYPE_FILE);
					BlobIdent newBlobIdent = new BlobIdent(newRev, comment.getMarkPos().getPath(), FileMode.TYPE_FILE);
					allChanges.add(new BlobChange(null, oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()) {

						@Override
						public Blob getBlob(BlobIdent blobIdent) {
							return projectModel.getObject().getBlob(blobIdent);
						}
						
					});
				}
				markedPaths.add(comment.getMarkPos().getPath());
			}
			
			MarkPos mark = getMark();
			if (mark != null && !changedPaths.contains(mark.getPath()) && !markedPaths.contains(mark.getPath())) {
				BlobIdent oldBlobIdent = new BlobIdent(oldRev, mark.getPath(), FileMode.TYPE_FILE);
				BlobIdent newBlobIdent = new BlobIdent(newRev, mark.getPath(), FileMode.TYPE_FILE);
				allChanges.add(new BlobChange(null, oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						return projectModel.getObject().getBlob(blobIdent);
					}
					
				});
				markedPaths.add(mark.getPath());
			}
			
			List<BlobChange> filterChanges;
    		String matchWith = pathFilterModel.getObject();
    		if (StringUtils.isNotBlank(matchWith)) {
        		matchWith = matchWith.toLowerCase().trim();
        		boolean exclude = false;
        		if (matchWith.startsWith("-")) {
        			matchWith = matchWith.substring(1).trim();
        			exclude = true;
        		}
    			matchWith = StringUtils.stripStart(matchWith, "/");
    			matchWith = StringUtils.stripEnd(matchWith, "/");

    			List<BlobChange> matchChanges = new ArrayList<>();
    	    	for (BlobChange change: allChanges) {
        			String oldPath = change.getOldBlobIdent().path;
        			if (oldPath == null)
        				oldPath = "";
        			else
        				oldPath = oldPath.toLowerCase();
        			String newPath = change.getNewBlobIdent().path;
        			if (newPath == null)
        				newPath = "";
        			else
        				newPath = newPath.toLowerCase();
        			if (matchWith.equals(oldPath) || matchWith.equals(newPath)) {
        				matchChanges.add(change);
        			} else if (oldPath.startsWith(matchWith + "/") || newPath.startsWith(matchWith + "/")) {
        				matchChanges.add(change);
        			} else if (WildcardUtils.matchString(matchWith, oldPath) 
        					|| WildcardUtils.matchString(matchWith, newPath)){
        				matchChanges.add(change);
        			}
    	    	}
    	    	
    			if (exclude) {
    				filterChanges = new ArrayList<>(allChanges);
    				filterChanges.removeAll(matchChanges);
    			} else {
    				filterChanges = matchChanges;
    			}
    		} else {
    			filterChanges = new ArrayList<>(allChanges);
    		}
			
	    	// for some unknown reason, some paths in the diff entries is DELETE/ADD 
	    	// pair instead MODIFICATION, here we normalize those as a single 
	    	// MODIFICATION entry
	    	Map<String, BlobIdent> deleted = new HashMap<>();
	    	Map<String, BlobIdent> added = new HashMap<>();
	    	for (BlobChange change: filterChanges) {
	    		if (change.getType() == ChangeType.DELETE)
	    			deleted.put(change.getPath(), change.getOldBlobIdent());
	    		else if (change.getType() == ChangeType.ADD) 
	    			added.put(change.getPath(), change.getNewBlobIdent());
	    	}
	    	
	    	List<BlobChange> normalizedChanges = new ArrayList<>();
	    	for (BlobChange change: filterChanges) {
	    		BlobIdent oldBlobIdent = deleted.get(change.getPath());
	    		BlobIdent newBlobIdent = added.get(change.getPath());
	    		if (oldBlobIdent != null && newBlobIdent != null) {
	    			if (change.getType() == ChangeType.DELETE) {
	        			BlobChange normalizedChange = new BlobChange(ChangeType.MODIFY, 
	        					oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()) {

	    					@Override
	    					public Blob getBlob(BlobIdent blobIdent) {
	    						return projectModel.getObject().getBlob(blobIdent);
	    					}

	    	    		};
	    				normalizedChanges.add(normalizedChange);
	    			}
	    		} else {
	    			normalizedChanges.add(change);
	    		}
	    	}

	    	PathComparator comparator = new PathComparator();
	    	normalizedChanges.sort((change1, change2)->comparator.compare(change1.getPath(), change2.getPath()));
	    	
			List<BlobChange> diffChanges = new ArrayList<>();
			if (normalizedChanges.size() > WebConstants.MAX_DIFF_FILES)
				diffChanges = normalizedChanges.subList(0, WebConstants.MAX_DIFF_FILES);
			else
				diffChanges = normalizedChanges;
			
	    	// Diff calculation can be slow, so we pre-load diffs of each change 
	    	// concurrently
	    	Collection<Callable<Void>> tasks = new ArrayList<>();
	    	for (BlobChange change: diffChanges) {
	    		tasks.add(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						change.getDiffBlocks();
						return null;
					}
	    			
	    		});
	    	}
	    	for (Future<Void> future: GitPlex.getInstance(ForkJoinPool.class).invokeAll(tasks)) {
	    		try {
	    			// call get in order to throw exception if there is any during task execution
					future.get();
				} catch (InterruptedException|ExecutionException e) {
					throw new RuntimeException(e);
				}
	    	}
	    	
	    	int totalChanges = normalizedChanges.size();
	    	
	    	if (diffChanges.size() == totalChanges) { 
		    	// some changes should be removed if content is the same after line processing 
		    	for (Iterator<BlobChange> it = diffChanges.iterator(); it.hasNext();) {
		    		BlobChange change = it.next();
		    		if (change.getType() == ChangeType.MODIFY 
		    				&& Objects.equal(change.getOldBlobIdent().mode, change.getNewBlobIdent().mode)
		    				&& change.getAdditions() + change.getDeletions() == 0
		    				&& !markedPaths.contains(change.getPath())) {
		    			Blob.Text oldText = change.getOldText();
		    			Blob.Text newText = change.getNewText();
		    			if (oldText != null && newText != null 
		    					&& (oldText.getLines().size() + newText.getLines().size()) <= DiffUtils.MAX_DIFF_SIZE) {
			    			it.remove();
		    			}
		    		}
		    	}
		    	totalChanges = diffChanges.size();
	    	} 
	    	
	    	List<BlobChange> displayChanges = new ArrayList<>();
	    	int totalChangedLines = 0;
	    	for (BlobChange change: diffChanges) {
	    		int changedLines = change.getAdditions() + change.getDeletions(); 
	    		/*
	    		 * we do not count large diff in a single file in order to
	    		 * display smaller diffs from different files as many as
	    		 * possible
	    		 */
	    		if (changedLines <= WebConstants.MAX_SINGLE_FILE_DIFF_LINES) {
		    		totalChangedLines += changedLines;
		    		if (totalChangedLines <= WebConstants.MAX_DIFF_LINES)
		    			displayChanges.add(change);
		    		else
		    			break;
	    		} else {
	    			/*
	    			 * large diff in a single file will not be displayed, but 
	    			 * we still add it into the list as otherwise we may 
	    			 * incorrectly display the "too many changed files" warning  
	    			 */
	    			displayChanges.add(change);
	    		}
	    	}
	    	return new ChangesAndCount(displayChanges, totalChanges);
		}
	};
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			Collection<CodeComment> comments = new ArrayList<>();
			if (commentSupport != null) {
				CodeCommentManager codeCommentManager = GitPlex.getInstance(CodeCommentManager.class);
				PullRequest request = requestModel.getObject();
				for(CodeComment comment: 
						codeCommentManager.findAll(projectModel.getObject(), getOldCommitId(), getNewCommitId())) {
					if (request == null || request.getRequestComparingInfo(comment.getComparingInfo()) != null)
						comments.add(comment);
				}
			} 
			return comments;
		}
		
	};
	
	private final IModel<List<CodeComment>> commitCommentsModel = new LoadableDetachableModel<List<CodeComment>>() {

		@Override
		protected List<CodeComment> load() {
			List<CodeComment> commitComments = new ArrayList<>();
			for (CodeComment comment: commentsModel.getObject()) {
				if (comment.getMarkPos().getPath() == null)
					commitComments.add(comment);
			}
			return commitComments;
		}
		
	};
	
	private WebMarkupContainer commentContainer;

	private ListView<BlobChange> diffsView;
	
	public RevisionDiffPanel(String id, IModel<Project> projectModel, IModel<PullRequest> requestModel, 
			String oldRev, String newRev, IModel<String> pathFilterModel, IModel<WhitespaceOption> whitespaceOptionModel, 
			@Nullable IModel<String> blameModel, @Nullable CommentSupport commentSupport) {
		super(id);
		
		this.projectModel = projectModel;
		this.requestModel = requestModel;
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.pathFilterModel = pathFilterModel;
		this.blameModel = new IModel<String>() {

			@Override
			public void detach() {
				blameModel.detach();
			}

			@Override
			public String getObject() {
				return blameModel.getObject();
			}

			@Override
			public void setObject(String object) {
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				String prevBlameFile = blameModel.getObject();
				blameModel.setObject(object);
				if (prevBlameFile != null && object != null && !prevBlameFile.equals(object)) {
					SourceAware sourceAware = getSourceAware(prevBlameFile);
					sourceAware.onUnblame(target);
				}
				target.appendJavaScript("gitplex.server.revisionDiff.reposition();");
			}
			
		};
		this.whitespaceOptionModel = whitespaceOptionModel;
		this.commentSupport = commentSupport;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_VIEW_MODE);
		if (cookie == null)
			diffMode = DiffViewMode.UNIFIED;
		else
			diffMode = DiffViewMode.valueOf(cookie.getValue());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("revisionsIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new NoAntiCacheImage("icon", 
						new PackageResourceReference(RevisionDiffPanel.class, "indexing.gif")));
				
				setOutputMarkupPlaceholderTag(true);
			}
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					pageDataChanged.getHandler().add(this);
					pageDataChanged.getHandler().appendJavaScript("$(window).resize();");
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				Project project = projectModel.getObject();
				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				ObjectId oldCommit = getOldCommitId();
				ObjectId newCommit = getNewCommitId();
				boolean oldCommitIndexed = oldCommit.equals(ObjectId.zeroId()) 
						|| indexManager.isIndexed(project, oldCommit);
				boolean newCommitIndexed = newCommit.equals(ObjectId.zeroId()) 
						|| indexManager.isIndexed(project, newCommit);
				if (oldCommitIndexed && newCommitIndexed) {
					setVisible(false);
				} else {
					if (!oldCommitIndexed)
						indexManager.indexAsync(project, oldCommit);
					if (!newCommitIndexed)
						indexManager.indexAsync(project, newCommit);
					setVisible(true);
				}
			}
			
		});

		WebMarkupContainer body = new WebMarkupContainer(BODY_ID) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(OnDomReadyHeaderItem.forScript("gitplex.server.revisionDiff.onDomReady();"));
			}
			
		};
		body.setOutputMarkupId(true);
		add(body);
		
		for (DiffViewMode each: DiffViewMode.values()) {
			add(new AjaxLink<Void>(each.name().toLowerCase()) {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					diffMode = each;
					WebResponse response = (WebResponse) RequestCycle.get().getResponse();
					Cookie cookie = new Cookie(COOKIE_VIEW_MODE, diffMode.name());
					cookie.setMaxAge(Integer.MAX_VALUE);
					response.addCookie(cookie);
					target.add(RevisionDiffPanel.this);
				}
				
			}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return each==diffMode?" active":"";
				}
				
			})));
		}
		
		add(new MenuLink("whitespaceOption") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				for (WhitespaceOption each: WhitespaceOption.values()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return each.getDescription();
						}

						@Override
						public String getIconClass() {
							if (whitespaceOptionModel.getObject() == each)
								return "fa fa-check";
							else
								return null;
						}

						@Override
						public AbstractLink newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									whitespaceOptionModel.setObject(each);
									target.add(body);
								}

								@Override
								protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
									super.updateAjaxAttributes(attributes);
									attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
								}
								
							};
						}
						
					});
				}

				return menuItems;
			}
			
		});
		
		Form<?> pathFilterForm = new Form<Void>("pathFilter");
		TextField<String> filterInput;
		pathFilterForm.add(filterInput = new TextField<String>("input", pathFilterModel));
		
		Set<String> setOfInvolvedPaths = new HashSet<>();
		for (DiffEntry diffEntry: diffEntriesModel.getObject()) {
			if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD) {
				setOfInvolvedPaths.add(diffEntry.getNewPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.COPY) {
				setOfInvolvedPaths.add(diffEntry.getNewPath());
				setOfInvolvedPaths.add(diffEntry.getOldPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
				setOfInvolvedPaths.add(diffEntry.getOldPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
				setOfInvolvedPaths.add(diffEntry.getNewPath());
			} else if (diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME) {
				setOfInvolvedPaths.add(diffEntry.getNewPath());
				setOfInvolvedPaths.add(diffEntry.getOldPath());
			} else {
				throw new IllegalStateException();
			}
		}
		for (CodeComment comment: commentsModel.getObject()) {
			setOfInvolvedPaths.add(comment.getMarkPos().getPath());
		}
		
		List<String> listOfInvolvedPaths = new ArrayList<>(setOfInvolvedPaths);
		listOfInvolvedPaths.sort(new PathComparator());
		
		filterInput.add(new InputAssistBehavior() {
			
			@Override
			protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
				List<InputCompletion> completions = new ArrayList<>();
				String matchWith = inputStatus.getContentBeforeCaret().trim();
				boolean exclude = false;
				if (matchWith.startsWith("-")) {
					matchWith = matchWith.substring(1).trim();
					exclude = true;
				}
				
				for (InputSuggestion suggestion: SuggestionUtils.suggestPath(listOfInvolvedPaths, matchWith)) {
					int caret = suggestion.getCaret();
					if (caret == -1)
						caret = suggestion.getContent().length();
					InputCompletion completion;
					if (exclude) {
						completion = new InputCompletion(0, inputStatus.getContent().length(), 
								"-"+suggestion.getContent(), caret+1, null, null, suggestion.getMatchRange());
					} else {
						completion = new InputCompletion(0, inputStatus.getContent().length(), suggestion.getContent(), 
								caret, null, null, suggestion.getMatchRange());
					}
					completions.add(completion);
				}
				return completions;
			}
			
			@Override
			protected List<String> getHints(InputStatus inputStatus) {
				return Lists.newArrayList("Use '*' to match any part of the path", "Prefix path with '-' to exclude");
			}

			@Override
			protected List<Range> getErrors(String inputContent) {
				return null;
			}
			
			@Override
			protected int getAnchor(String content) {
				for (int i=0; i<content.length(); i++) {
					if (!Character.isWhitespace(content.charAt(i)))
						return i;
				}
				return content.length();
			}
			
		});
		
		pathFilterForm.add(new AjaxButton("submit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				body.replace(commentContainer = newCommentContainer());
				target.add(body);
			}
			
		});
		add(pathFilterForm);
		
		body.add(commentContainer = newCommentContainer());
		
		Component totalFilesLink;
		body.add(totalFilesLink = new Label("totalFiles", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return changesAndCountModel.getObject().getChanges().size() + " files ";
			}
			
		}));
		
		body.add(new WebMarkupContainer("tooManyFiles") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				ChangesAndCount changesAndCount = changesAndCountModel.getObject();
				setVisible(changesAndCount.getChanges().size() < changesAndCount.getCount());
			}
			
		});
		
		WebMarkupContainer diffStats = new WebMarkupContainer("diffStats");
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie("revisionDiff.showDiffStats");
		if (cookie == null || !"yes".equals(cookie.getValue())) {
			diffStats.add(AttributeAppender.append("style", "display:none;"));
		} else {
			totalFilesLink.add(AttributeAppender.append("class", "expanded"));			
		}
		body.add(diffStats);
		diffStats.add(new ListView<BlobChange>("diffStats", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return changesAndCountModel.getObject().getChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				String iconClass;
				if (change.getType() == null) {
					iconClass = " fa fa-file-text-o";
				} else if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY)
					iconClass = " fa-ext fa-diff-added";
				else if (change.getType() == ChangeType.DELETE)
					iconClass = " fa-ext fa-diff-removed";
				else if (change.getType() == ChangeType.MODIFY)
					iconClass = " fa-ext fa-diff-modified";
				else
					iconClass = " fa-ext fa-diff-renamed";
				
				item.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));

				item.add(new WebMarkupContainer("hasComments").setVisible(!getComments(change).isEmpty()));
				
				WebMarkupContainer fileLink = new WebMarkupContainer("file");
				fileLink.add(new Label("name", change.getPath()));
				fileLink.add(AttributeModifier.replace("href", "#diff-" + change.getPath()));
				item.add(fileLink);

				item.add(new Label("additions", "+" + change.getAdditions()));
				item.add(new Label("deletions", "-" + change.getDeletions()));
				
				boolean barVisible;
				if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY) {
					Blob.Text text = change.getNewText();
					barVisible = (text != null && text.getLines().size() <= DiffUtils.MAX_DIFF_SIZE);
				} else if (change.getType() == ChangeType.DELETE) {
					Blob.Text text = change.getOldText();
					barVisible = (text != null && text.getLines().size() <= DiffUtils.MAX_DIFF_SIZE);
				} else {
					Blob.Text oldText = change.getOldText();
					Blob.Text newText = change.getNewText();
					barVisible = (oldText != null && newText != null 
							&& oldText.getLines().size()+newText.getLines().size() <= DiffUtils.MAX_DIFF_SIZE);
				}
				item.add(new DiffStatBar("bar", change.getAdditions(), change.getDeletions(), false).setVisible(barVisible));
			}
			
		});
		
		body.add(diffsView = new ListView<BlobChange>("diffs", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return changesAndCountModel.getObject().getChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				item.setMarkupId("diff-" + change.getPath());
				if (commentSupport != null) {
					item.add(new BlobDiffPanel(DIFF_ID, projectModel, requestModel, change, diffMode, 
							getBlobBlameModel(change), new BlobCommentSupport() {
	
						@Override
						public MarkPos getMark() {
							MarkPos mark = RevisionDiffPanel.this.getMark();
							if (mark != null && change.getPaths().contains(mark.getPath()))
								return mark;
							else
								return null;
						}
	
						@Override
						public String getMarkUrl(MarkPos mark) {
							return commentSupport.getMarkUrl(mark);
						}
	
						@Override
						public CodeComment getOpenComment() {
							CodeComment comment = RevisionDiffPanel.this.getOpenComment();
							if (comment != null && change.getPaths().contains(comment.getMarkPos().getPath()))
								return comment;
							else
								return null;
						}
	
						@Override
						public void onToggleComment(AjaxRequestTarget target, CodeComment comment) {
							RevisionDiffPanel.this.onToggleComment(target, comment);
						}
	
						@Override
						public void onAddComment(AjaxRequestTarget target, MarkPos markPos) {
							commentContainer.setDefaultModelObject(markPos);
							
							Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", RevisionDiffPanel.this);
							fragment.setOutputMarkupId(true);
							
							Form<?> form = new Form<Void>("form");
							
							String uuid = UUID.randomUUID().toString();
							
							String autosaveKey = "autosave:addCodeCommentOnDiff:" + projectModel.getObject().getId() 
									+ ":" + change.getPath();
							CommentInput contentInput;
							
							StringBuilder mentions = new StringBuilder();

							if (requestModel.getObject() == null) {
								/*
								 * Outside of pull request, no one will be notified of the comment. So we automatically 
								 * mention authors of commented lines
								 */
								Range range = new Range(markPos.getRange().getBeginLine(), markPos.getRange().getEndLine());
								ObjectId commitId = ObjectId.fromString(markPos.getCommit());
								for (User user: projectModel.getObject().getAuthors(markPos.getPath(), commitId, range)) {
									if (user.getEmail() != null)
										mentions.append("@").append(user.getName()).append(" ");
								}
							}
							
							form.add(contentInput = new CommentInput("content", Model.of(mentions.toString()), true) {

								@Override
								protected ProjectAttachmentSupport getAttachmentSupport() {
									return new ProjectAttachmentSupport(projectModel.getObject(), uuid);
								}

								@Override
								protected Project getProject() {
									return projectModel.getObject();
								}
								
								@Override
								protected String getAutosaveKey() {
									return autosaveKey;
								}
								
							});
							contentInput.setRequired(true);
							
							NotificationPanel feedback = new NotificationPanel("feedback", form); 
							feedback.setOutputMarkupPlaceholderTag(true);
							form.add(feedback);
							
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
									super.updateAjaxAttributes(attributes);
									attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
								}
								
								@Override
								public void onClick(AjaxRequestTarget target) {
									clearComment(target);
									MarkPos mark = getMark();
									if (mark != null) {
										SourceAware sourceAware = getSourceAware(mark.getPath());
										if (sourceAware != null) 
											sourceAware.mark(target, null);
										((CommentSupport)commentSupport).onMark(target, null);
									}
									
									target.appendJavaScript("gitplex.server.revisionDiff.reposition();");
								}
								
							});
							
							form.add(new AjaxButton("save") {

								@Override
								protected void onError(AjaxRequestTarget target, Form<?> form) {
									super.onError(target, form);
									target.add(feedback);
								}

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									
									CodeComment comment = new CodeComment();
									comment.setUUID(uuid);
									comment.setProject(projectModel.getObject());
									comment.setUser(SecurityUtils.getUser());
									comment.setMarkPos(markPos);
									comment.setContent(contentInput.getModelObject());
									comment.setCompareContext(getCompareContext(comment.getMarkPos().getCommit()));
									
									GitPlex.getInstance(CodeCommentManager.class).save(comment, requestModel.getObject());
									
									CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), comment.getId()) {

										@Override
										protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
											RevisionDiffPanel.this.onCommentDeleted(target, comment);
										}
										
										@Override
										protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
											target.add(commentContainer.get("head"));
										}

										@Override
										protected PullRequest getPullRequest() {
											return requestModel.getObject();
										}

										@Override
										protected CompareContext getCompareContext() {
											return RevisionDiffPanel.this.getCompareContext(
													comment.getMarkPos().getCommit());
										}

									};
									commentContainer.replace(commentPanel);
									target.add(commentContainer);
									
									SourceAware sourceAware = getSourceAware(comment.getMarkPos().getPath());
									if (sourceAware != null) 
										sourceAware.onCommentAdded(target, comment);

									((CommentSupport)commentSupport).onCommentOpened(target, comment);
									target.appendJavaScript(String.format(""
											+ "gitplex.server.revisionDiff.reposition();"
											+ "localStorage.removeItem('%s');", 
											autosaveKey));							
								}

							});
							fragment.add(form);
							
							commentContainer.replace(fragment);
							commentContainer.setVisible(true);
							target.add(commentContainer);
							
							MarkPos prevMark = RevisionDiffPanel.this.getMark();
							if (prevMark != null) {
								SourceAware sourceAware = getSourceAware(prevMark.getPath());
								if (sourceAware != null) 
									sourceAware.mark(target, null);
							}
							
							CodeComment prevComment = RevisionDiffPanel.this.getOpenComment();
							if (prevComment != null) {
								SourceAware sourceAware = getSourceAware(prevComment.getMarkPos().getPath());
								if (sourceAware != null) 
									sourceAware.onCommentClosed(target, prevComment);
							}  
							((CommentSupport)commentSupport).onAddComment(target, markPos);
							String script = String.format(""
									+ "gitplex.server.revisionDiff.reposition(); "
									+ "setTimeout(function() {"
									+ "  var $textarea = $('#%s textarea');"
									+ "  $textarea.caret($textarea.val().length);"
									+ "}, 100);", 
									commentContainer.getMarkupId());
							target.appendJavaScript(script);		
						}

						@Override
						public Collection<CodeComment> getComments() {
							return RevisionDiffPanel.this.getComments(change);
						}

						@Override
						public Component getDirtyContainer() {
							return commentContainer;
						}

					}));
				} else {
					item.add(new BlobDiffPanel(DIFF_ID, projectModel, requestModel, change, 
							diffMode, getBlobBlameModel(change), null));
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private Collection<CodeComment> getComments(BlobChange change) {
		Collection<CodeComment> comments = new ArrayList<>();
		for (CodeComment comment: commentsModel.getObject()) {
			if (change.getPaths().contains(comment.getMarkPos().getPath())) {
				comments.add(comment);
			}
		}
		return comments;
	}

	private CompareContext getCompareContext(String commitHash) {
		CompareContext compareContext = new CompareContext();
		String oldCommitHash = getOldCommitId().name();
		String newCommitHash = getNewCommitId().name();
		if (commitHash.equals(oldCommitHash)) {
			compareContext.setCompareCommit(newCommitHash);
			compareContext.setLeftSide(false);
		} else {
			compareContext.setCompareCommit(oldCommitHash);
			compareContext.setLeftSide(true);
		}
		compareContext.setPathFilter(pathFilterModel.getObject());
		compareContext.setWhitespaceOption(whitespaceOptionModel.getObject());
		return compareContext;
	}
	
	private void onToggleComment(AjaxRequestTarget target, CodeComment comment) {
		if (!comment.equals(getOpenComment())) {
			CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, comment.getId()) {

				@Override
				protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
					RevisionDiffPanel.this.onCommentDeleted(target, comment);
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
					target.add(commentContainer.get("head"));
				}

				@Override
				protected PullRequest getPullRequest() {
					return requestModel.getObject();
				}

				@Override
				protected CompareContext getCompareContext() {
					return RevisionDiffPanel.this.getCompareContext(comment.getMarkPos().getCommit());
				}

			};
			
			commentContainer.replace(commentPanel);
			commentContainer.setVisible(true);
			target.add(commentContainer);
			
			CodeComment prevComment = RevisionDiffPanel.this.getOpenComment();
			if (prevComment != null) {
				SourceAware sourceAware = getSourceAware(prevComment.getMarkPos().getPath());
				if (sourceAware != null) 
					sourceAware.onCommentClosed(target, prevComment);
			} 
			
			MarkPos prevMark = RevisionDiffPanel.this.getMark();
			if (prevMark != null) {
				SourceAware sourceAware = getSourceAware(prevMark.getPath());
				if (sourceAware != null)
					sourceAware.mark(target, null);
			}
			commentSupport.onCommentOpened(target, comment);
		} else {
			clearComment(target);
			commentSupport.onCommentOpened(target, null);
		}
		target.appendJavaScript("gitplex.server.revisionDiff.reposition();");
	}
	
	private @Nullable IModel<Boolean> getBlobBlameModel(BlobChange change) {
		if (blameModel != null) {
			return new IModel<Boolean>() {

				@Override
				public void detach() {
				}

				@Override
				public Boolean getObject() {
					return change.getPath().equals(blameModel.getObject());
				}

				@Override
				public void setObject(Boolean object) {
					if (object)
						blameModel.setObject(change.getPath());
					else
						blameModel.setObject(null);
				}
				
			};
		} else {
			return null;
		}
	}
	
	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new WebMarkupContainer("comment", Model.of((MarkPos)null)) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.server.revisionDiff.initComment();"));
			}
			
		};
		commentContainer.setOutputMarkupPlaceholderTag(true);
		
		WebMarkupContainer head = new WebMarkupContainer("head") {
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (commentContainer.isVisible() && event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					pageDataChanged.getHandler().add(this);
				}
			}

		};
		head.setOutputMarkupId(true);
		commentContainer.add(head);
		
		head.add(new DropdownLink("context") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(requestModel.getObject() == null && getOpenComment() != null);
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new RevisionSelector(id, projectModel) {
					
					@Override
					protected void onSelect(AjaxRequestTarget target, String revision) {
						RevisionComparePage.State state = new RevisionComparePage.State();
						CodeComment comment = getOpenComment();
						state.commentId = comment.getId();
						state.mark = comment.getMarkPos();
						state.compareWithMergeBase = false;
						state.leftSide = new ProjectAndRevision(comment.getProject(), 
								comment.getMarkPos().getCommit());
						state.rightSide = new ProjectAndRevision(comment.getProject(), revision);
						state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
						state.whitespaceOption = whitespaceOptionModel.getObject();
						PageParameters params = RevisionComparePage.paramsOf(comment.getProject(), state);
						setResponsePage(RevisionComparePage.class, params);
					}
					
				};
			}
			
		});
		
		head.add(new AjaxLink<Void>("locate") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(AttributeAppender.append("title", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						MarkPos markPos = getMarkPos();
						if (markPos.getRange() != null) 
							return "Locate the text this comment applied to";
						else
							return "Locate the file this comment applied to";
					}
					
				}));
				setOutputMarkupId(true);
			}
			
			private MarkPos getMarkPos() {
				CodeComment comment = getOpenComment();
				if (comment != null) {
					return comment.getMarkPos();
				} else {
					return (MarkPos)commentContainer.getDefaultModelObject();
				}
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				MarkPos markPos = getMarkPos();
				SourceAware sourceAware = getSourceAware(markPos.getPath());
				if (sourceAware != null)
					sourceAware.mark(target, markPos);
				((CommentSupport)commentSupport).onMark(target, markPos);
				target.appendJavaScript(String.format("$('#%s').blur();", getMarkupId()));
			}

		});
		
		head.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				clearComment(target);
				CodeComment comment = getOpenComment();
				if (comment != null) {
					SourceAware sourceAware = getSourceAware(comment.getMarkPos().getPath());
					if (sourceAware != null) 
						sourceAware.onCommentClosed(target, comment);
					commentSupport.onCommentOpened(target, null);
				}
				target.appendJavaScript("gitplex.server.revisionDiff.reposition();");
			}
			
		});

		boolean locatable = false;
		CodeComment comment = getOpenComment();
		if (comment != null) {
			for (BlobChange change: changesAndCountModel.getObject().getChanges()) {
				if (change.getPaths().contains(comment.getMarkPos().getPath())) {
					locatable = true;
					break;
				}
			}
		}
		
		if (locatable) {
			CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, getOpenComment().getId()) {

				@Override
				protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
					RevisionDiffPanel.this.onCommentDeleted(target, comment);
				}
				
				@Override
				protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
					target.add(commentContainer.get("head"));
				}

				@Override
				protected PullRequest getPullRequest() {
					return requestModel.getObject();
				}

				@Override
				protected CompareContext getCompareContext() {
					return RevisionDiffPanel.this.getCompareContext(comment.getMarkPos().getCommit());
				}

			};
			commentContainer.add(commentPanel);
		} else {
			commentContainer.add(new WebMarkupContainer(BODY_ID));
			commentContainer.setVisible(false);
		}
		
		return commentContainer;
	}
	
	private ObjectId getOldCommitId() {
		if (oldRev.equals(ObjectId.zeroId().name().toString())) {
			return ObjectId.zeroId();
		} else {
			return projectModel.getObject().getRevCommit(oldRev);
		}
	}
	
	private ObjectId getNewCommitId() {
		if (newRev.equals(ObjectId.zeroId().name().toString())) {
			return ObjectId.zeroId();
		} else {
			return projectModel.getObject().getRevCommit(newRev);
		}
	}
	
	@Nullable
	private CodeComment getOpenComment() {
		if (commentSupport != null) {
			CodeComment comment = ((CommentSupport)commentSupport).getOpenComment();
			if (comment != null) {
				String commit = comment.getMarkPos().getCommit();
				String oldCommitHash = getOldCommitId().name();
				String newCommitHash = getNewCommitId().name();
				if (commit.equals(oldCommitHash) || commit.equals(newCommitHash))
					return comment;
			}
		}
		return null;
	}
	
	@Nullable
	private MarkPos getMark() {
		if (commentSupport != null) {
			MarkPos mark = commentSupport.getMark();
			if (mark != null) {
				String commit = mark.getCommit();
				String oldCommitHash = getOldCommitId().name();
				String newCommitHash = getNewCommitId().name();
				if (commit.equals(oldCommitHash) || commit.equals(newCommitHash))
					return mark;
			}
		}
		return null;
	}
	
	@Nullable
	private SourceAware getSourceAware(String path) {
		return diffsView.visitChildren(new IVisitor<Component, SourceAware>() {

			@SuppressWarnings("unchecked")
			@Override
			public void component(Component object, IVisit<SourceAware> visit) {
				if (object instanceof ListItem) {
					ListItem<BlobChange> item = (ListItem<BlobChange>) object;
					if (item.getModelObject().getPaths().contains(path)) {
						visit.stop((SourceAware) item.get(DIFF_ID));
					} else {
						visit.dontGoDeeper();
					}
				} 
			}

		});
	}
	
	private void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		clearComment(target);
		SourceAware sourceAware = getSourceAware(comment.getMarkPos().getPath());
		if (sourceAware != null)
			sourceAware.onCommentDeleted(target, comment);
		((CommentSupport)commentSupport).onCommentOpened(target, null);
		target.appendJavaScript("gitplex.server.revisionDiff.reposition();");
		MarkPos mark = getMark();
		if (mark != null) {
			sourceAware = getSourceAware(mark.getPath());
			if (sourceAware != null) {
				sourceAware.mark(target, mark);
			}
		}
	}
	
	private void clearComment(AjaxRequestTarget target) {
		commentContainer.replace(new WebMarkupContainer(BODY_ID));
		commentContainer.setVisible(false);
		target.add(commentContainer);
	}
	
	@Override
	protected void onDetach() {
		commitCommentsModel.detach();
		commentsModel.detach();
		diffEntriesModel.detach();
		changesAndCountModel.detach();
		projectModel.detach();
		requestModel.detach();
		if (blameModel != null)
			blameModel.detach();
		pathFilterModel.detach();
		whitespaceOptionModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new RevisionDiffResourceReference()));
	}
	
	private static class ChangesAndCount {
		
		private final List<BlobChange> changes;
		
		private final int count;
		
		public ChangesAndCount(List<BlobChange> changes, int count) {
			this.changes = changes;
			this.count = count;
		}
		
		/**
		 * Get list of changes we are capable to handle, note that size of this list 
		 * might be less than total number of changes in order not to put heavy 
		 * burden on the system and browser
		 * 
		 * @return
		 * 			list of changes we are capable to handle
		 */
		public List<BlobChange> getChanges() {
			return changes;
		}

		/**
		 * Get total number of changes detected
		 * 
		 * @return
		 * 			total number of changes detected
		 */
		public int getCount() {
			return count;
		}
		
	}

}
