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
import org.apache.wicket.markup.head.CssHeaderItem;
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
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.git.Blob;
import com.pmease.commons.git.BlobChange;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.diff.DiffUtils;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.util.Range;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.assets.jqueryui.JQueryUIResourceReference;
import com.pmease.commons.wicket.behavior.inputassist.InputAssistBehavior;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.CompareContext;
import com.pmease.gitplex.core.entity.component.Mark;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.comment.CodeCommentPanel;
import com.pmease.gitplex.web.component.comment.CommentInput;
import com.pmease.gitplex.web.component.comment.DepotAttachmentSupport;
import com.pmease.gitplex.web.component.diff.blob.BlobDiffPanel;
import com.pmease.gitplex.web.component.diff.blob.MarkAware;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.util.SuggestionUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Make sure to add only one revision diff panel on a page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public abstract class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "gitplex.diff.viewmode";

	private static final String BODY_ID = "body";
	
	private static final String DIFF_ID = "diff";

	private final IModel<Depot> depotModel;
	
	private final IModel<PullRequest> requestModel;

	private final String oldRev;
	
	private final String newRev;
	
	private final MarkSupport markSupport;
	
	private String pathFilter;
	
	private WhitespaceOption whitespaceOption;
	
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
    			BlobChange change = new BlobChange(oldRev, newRev, entry, whitespaceOption) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						return depotModel.getObject().getBlob(blobIdent);
					}

	    		};
	    		allChanges.add(change);
	    		changedPaths.addAll(change.getPaths());
			}

			Set<String> markedPaths = new HashSet<>();
			if (markSupport != null) {
				for (CodeComment comment: commentsModel.getObject()) {
					if (!changedPaths.contains(comment.getPath()) 
							&& !markedPaths.contains(comment.getPath())) {
						BlobIdent oldBlobIdent = new BlobIdent(oldRev, comment.getPath(), FileMode.TYPE_FILE);
						BlobIdent newBlobIdent = new BlobIdent(newRev, comment.getPath(), FileMode.TYPE_FILE);
						allChanges.add(new BlobChange(null, oldBlobIdent, newBlobIdent, whitespaceOption) {

							@Override
							public Blob getBlob(BlobIdent blobIdent) {
								return depotModel.getObject().getBlob(blobIdent);
							}
							
						});
					}
					markedPaths.add(comment.getPath());
				}
				
				DiffMark mark = markSupport.getMark();
				if (mark != null) {
					BlobIdent oldBlobIdent = new BlobIdent(oldRev, mark.getPath(), FileMode.TYPE_FILE);
					BlobIdent newBlobIdent = new BlobIdent(newRev, mark.getPath(), FileMode.TYPE_FILE);
					allChanges.add(new BlobChange(null, oldBlobIdent, newBlobIdent, whitespaceOption) {

						@Override
						public Blob getBlob(BlobIdent blobIdent) {
							return depotModel.getObject().getBlob(blobIdent);
						}
						
					});
					markedPaths.add(mark.getPath());
				}
			}
			
			List<BlobChange> filterChanges = new ArrayList<>();
	    	for (BlobChange change: allChanges) {
	    		if (StringUtils.isNotBlank(pathFilter)) {
		    		String matchWith = pathFilter.toLowerCase().trim();
	    			matchWith = StringUtils.stripStart(matchWith, "/");
	    			matchWith = StringUtils.stripEnd(matchWith, "/");
	    			String oldPath = change.getOldBlobIdent().path;
	    			if (oldPath == null)
	    				oldPath = "";
	    			else
	    				oldPath = oldPath.toLowerCase();
	    			String newPath = change.getNewBlobIdent().path.toLowerCase();
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
	        					oldBlobIdent, newBlobIdent, whitespaceOption) {

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
			Depot depot = depotModel.getObject();
			return GitPlex.getInstance(CodeCommentManager.class).query(
					depot, depot.getRevCommit(oldRev), depot.getRevCommit(newRev));
		}
		
	};
	
	private WebMarkupContainer commentContainer;
	
	private ListView<BlobChange> diffsView;
	
	public RevisionDiffPanel(String id, IModel<Depot> depotModel, IModel<PullRequest> requestModel, 
			String oldRev, String newRev, @Nullable String pathFilter, 
			WhitespaceOption whitespaceOption, @Nullable MarkSupport markSupport) {
		super(id);
		
		this.depotModel = depotModel;
		this.requestModel = requestModel;
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.pathFilter = pathFilter;
		this.whitespaceOption = whitespaceOption;
		this.markSupport = markSupport;
		
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
							if (whitespaceOption == each)
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
									whitespaceOption = each;
									target.add(body);
									onWhitespaceOptionChange(target, whitespaceOption);
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
		
		Form<?> form = new Form<Void>("pathFilter");
		TextField<String> filterInput;
		form.add(filterInput = new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return pathFilter;
			}

			@Override
			public void setObject(String object) {
				pathFilter = object;
			}
			
		}));
		
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
			setOfInvolvedPaths.add(comment.getPath());
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
		
		form.add(new AjaxButton("submit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(body);
				onPathFilterChange(target, pathFilter);
			}
			
		});
		add(form);

		commentContainer = new WebMarkupContainer("comment", Model.of((DiffMark)null)) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("gitplex.revisionDiff.initComment();"));
			}
			
		};
		commentContainer.setOutputMarkupPlaceholderTag(true);
		
		body.add(commentContainer);
		commentContainer.add(new AjaxLink<Void>("locate") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				setOutputMarkupId(true);
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				CodeComment comment = markSupport.getOpenComment();
				DiffMark mark;
				if (comment != null) {
					mark = new DiffMark(comment, getOldCommit().name(), getNewCommit().name());
				} else {
					mark = (DiffMark)commentContainer.getDefaultModelObject();
				}
				MarkAware markAware = getMarkAware(mark.getPath());
				if (markAware != null)
					markAware.mark(target, mark);
				markSupport.onMark(target, mark);
				target.appendJavaScript(String.format("$('#%s').blur();", getMarkupId()));
			}

		});
		
		// use this instead of bookmarkable link as we want to get the link 
		// updated whenever we re-render the comment container
		AttributeAppender appender = AttributeAppender.append("href", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (markSupport.getOpenComment() != null) {
					return markSupport.getCommentUrl(markSupport.getOpenComment());
				} else {
					return "";
				}
			}
			
		});
		
		commentContainer.add(new WebMarkupContainer("permanent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(markSupport.getOpenComment() != null);
			}
			
		}.add(appender));
		
		commentContainer.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				clearComment(target);
				CodeComment comment = markSupport.getOpenComment();
				if (comment != null) {
					MarkAware markAware = getMarkAware(comment.getPath());
					if (markAware != null) 
						markAware.onCommentClosed(target, comment);
					markSupport.onCommentClosed(target);
				}
				target.appendJavaScript("gitplex.revisionDiff.reposition();");
				
				DiffMark mark = markSupport.getMark();
				if (mark != null) {
					MarkAware markAware = getMarkAware(mark.getPath());
					if (markAware != null) {
						markAware.mark(target, mark);
					}
				}
			}
			
		});
		if (markSupport != null && markSupport.getOpenComment() != null) {
			IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

				@Override
				protected CodeComment load() {
					return markSupport.getOpenComment();
				}
				
			};
			CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, commentModel) {

				@Override
				protected void onCommentDeleted(AjaxRequestTarget target) {
					CodeComment comment = commentModel.getObject();
					RevisionDiffPanel.this.onCommentDeleted(target, comment);
				}
				
			};
			commentContainer.add(commentPanel);
		} else {
			commentContainer.add(new WebMarkupContainer(BODY_ID));
			commentContainer.setVisible(false);
		}
				
		Component totalChangedLink;
		body.add(totalChangedLink = new Label("totalChanged", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return changesAndCountModel.getObject().getChanges().size() + " files ";
			}
			
		}));

		body.add(new WebMarkupContainer("tooManyChanges") {

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
			totalChangedLink.add(AttributeAppender.append("class", "expanded"));			
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

				boolean hasComments = false;
				for (CodeComment comment: commentsModel.getObject()) {
					if (change.getPaths().contains(comment.getPath())) {
						hasComments = true;
						break;
					}
				}
				
				item.add(new WebMarkupContainer("hasComments").setVisible(hasComments));
				
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
				if (markSupport != null) {
					item.add(new BlobDiffPanel(DIFF_ID, depotModel, requestModel, change, diffMode, new BlobMarkSupport() {
	
						@Override
						public DiffMark getMark() {
							DiffMark mark = markSupport.getMark();
							if (mark != null && change.getPaths().contains(mark.getPath()))
								return mark;
							else
								return null;
						}
	
						@Override
						public String getMarkUrl(DiffMark mark) {
							return markSupport.getMarkUrl(mark);
						}
	
						@Override
						public CodeComment getOpenComment() {
							CodeComment comment = markSupport.getOpenComment();
							if (comment != null && change.getPaths().contains(comment.getPath()))
								return comment;
							else
								return null;
						}
	
						@Override
						public void onOpenComment(AjaxRequestTarget target, CodeComment comment) {
							Long commentId = comment.getId();
							IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

								@Override
								protected CodeComment load() {
									return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
								}
								
							};
							
							CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, commentModel) {

								@Override
								protected void onCommentDeleted(AjaxRequestTarget target) {
									CodeComment comment = commentModel.getObject();
									RevisionDiffPanel.this.onCommentDeleted(target, comment);
								}
								
							};
							
							commentContainer.replace(commentPanel);
							commentContainer.setVisible(true);
							target.add(commentContainer);
							
							CodeComment prevComment = markSupport.getOpenComment();
							if (prevComment != null) {
								MarkAware markAware = getMarkAware(prevComment.getPath());
								if (markAware != null) 
									markAware.onCommentClosed(target, prevComment);
							} 
							
							DiffMark prevMark = markSupport.getMark();
							if (prevMark != null) {
								MarkAware markAware = getMarkAware(prevMark.getPath());
								if (markAware != null)
									markAware.clearMark(target);
							}
							
							markSupport.onCommentOpened(target, comment);
							target.appendJavaScript("gitplex.revisionDiff.reposition();");
						}
	
						@Override
						public void onAddComment(AjaxRequestTarget target, DiffMark mark) {
							commentContainer.setDefaultModelObject(mark);
							
							Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", RevisionDiffPanel.this);
							fragment.setOutputMarkupId(true);
							
							Form<?> form = new Form<Void>("form");
							
							CommentInput input;
							form.add(input = new CommentInput("input", Model.of("")) {

								@Override
								protected DepotAttachmentSupport getAttachmentSupport() {
									return new DepotAttachmentSupport(depotModel.getObject());
								}
								
							});
							input.setRequired(true);
							
							NotificationPanel feedback = new NotificationPanel("feedback", input); 
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
									
									CodeComment comment = new CodeComment();
									CompareContext compareContext = new CompareContext();
									Depot depot = depotModel.getObject();
									String oldCommitHash = depot.getRevCommit(oldRev).name();
									String newCommitHash = depot.getRevCommit(newRev).name();
									if (mark.isLeftSide()) {
										comment.setCommit(oldCommitHash);
										compareContext.setCompareCommit(newCommitHash);
										compareContext.setLeftSide(false);
									} else {
										comment.setCommit(newCommitHash);
										compareContext.setCompareCommit(oldCommitHash);
										compareContext.setLeftSide(true);
									}
									compareContext.setPathFilter(pathFilter);
									compareContext.setWhitespaceOption(whitespaceOption);
									comment.setCompareContext(compareContext);
									comment.setPath(mark.getPath());
									comment.setContent(input.getModelObject());
									comment.setDepot(depotModel.getObject());
									comment.setUser(SecurityUtils.getAccount());
									comment.setMark(new Mark(mark.getBeginLine(), mark.getBeginChar(), 
											mark.getEndLine(), mark.getEndChar()));
									GitPlex.getInstance(CodeCommentManager.class).persist(comment);
									
									Long commentId = comment.getId();
									IModel<CodeComment> commentModel = new LoadableDetachableModel<CodeComment>() {

										@Override
										protected CodeComment load() {
											return GitPlex.getInstance(CodeCommentManager.class).load(commentId);
										}
										
									};
									CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), commentModel) {

										@Override
										protected void onCommentDeleted(AjaxRequestTarget target) {
											CodeComment comment = commentModel.getObject();
											RevisionDiffPanel.this.onCommentDeleted(target, comment);
										}
										
									};
									commentContainer.replace(commentPanel);
									target.add(commentContainer);
									
									MarkAware markAware = getMarkAware(comment.getPath());
									if (markAware != null) 
										markAware.onCommentAdded(target, comment);

									markSupport.onCommentOpened(target, comment);
									target.appendJavaScript("gitplex.revisionDiff.reposition();");
								}

							});
							fragment.add(form);
							
							commentContainer.replace(fragment);
							commentContainer.setVisible(true);
							target.add(commentContainer);
							
							DiffMark prevMark = markSupport.getMark();
							if (prevMark != null) {
								MarkAware markAware = getMarkAware(prevMark.getPath());
								if (markAware != null) 
									markAware.clearMark(target);
							}
							
							CodeComment prevComment = markSupport.getOpenComment();
							if (prevComment != null) {
								MarkAware markAware = getMarkAware(prevComment.getPath());
								if (markAware != null) 
									markAware.onCommentClosed(target, prevComment);
							}  
							markSupport.onAddComment(target, mark);
							target.appendJavaScript("gitplex.revisionDiff.reposition();");
						}

						@Override
						public Collection<CodeComment> getComments() {
							Collection<CodeComment> comments = new ArrayList<>();
							for (CodeComment comment: commentsModel.getObject()) {
								if (change.getPaths().contains(comment.getPath())) {
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
					item.add(new BlobDiffPanel(DIFF_ID, depotModel, requestModel, change, diffMode, null));
				}
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	private MarkAware getMarkAware(String path) {
		return diffsView.visitChildren(new IVisitor<Component, MarkAware>() {

			@SuppressWarnings("unchecked")
			@Override
			public void component(Component object, IVisit<MarkAware> visit) {
				if (object instanceof ListItem) {
					ListItem<BlobChange> item = (ListItem<BlobChange>) object;
					if (item.getModelObject().getPaths().contains(path)) {
						visit.stop((MarkAware) item.get(DIFF_ID));
					} else {
						visit.dontGoDeeper();
					}
				} 
			}

		});
	}
	
	private RevCommit getOldCommit() {
		return depotModel.getObject().getRevCommit(oldRev);
	}
	
	private RevCommit getNewCommit() {
		return depotModel.getObject().getRevCommit(newRev);
	}
	
	private void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		clearComment(target);
		MarkAware markAware = getMarkAware(comment.getPath());
		if (markAware != null)
			markAware.onCommentDeleted(target, comment);
		markSupport.onCommentClosed(target);
		target.appendJavaScript("gitplex.revisionDiff.reposition();");
		DiffMark mark = markSupport.getMark();
		if (mark != null) {
			markAware = getMarkAware(mark.getPath());
			if (markAware != null) {
				markAware.mark(target, mark);
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
		commentsModel.detach();
		diffEntriesModel.detach();
		changesAndCountModel.detach();
		depotModel.detach();
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(JQueryUIResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RevisionDiffPanel.class, "revision-diff.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionDiffPanel.class, "revision-diff.css")));
	}
	
	protected abstract void onPathFilterChange(AjaxRequestTarget target, String pathFilter);
	
	protected abstract void onWhitespaceOptionChange(AjaxRequestTarget target, 
			WhitespaceOption whitespaceOption);
	
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
