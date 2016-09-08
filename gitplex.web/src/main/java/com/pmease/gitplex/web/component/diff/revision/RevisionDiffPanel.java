package com.pmease.gitplex.web.component.diff.revision;

import java.nio.file.Paths;
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
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
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
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.support.CommentPos;
import com.pmease.gitplex.core.entity.support.CompareContext;
import com.pmease.gitplex.core.entity.support.DepotAndRevision;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.comment.CodeCommentPanel;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.diff.blob.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.blob.SourceAware;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.component.revisionpicker.RevisionSelector;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.pmease.gitplex.web.util.SuggestionUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Make sure to add only one revision diff panel on a page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "gitplex.diff.viewmode";

	private static final String BODY_ID = "body";
	
	private static final String DIFF_ID = "diff";

	private final IModel<Depot> depotModel;
	
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
			return depotModel.getObject().getDiffs(oldRev, newRev);
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
						return depotModel.getObject().getBlob(blobIdent);
					}

	    		};
	    		allChanges.add(change);
	    		changedPaths.addAll(change.getPaths());
			}

			Set<String> markedPaths = new HashSet<>();
			for (CodeComment comment: commentsModel.getObject()) {
				if (!changedPaths.contains(comment.getCommentPos().getPath()) 
						&& !markedPaths.contains(comment.getCommentPos().getPath())) {
					BlobIdent oldBlobIdent = new BlobIdent(oldRev, comment.getCommentPos().getPath(), FileMode.TYPE_FILE);
					BlobIdent newBlobIdent = new BlobIdent(newRev, comment.getCommentPos().getPath(), FileMode.TYPE_FILE);
					allChanges.add(new BlobChange(null, oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()) {

						@Override
						public Blob getBlob(BlobIdent blobIdent) {
							return depotModel.getObject().getBlob(blobIdent);
						}
						
					});
				}
				markedPaths.add(comment.getCommentPos().getPath());
			}
			
			CommentPos mark = getMark();
			if (mark != null && !changedPaths.contains(mark.getPath()) && !markedPaths.contains(mark.getPath())) {
				BlobIdent oldBlobIdent = new BlobIdent(oldRev, mark.getPath(), FileMode.TYPE_FILE);
				BlobIdent newBlobIdent = new BlobIdent(newRev, mark.getPath(), FileMode.TYPE_FILE);
				allChanges.add(new BlobChange(null, oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						return depotModel.getObject().getBlob(blobIdent);
					}
					
				});
				markedPaths.add(mark.getPath());
			}
			
			List<BlobChange> filterChanges = new ArrayList<>();
	    	for (BlobChange change: allChanges) {
	    		if (StringUtils.isNotBlank(pathFilterModel.getObject())) {
		    		String matchWith = pathFilterModel.getObject().toLowerCase().trim();
	    			matchWith = StringUtils.stripStart(matchWith, "/");
	    			matchWith = StringUtils.stripEnd(matchWith, "/");
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
	    				filterChanges.add(change);
	    			} else if (oldPath.startsWith(matchWith + "/") || newPath.startsWith(matchWith + "/")) {
	    				filterChanges.add(change);
	    			} else if (WildcardUtils.matchString(matchWith, oldPath) 
	    					|| WildcardUtils.matchString(matchWith, newPath)){
	    				filterChanges.add(change);
	    			}
	    		} else {
	    			filterChanges.add(change);
	    		}
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
	    						return depotModel.getObject().getBlob(blobIdent);
	    					}

	    	    		};
	    				normalizedChanges.add(normalizedChange);
	    			}
	    		} else {
	    			normalizedChanges.add(change);
	    		}
	    	}

	    	normalizedChanges.sort((change1, change2)
	    			->Paths.get(change1.getPath()).compareTo(Paths.get(change2.getPath())));
	    	
			List<BlobChange> diffChanges = new ArrayList<>();
			if (normalizedChanges.size() > Constants.MAX_DIFF_FILES)
				diffChanges = normalizedChanges.subList(0, Constants.MAX_DIFF_FILES);
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
	    		if (changedLines <= Constants.MAX_SINGLE_FILE_DIFF_LINES) {
		    		totalChangedLines += changedLines;
		    		if (totalChangedLines <= Constants.MAX_DIFF_LINES)
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
			if (commentSupport != null) {
				Collection<CodeComment> comments = GitPlex.getInstance(CodeCommentManager.class)
						.findAll(depotModel.getObject(), getOldCommit(), getNewCommit());
				if (requestModel.getObject() != null) {
					comments.retainAll(requestModel.getObject().getCodeComments());
				}
				return comments;
			} else {
				return new ArrayList<>();
			}
		}
		
	};
	
	private final IModel<List<CodeComment>> commitCommentsModel = new LoadableDetachableModel<List<CodeComment>>() {

		@Override
		protected List<CodeComment> load() {
			List<CodeComment> commitComments = new ArrayList<>();
			for (CodeComment comment: commentsModel.getObject()) {
				if (comment.getCommentPos().getPath() == null)
					commitComments.add(comment);
			}
			return commitComments;
		}
		
	};
	
	private WebMarkupContainer commentContainer;

	private ListView<BlobChange> diffsView;
	
	public RevisionDiffPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			String oldRev, String newRev, IModel<String> pathFilterModel, IModel<WhitespaceOption> whitespaceOptionModel, 
			@Nullable IModel<String> blameModel, @Nullable CommentSupport markSupport) {
		super(id);
		
		this.depotModel = depotModel;
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
				target.appendJavaScript("gitplex.revisionDiff.reposition();");
			}
			
		};
		this.whitespaceOptionModel = whitespaceOptionModel;
		this.commentSupport = markSupport;
		
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

		WebMarkupContainer body = new WebMarkupContainer(BODY_ID) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.revisionDiff.init();"));
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
			protected List<MenuItem> getMenuItems() {
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
									close();
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
			setOfInvolvedPaths.add(comment.getCommentPos().getPath());
		}
		
		List<String> listOfInvolvedPaths = new ArrayList<>(setOfInvolvedPaths);
		listOfInvolvedPaths.sort((path1, path2)->Paths.get(path1).compareTo(Paths.get(path2)));
		
		filterInput.add(new InputAssistBehavior() {
			
			@Override
			protected List<InputCompletion> getSuggestions(InputStatus inputStatus, int count) {
				List<InputCompletion> completions = new ArrayList<>();
				for (InputSuggestion suggestion: SuggestionUtils.suggestPath(listOfInvolvedPaths, 
						inputStatus.getContentBeforeCaret().trim(), count)) {
					int caret = suggestion.getCaret();
					if (caret == -1)
						caret = suggestion.getContent().length();
					InputCompletion completion = new InputCompletion(0, inputStatus.getContent().length(), 
							suggestion.getContent(), caret, suggestion.getLabel(), 
							null, suggestion.getMatchRange());
					completions.add(completion);
				}
				return completions;
			}
			
			@Override
			protected List<String> getHints(InputStatus inputStatus) {
				return Lists.newArrayList("Use * to match any string in the path");
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
					iconClass = " fa fa-square-o";
				} else if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY)
					iconClass = " fa-ext fa-diff-added";
				else if (change.getType() == ChangeType.DELETE)
					iconClass = " fa-ext fa-diff-removed";
				else if (change.getType() == ChangeType.MODIFY)
					iconClass = " fa-ext fa-diff-modified";
				else
					iconClass = " fa-ext fa-diff-renamed";
				
				item.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", iconClass)));
				
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
					item.add(new BlobDiffPanel(DIFF_ID, depotModel, requestModel, change, diffMode, 
							getBlobBlameModel(change), new BlobCommentSupport() {
	
						@Override
						public CommentPos getMark() {
							CommentPos mark = RevisionDiffPanel.this.getMark();
							if (mark != null && change.getPaths().contains(mark.getPath()))
								return mark;
							else
								return null;
						}
	
						@Override
						public String getMarkUrl(CommentPos mark) {
							return commentSupport.getMarkUrl(mark);
						}
	
						@Override
						public CodeComment getOpenComment() {
							CodeComment comment = RevisionDiffPanel.this.getOpenComment();
							if (comment != null && change.getPaths().contains(comment.getCommentPos().getPath()))
								return comment;
							else
								return null;
						}
	
						@Override
						public void onOpenComment(AjaxRequestTarget target, CodeComment comment) {
							RevisionDiffPanel.this.onOpenComment(target, comment);
							send(RevisionDiffPanel.this, Broadcast.BREADTH, new CodeCommentToggled(target));
						}
	
						@Override
						public void onAddComment(AjaxRequestTarget target, CommentPos commentPos) {
							commentContainer.setDefaultModelObject(commentPos);
							
							Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", RevisionDiffPanel.this);
							fragment.setOutputMarkupId(true);
							
							Form<?> form = new Form<Void>("form");
							
							String uuid = UUID.randomUUID().toString();
							
							TextField<String> titleInput = new TextField<String>("title", Model.of(""));
							titleInput.setRequired(true);
							form.add(titleInput);
							CommentInput contentInput;
							form.add(contentInput = new CommentInput("content", Model.of("")) {

								@Override
								protected DepotAttachmentSupport getAttachmentSupport() {
									return new DepotAttachmentSupport(depotModel.getObject(), uuid);
								}

								@Override
								protected Depot getDepot() {
									return depotModel.getObject();
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
									target.appendJavaScript("gitplex.revisionDiff.reposition();");
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
									
									Depot depot = depotModel.getObject();
									CodeComment comment = new CodeComment();
									comment.setUUID(uuid);
									comment.setDepot(depot);
									comment.setUser(SecurityUtils.getAccount());
									comment.setCommentPos(commentPos);
									comment.setCompareContext(getCompareContext(comment.getCommentPos().getCommit()));
									comment.setTitle(titleInput.getModelObject());
									comment.setContent(contentInput.getModelObject());
									Ref branchRef = depot.getBranchRef(newRev);									
									if (branchRef != null)
										comment.setBranchRef(branchRef.getName());
									
									GitPlex.getInstance(CodeCommentManager.class).save(comment, requestModel.getObject());
									
									CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), comment.getId()) {

										@Override
										protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
											RevisionDiffPanel.this.onCommentDeleted(target, comment);
										}
										
										@Override
										protected CompareContext getCompareContext(CodeComment comment) {
											return RevisionDiffPanel.this.getCompareContext(comment.getCommentPos().getCommit());
										}

										@Override
										protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
											target.add(commentContainer.get("head"));
										}

										@Override
										protected PullRequest getPullRequest() {
											return requestModel.getObject();
										}
										
									};
									commentContainer.replace(commentPanel);
									target.add(commentContainer);
									
									SourceAware sourceAware = getSourceAware(comment.getCommentPos().getPath());
									if (sourceAware != null) 
										sourceAware.onCommentAdded(target, comment);

									commentSupport.onCommentOpened(target, comment);
									target.appendJavaScript("gitplex.revisionDiff.reposition();");
									send(RevisionDiffPanel.this, Broadcast.BREADTH, new CodeCommentToggled(target));
								}

							});
							fragment.add(form);
							
							commentContainer.replace(fragment);
							commentContainer.setVisible(true);
							target.add(commentContainer);
							
							CommentPos prevMark = RevisionDiffPanel.this.getMark();
							if (prevMark != null) {
								SourceAware sourceAware = getSourceAware(prevMark.getPath());
								if (sourceAware != null) 
									sourceAware.mark(target, null);
							}
							
							CodeComment prevComment = RevisionDiffPanel.this.getOpenComment();
							if (prevComment != null) {
								SourceAware sourceAware = getSourceAware(prevComment.getCommentPos().getPath());
								if (sourceAware != null) 
									sourceAware.onCommentClosed(target, prevComment);
							}  
							commentSupport.onAddComment(target, commentPos);
							target.appendJavaScript("gitplex.revisionDiff.reposition();");		
							send(RevisionDiffPanel.this, Broadcast.BREADTH, new CodeCommentToggled(target));
						}

						@Override
						public Collection<CodeComment> getComments() {
							Collection<CodeComment> comments = new ArrayList<>();
							for (CodeComment comment: commentsModel.getObject()) {
								if (change.getPaths().contains(comment.getCommentPos().getPath())) {
									comments.add(comment);
								}
							}
							return comments;
						}

						@Override
						public Component getDirtyContainer() {
							return commentContainer;
						}

					}));
				} else {
					item.add(new BlobDiffPanel(DIFF_ID, depotModel, requestModel, change, 
							diffMode, getBlobBlameModel(change), null));
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}

	private void onOpenComment(AjaxRequestTarget target, CodeComment comment) {
		CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, comment.getId()) {

			@Override
			protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
				RevisionDiffPanel.this.onCommentDeleted(target, comment);
			}

			@Override
			protected CompareContext getCompareContext(CodeComment comment) {
				return RevisionDiffPanel.this.getCompareContext(comment.getCommentPos().getCommit());
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
				target.add(commentContainer.get("head"));
			}

			@Override
			protected PullRequest getPullRequest() {
				return requestModel.getObject();
			}
			
		};
		
		commentContainer.replace(commentPanel);
		commentContainer.setVisible(true);
		target.add(commentContainer);
		
		CodeComment prevComment = RevisionDiffPanel.this.getOpenComment();
		if (prevComment != null) {
			SourceAware sourceAware = getSourceAware(prevComment.getCommentPos().getPath());
			if (sourceAware != null) 
				sourceAware.onCommentClosed(target, prevComment);
		} 
		
		CommentPos prevMark = RevisionDiffPanel.this.getMark();
		if (prevMark != null) {
			SourceAware sourceAware = getSourceAware(prevMark.getPath());
			if (sourceAware != null)
				sourceAware.mark(target, null);
		}
		
		commentSupport.onCommentOpened(target, comment);
		target.appendJavaScript("gitplex.revisionDiff.reposition();");
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
		WebMarkupContainer commentContainer = new WebMarkupContainer("comment", Model.of((CommentPos)null)) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.revisionDiff.initComment();"));
			}
			
		};
		commentContainer.setOutputMarkupPlaceholderTag(true);
		
		WebMarkupContainer head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		commentContainer.add(head);
		
		head.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				CodeComment comment = getOpenComment();
				return comment!=null?comment.getTitle():"";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getOpenComment() != null);
			}
			
		});
		
		head.add(new DropdownLink("context") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getOpenComment() != null);
			}

			@Override
			protected Component newContent(String id) {
				if (requestModel.getObject() != null) {
					Fragment fragment = new Fragment(id, "requestCommitSelectorFrag", RevisionDiffPanel.this);
					IModel<List<RevCommit>> commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

						@Override
						protected List<RevCommit> load() {
							List<RevCommit> commits = new ArrayList<>();
							PullRequest request = requestModel.getObject();
							commits.add(request.getBaseCommit());
							commits.addAll(request.getCommits());
							return commits;
						}
						
					};
					fragment.add(new ListView<RevCommit>("commits", commitsModel) {

						@Override
						protected void populateItem(ListItem<RevCommit> item) {
							RevCommit commit = item.getModelObject();
							AjaxLink<Void> link = new AjaxLink<Void>("link") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									RequestChangesPage.State state = new RequestChangesPage.State();
									CodeComment comment = getOpenComment();
									state.commentId = comment.getId();
									state.mark = comment.getCommentPos();
									int index = commitsModel.getObject().stream().map(RevCommit::getName).collect(Collectors.toList())
											.indexOf(comment.getCommentPos().getCommit());
									int compareIndex = commitsModel.getObject().indexOf(commit);
									if (index < compareIndex) {
										state.oldCommit = comment.getCommentPos().getCommit();
										state.newCommit = commit.name();
									} else {
										state.oldCommit = commit.name();
										state.newCommit = comment.getCommentPos().getCommit();
									}
									state.pathFilter = pathFilterModel.getObject();
									state.whitespaceOption = whitespaceOptionModel.getObject();
									PageParameters params = RequestChangesPage.paramsOf(requestModel.getObject(), state);
									setResponsePage(RequestChangesPage.class, params);
								}
								
							};
							link.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
							link.add(new Label("subject", commit.getShortMessage()));
							if (commit.name().equals(getOpenComment().getCommentPos().getCommit())) {
								link.setEnabled(false);
								link.add(AttributeAppender.append("class", "commented"));
								link.add(new WebMarkupContainer("commented"));
							} else {
								link.add(new WebMarkupContainer("commented").setVisible(false));
							}
							item.add(link);
						}
						
					});
					return fragment;
				} else {
					return new RevisionSelector(id, new AbstractReadOnlyModel<Depot>() {

						@Override
						public Depot getObject() {
							return getOpenComment().getDepot();
						}
						
					}) {
						
						@Override
						protected void onSelect(AjaxRequestTarget target, String revision) {
							RevisionComparePage.State state = new RevisionComparePage.State();
							CodeComment comment = getOpenComment();
							state.commentId = comment.getId();
							state.mark = comment.getCommentPos();
							state.compareWithMergeBase = false;
							state.leftSide = new DepotAndRevision(comment.getDepot(), comment.getCommentPos().getCommit());
							state.rightSide = new DepotAndRevision(comment.getDepot(), revision);
							state.tabPanel = RevisionComparePage.TabPanel.CHANGES;
							PageParameters params = RevisionComparePage.paramsOf(comment.getDepot(), state);
							setResponsePage(RevisionComparePage.class, params);
						}
						
					};
				}
			}
			
		});

		head.add(new AjaxLink<Void>("locate") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(AttributeAppender.append("title", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						CommentPos commentPos = getCommentPos();
						if (commentPos.getRange() != null) 
							return "Locate the text this comment applied to";
						else
							return "Locate the file this comment applied to";
					}
					
				}));
				setOutputMarkupId(true);
			}
			
			private CommentPos getCommentPos() {
				CodeComment comment = getOpenComment();
				if (comment != null) {
					return comment.getCommentPos();
				} else {
					return (CommentPos)commentContainer.getDefaultModelObject();
				}
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				CommentPos commentPos = getCommentPos();
				SourceAware sourceAware = getSourceAware(commentPos.getPath());
				if (sourceAware != null)
					sourceAware.mark(target, commentPos);
				commentSupport.onMark(target, commentPos);
				target.appendJavaScript(String.format("$('#%s').blur();", getMarkupId()));
			}

		});
		
		// use this instead of bookmarkable link as we want to get the link 
		// updated whenever we re-render the comment container
		AttributeAppender appender = AttributeAppender.append("href", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (getOpenComment() != null) {
					return commentSupport.getCommentUrl(getOpenComment());
				} else {
					return "";
				}
			}
			
		});
		
		head.add(new WebMarkupContainer("permanent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getOpenComment() != null);
			}
			
		}.add(appender));
		
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
					SourceAware sourceAware = getSourceAware(comment.getCommentPos().getPath());
					if (sourceAware != null) 
						sourceAware.onCommentClosed(target, comment);
					commentSupport.onCommentOpened(target, null);
				}
				target.appendJavaScript("gitplex.revisionDiff.reposition();");
				
				CommentPos mark = getMark();
				if (mark != null) {
					SourceAware sourceAware = getSourceAware(mark.getPath());
					if (sourceAware != null) {
						sourceAware.mark(target, mark);
					}
				}
				send(RevisionDiffPanel.this, Broadcast.BREADTH, new CodeCommentToggled(target));
			}
			
		});

		head.add(new AjaxLink<Void>("toggleResolve") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getOpenComment() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				CodeComment comment = getOpenComment();
				if (comment != null) {
					if (comment.isResolved()) {
						tag.put("title", "Comment is currently resolved, click to unresolve");
						tag.put("class", "pull-right resolve resolved");
					} else {
						tag.put("title", "Comment is currently unresolved, click to resolve");
						tag.put("class", "pull-right resolve unresolved");
					}
				} 
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (SecurityUtils.getAccount() != null) {
					((CodeCommentPanel)commentContainer.get("body")).onChangeStatus(target);
					target.appendJavaScript("gitplex.revisionDiff.scrollToCommentBottom();");
				} else {
					Session.get().warn("Please login to resolve/unresolve comment");
				}
			}
			
		}.setOutputMarkupId(true));
		
		boolean locatable = false;
		CodeComment comment = getOpenComment();
		if (comment != null) {
			for (BlobChange change: changesAndCountModel.getObject().getChanges()) {
				if (change.getPaths().contains(comment.getCommentPos().getPath())) {
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
				protected CompareContext getCompareContext(CodeComment comment) {
					return RevisionDiffPanel.this.getCompareContext(comment.getCommentPos().getCommit());
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
					target.add(commentContainer.get("head"));
				}

				@Override
				protected PullRequest getPullRequest() {
					return requestModel.getObject();
				}
				
			};
			commentContainer.add(commentPanel);
		} else {
			commentContainer.add(new WebMarkupContainer(BODY_ID));
			commentContainer.setVisible(false);
		}

		return commentContainer;
	}
	
	private RevCommit getOldCommit() {
		return depotModel.getObject().getRevCommit(oldRev);
	}
	
	private RevCommit getNewCommit() {
		return depotModel.getObject().getRevCommit(newRev);
	}
	
	@Nullable
	private CodeComment getOpenComment() {
		if (commentSupport != null) {
			CodeComment comment = commentSupport.getOpenComment();
			if (comment != null) {
				String commit = comment.getCommentPos().getCommit();
				String oldCommitHash = getOldCommit().name();
				String newCommitHash = getNewCommit().name();
				if (commit.equals(oldCommitHash) || commit.equals(newCommitHash))
					return comment;
			}
		}
		return null;
	}
	
	@Nullable
	private CommentPos getMark() {
		if (commentSupport != null) {
			CommentPos mark = commentSupport.getMark();
			if (mark != null) {
				String commit = mark.getCommit();
				String oldCommitHash = getOldCommit().name();
				String newCommitHash = getNewCommit().name();
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
		SourceAware sourceAware = getSourceAware(comment.getCommentPos().getPath());
		if (sourceAware != null)
			sourceAware.onCommentDeleted(target, comment);
		commentSupport.onCommentOpened(target, null);
		target.appendJavaScript("gitplex.revisionDiff.reposition();");
		CommentPos mark = getMark();
		if (mark != null) {
			sourceAware = getSourceAware(mark.getPath());
			if (sourceAware != null) {
				sourceAware.mark(target, mark);
			}
		}
		send(RevisionDiffPanel.this, Broadcast.BREADTH, new CodeCommentToggled(target));
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
		depotModel.detach();
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
	
	private CompareContext getCompareContext(String commitHash) {
		CompareContext compareContext = new CompareContext();
		String oldCommitHash = getOldCommit().name();
		String newCommitHash = getNewCommit().name();
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
