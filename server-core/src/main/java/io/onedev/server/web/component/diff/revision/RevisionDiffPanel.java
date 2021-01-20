package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;
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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
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
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.LineCoverage;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.search.code.CommitIndexed;
import io.onedev.server.search.code.IndexManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.PathComparator;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.codecomment.CodeCommentPanel;
import io.onedev.server.web.component.diff.blob.BlobDiffPanel;
import io.onedev.server.web.component.diff.blob.text.TextDiffPanel;
import io.onedev.server.web.component.diff.diffstat.DiffStatBar;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.DiffPlanarRange;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.util.SuggestionUtils;

/**
 * Make sure to add only one revision diff panel on a page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "onedev.server.diff.viewmode";

	private static final String BODY_ID = "body";
	
	private static final String DIFF_ID = "diff";

	private final IModel<Project> projectModel;
	
	private final IModel<PullRequest> requestModel;

	private final String oldRev;
	
	private final String newRev;

	private final IModel<String> blameModel;
	
	private final AnnotationSupport annotationSupport;
	
	private final IModel<String> pathFilterModel;
	
	private final IModel<WhitespaceOption> whitespaceOptionModel;
	
	private DiffViewMode diffMode;
	
	private IModel<List<DiffEntry>> diffEntriesModel = new LoadableDetachableModel<List<DiffEntry>>() {

		@Override
		protected List<DiffEntry> load() {
			AnyObjectId oldRevId = projectModel.getObject().getObjectId(oldRev, true);
			AnyObjectId newRevId = projectModel.getObject().getObjectId(newRev, true);
			return GitUtils.diff(projectModel.getObject().getRepository(), oldRevId, newRevId);
		}
		
	};
	
	private IModel<ChangesAndCount> changesAndCountModel = new LoadableDetachableModel<ChangesAndCount>() {

		@Override
		protected ChangesAndCount load() {
			List<DiffEntry> diffEntries = diffEntriesModel.getObject();
			
			List<BlobChange> changes = new ArrayList<>();
			for (DiffEntry entry: diffEntries) {
    			BlobChange change = new BlobChange(oldRev, newRev, entry, whitespaceOptionModel.getObject()) {

					@Override
					public Blob getBlob(BlobIdent blobIdent) {
						return projectModel.getObject().getBlob(blobIdent, true);
					}

	    		};
	    		changes.add(change);
			}
			
			List<BlobChange> filteredChanges = new ArrayList<>();
    		String patternSetString = pathFilterModel.getObject();
    		if (StringUtils.isNotBlank(patternSetString)) {
    			try {
    				PatternSet patternSet = PatternSet.parse(patternSetString.toLowerCase());
    				Matcher matcher = new PathMatcher();
    				for (BlobChange change: changes) {
	        			String oldPath = change.getOldBlobIdent().path;
	        			if (oldPath != null)
	        				oldPath = oldPath.toLowerCase();
	        			String newPath = change.getNewBlobIdent().path;
	        			if (newPath != null)
	        				newPath = newPath.toLowerCase();
    					if (oldPath != null && patternSet.matches(matcher, oldPath) 
    							|| newPath != null && patternSet.matches(matcher, newPath)) {
    						filteredChanges.add(change);
    					}
    				}
    			} catch (Exception e) {
    				error("Malformed path filter");
    			}
    		} else {
    			filteredChanges.addAll(changes);
    		}
			
	    	// for some unknown reason, some paths in the diff entries is DELETE/ADD 
	    	// pair instead MODIFICATION, here we normalize those as a single 
	    	// MODIFICATION entry
	    	Map<String, BlobIdent> deleted = new HashMap<>();
	    	Map<String, BlobIdent> added = new HashMap<>();
	    	for (BlobChange change: filteredChanges) {
	    		if (change.getType() == ChangeType.DELETE)
	    			deleted.put(change.getPath(), change.getOldBlobIdent());
	    		else if (change.getType() == ChangeType.ADD) 
	    			added.put(change.getPath(), change.getNewBlobIdent());
	    	}
	    	
	    	List<BlobChange> normalizedChanges = new ArrayList<>();
	    	for (BlobChange change: filteredChanges) {
	    		BlobIdent oldBlobIdent = deleted.get(change.getPath());
	    		BlobIdent newBlobIdent = added.get(change.getPath());
	    		if (oldBlobIdent != null && newBlobIdent != null) {
	    			if (change.getType() == ChangeType.DELETE) {
	        			BlobChange normalizedChange = new BlobChange(ChangeType.MODIFY, 
	        					oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()) {

	    					@Override
	    					public Blob getBlob(BlobIdent blobIdent) {
	    						return projectModel.getObject().getBlob(blobIdent, true);
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
	    	for (Future<Void> future: OneDev.getInstance(ForkJoinPool.class).invokeAll(tasks)) {
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
		    				&& change.getAdditions() + change.getDeletions() == 0) {
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
	    		if (changedLines <= WebConstants.MAX_SINGLE_DIFF_LINES) {
		    		totalChangedLines += changedLines;
		    		if (totalChangedLines <= WebConstants.MAX_TOTAL_DIFF_LINES)
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
	
	private WebMarkupContainer commentContainer;

	private ListView<BlobChange> diffsView;
	
	private WebMarkupContainer body;
	
	public RevisionDiffPanel(String id, IModel<Project> projectModel, IModel<PullRequest> requestModel, 
			String oldRev, String newRev, IModel<String> pathFilterModel, IModel<WhitespaceOption> whitespaceOptionModel, 
			@Nullable IModel<String> blameModel, @Nullable AnnotationSupport annotationSupport) {
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
					BlobDiffPanel blobDiffPanel = getBlobDiffPanel(prevBlameFile);
					blobDiffPanel.onUnblame(target);
				}
			}
			
		};
		this.whitespaceOptionModel = whitespaceOptionModel;
		this.annotationSupport = annotationSupport;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_VIEW_MODE);
		if (cookie == null)
			diffMode = DiffViewMode.UNIFIED;
		else
			diffMode = DiffViewMode.valueOf(cookie.getValue());
	}

	private void doFilter(AjaxRequestTarget target) {
		body.replace(commentContainer = newCommentContainer());
		target.add(body);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("revisionsIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new WebSocketObserver() {
					
					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler) {
						handler.add(component);
					}
					
					@Override
					public Collection<String> getObservables() {
						return getWebSocketObservables();
					}
				});
				
				setOutputMarkupPlaceholderTag(true);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();

				Project project = projectModel.getObject();
				IndexManager indexManager = OneDev.getInstance(IndexManager.class);
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
					cookie.setPath("/");
					response.addCookie(cookie);
					target.add(RevisionDiffPanel.this);
					((BasePage)getPage()).resizeWindow(target);
				}
				
			}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					if (diffMode == each) {
						if (diffMode == DiffViewMode.SPLIT)
							return "active need-width";
						else
							return "active";
					} else {
						return "";
					}
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
						public boolean isSelected() {
							return whitespaceOptionModel.getObject() == each;
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
		
		List<String> listOfInvolvedPaths = new ArrayList<>(setOfInvolvedPaths);
		listOfInvolvedPaths.sort(new PathComparator());
		
		filterInput.add(new PatternSetAssistBehavior() {

			@Override
			protected List<InputSuggestion> suggest(String matchWith) {
				return SuggestionUtils.suggestPaths(listOfInvolvedPaths, matchWith);
			}

			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						"Path containing spaces or starting with dash needs to be quoted",
						"Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude"
						);
			}
			
		});

		filterInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				doFilter(target);
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
				doFilter(target);
			}
			
		});
		
		add(pathFilterForm);

		body = new WebMarkupContainer(BODY_ID) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.revisionDiff.onDomReady();"));
			}
			
		};
		body.setOutputMarkupId(true);
		add(body);

		body.add(new FencedFeedbackPanel("feedback", this));
		body.add(commentContainer = newCommentContainer());
		
		Component totalFilesLink;
		body.add(totalFilesLink = new Label("totalFiles", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String icon = String.format("<svg class='icon'><use xlink:href='%s'/></svg>", 
						SpriteImage.getVersionedHref("arrow"));
				return "Total " + changesAndCountModel.getObject().getChanges().size() + " files " + icon;
			}
			
		}).setEscapeModelStrings(false));
		
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
				String icon;
				if (change.getType() == null) {
					icon = "square";
				} else if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY)
					icon = "plus-square";
				else if (change.getType() == ChangeType.DELETE)
					icon = "minus-square";
				else if (change.getType() == ChangeType.MODIFY)
					icon = "dot-square";
				else
					icon = "arrow-square";
				
				item.add(new SpriteImage("icon", icon).add(AttributeAppender.append("class", icon)));

				boolean hasComments = false;
				if (annotationSupport != null) {
					for (Map.Entry<CodeComment, PlanarRange> entry: annotationSupport.getOldComments().entrySet()) {
						if (entry.getKey().getMark().getPath().equals(change.getOldBlobIdent().path)  
								&& change.isVisible(new DiffPlanarRange(true, entry.getValue()))) {
							hasComments = true;
							break;
						}
					}
					if (!hasComments) {
						for (Map.Entry<CodeComment, PlanarRange> entry: annotationSupport.getNewComments().entrySet()) {
							if (entry.getKey().getMark().getPath().equals(change.getNewBlobIdent().path) 
									&& change.isVisible(new DiffPlanarRange(false, entry.getValue()))) {
								hasComments = true;
								break;
							}
						}
					}
				}
				item.add(new WebMarkupContainer("hasComments").setVisible(hasComments));
				
				WebMarkupContainer fileLink = new WebMarkupContainer("file");
				fileLink.add(new Label("name", change.getPath()));
				fileLink.add(AttributeModifier.replace("href", "#diff-" + encodePath(change.getPath())));
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
				item.setMarkupId("diff-" + encodePath(change.getPath()));
				if (annotationSupport != null) {
					item.add(new BlobDiffPanel(DIFF_ID, projectModel, requestModel, change, diffMode, 
							getBlobBlameModel(change), new TextDiffPanel.AnnotationSupport() {
	
						@Override
						public DiffPlanarRange getMarkRange() {
							Mark mark = annotationSupport.getMark();
							if (mark != null) {
								if (change.getPaths().contains(mark.getPath())) {
									boolean leftSide = getOldCommitId().name().equals(mark.getCommitHash());
									return new DiffPlanarRange(leftSide, mark.getRange());
								} else {
									return null;
								}
							} else {
								CodeComment openComment = getOpenComment();
								if (openComment != null) 
									return getCommentRange(openComment);
								else
									return null;
							}
						}
	
						@Override
						public String getMarkUrl(DiffPlanarRange markRange) {
							return annotationSupport.getMarkUrl(getMark(change, markRange));
						}
	
						@Override
						public CodeComment getOpenComment() {
							CodeComment openComment = annotationSupport.getOpenComment();
							if (openComment != null && change.getPaths().contains(openComment.getMark().getPath()))
								return openComment;
							else
								return null;
						}
	
						@Override
						public void onOpenComment(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange commentRange) {
							RevisionDiffPanel.this.onOpenComment(target, comment, getMark(change, commentRange));
							((BasePage)getPage()).resizeWindow(target);
						}
	
						@Override
						public void onAddComment(AjaxRequestTarget target, DiffPlanarRange commentRange) {
							Mark mark = getMark(change, commentRange);
							commentContainer.setDefaultModelObject(mark);
							
							Fragment fragment = new Fragment(BODY_ID, "newCommentFrag", RevisionDiffPanel.this);
							fragment.setOutputMarkupId(true);
							
							Form<?> form = new Form<Void>("form");
							
							String uuid = UUID.randomUUID().toString();
							
							CommentInput contentInput;
							
							StringBuilder mentions = new StringBuilder();

							if (requestModel.getObject() == null) {
								/*
								 * Outside of pull request, no one will be notified of the comment. So we automatically 
								 * mention authors of commented lines
								 */
								for (User user: projectModel.getObject().getAuthors(mark.getPath(), 
										ObjectId.fromString(mark.getCommitHash()), 
										new LinearRange(commentRange.getFromRow(), commentRange.getToRow()))) {
									if (user.getEmail() != null)
										mentions.append("@").append(user.getName()).append(" ");
								}
							}
							
							form.add(contentInput = new CommentInput("content", Model.of(mentions.toString()), true) {

								@Override
								protected ProjectAttachmentSupport getAttachmentSupport() {
									return new ProjectAttachmentSupport(projectModel.getObject(), uuid, 
											SecurityUtils.canManageCodeComments(projectModel.getObject()));
								}

								@Override
								protected Project getProject() {
									return projectModel.getObject();
								}
								
							});
							contentInput.setRequired(true);
							contentInput.setLabel(Model.of("Comment"));
							
							FencedFeedbackPanel feedback = new FencedFeedbackPanel("feedback", form); 
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
									Mark mark = annotationSupport.getMark();
									if (mark != null) {
										BlobDiffPanel blobDiffPanel = getBlobDiffPanel(mark.getPath());
										if (blobDiffPanel != null) 
											blobDiffPanel.unmark(target);
										((AnnotationSupport)annotationSupport).onUnmark(target);
									}
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
									
									Project project = projectModel.getObject();
									PullRequest request = requestModel.getObject();
									CodeComment comment = new CodeComment();
									comment.setUUID(uuid);
									comment.setProject(project);
									comment.setRequest(request);
									comment.setUser(SecurityUtils.getUser());
									comment.setMark(mark);
									comment.setCompareContext(getCompareContext(mark.getCommitHash()));
									comment.setContent(contentInput.getModelObject());
									
									annotationSupport.onSaveComment(comment);
									
									CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), comment.getId()) {

										@Override
										protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
											RevisionDiffPanel.this.onCommentDeleted(target, comment, mark);
										}
										
										@Override
										protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
											annotationSupport.onSaveComment(comment);
											target.add(commentContainer.get("head"));
										}

										@Override
										protected PullRequest getPullRequest() {
											return requestModel.getObject();
										}

										@Override
										protected void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply) {
											reply.getComment().setCompareContext(getCompareContext(comment.getMark().getCommitHash()));
											annotationSupport.onSaveCommentReply(reply);
										}

									};
									commentContainer.replace(commentPanel);
									target.add(commentContainer);
									
									BlobDiffPanel blobDiffPanel = getBlobDiffPanel(comment.getMark().getPath());
									if (blobDiffPanel != null) 
										blobDiffPanel.onCommentAdded(target, comment, commentRange);

									((AnnotationSupport)annotationSupport).onCommentOpened(target, comment);
								}

							});
							fragment.add(form);
							
							commentContainer.replace(fragment);
							commentContainer.setVisible(true);
							target.add(commentContainer);
							
							Mark prevMark = annotationSupport.getMark();
							if (prevMark != null) {
								BlobDiffPanel blobDiffPanel = getBlobDiffPanel(prevMark.getPath());
								if (blobDiffPanel != null) 
									blobDiffPanel.unmark(target);
							}
							
							CodeComment prevComment = annotationSupport.getOpenComment();
							if (prevComment != null) {
								BlobDiffPanel blobDiffPanel = getBlobDiffPanel(prevComment.getMark().getPath());
								if (blobDiffPanel != null) 
									blobDiffPanel.onCommentClosed(target, prevComment);
							}  
							((AnnotationSupport)annotationSupport).onAddComment(target, mark);
							String script = String.format(""
									+ "setTimeout(function() {"
									+ "  var $textarea = $('#%s textarea');"
									+ "  $textarea.caret($textarea.val().length);"
									+ "}, 100);"
									+ "$(window).resize();", 
									commentContainer.getMarkupId());
							target.appendJavaScript(script);		
						}

						@Override
						public Component getCommentContainer() {
							return commentContainer;
						}

						@Override
						public Map<CodeComment, PlanarRange> getOldComments() {
							Map<CodeComment, PlanarRange> oldComments = new HashMap<>();
							for (Map.Entry<CodeComment, PlanarRange> entry: annotationSupport.getOldComments().entrySet()) {
								if (entry.getKey().getMark().getPath().equals(change.getOldBlobIdent().path)) 
									oldComments.put(entry.getKey(), entry.getValue());
							}
							return oldComments;
						}

						@Override
						public Map<CodeComment, PlanarRange> getNewComments() {
							Map<CodeComment, PlanarRange> newComments = new HashMap<>();
							for (Map.Entry<CodeComment, PlanarRange> entry: annotationSupport.getNewComments().entrySet()) {
								if (entry.getKey().getMark().getPath().equals(change.getNewBlobIdent().path)) 
									newComments.put(entry.getKey(), entry.getValue());
							}
							return newComments;
						}

						@Override
						public Collection<CodeProblem> getOldProblems() {
							return new ArrayList<>();
						}

						@Override
						public Collection<CodeProblem> getNewProblems() {
							return new ArrayList<>();
						}

						@Override
						public Collection<LineCoverage> getOldCoverages() {
							return new ArrayList<>();
						}

						@Override
						public Collection<LineCoverage> getNewCoverages() {
							return new ArrayList<>();
						}

					}));
				} else {
					item.add(new BlobDiffPanel(DIFF_ID, projectModel, requestModel, change, 
							diffMode, getBlobBlameModel(change), null));
				}
			}
			
		});
		
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "diff-mode-" + diffMode.name().toLowerCase();
			}
			
		}));
		
		setOutputMarkupId(true);
	}

	private CompareContext getCompareContext(String commitHash) {
		CompareContext compareContext = new CompareContext();
		String oldCommitHash = getOldCommitId().name();
		String newCommitHash = getNewCommitId().name();
		if (commitHash.equals(oldCommitHash)) {
			compareContext.setCompareCommitHash(newCommitHash);
			compareContext.setLeftSide(false);
		} else {
			compareContext.setCompareCommitHash(oldCommitHash);
			compareContext.setLeftSide(true);
		}
		compareContext.setPathFilter(pathFilterModel.getObject());
		compareContext.setWhitespaceOption(whitespaceOptionModel.getObject());
		return compareContext;
	}
	
	private void onOpenComment(AjaxRequestTarget target, CodeComment comment, Mark mark) {
		CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, comment.getId()) {

			@Override
			protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
				onCommentDeleted(target, comment, mark);
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
				annotationSupport.onSaveComment(comment);
				target.add(commentContainer.get("head"));
			}

			@Override
			protected PullRequest getPullRequest() {
				return requestModel.getObject();
			}

			@Override
			protected void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply) {
				reply.getComment().setCompareContext(getCompareContext(comment.getMark().getCommitHash()));
				annotationSupport.onSaveCommentReply(reply);
			}
			
		};
		
		commentContainer.setDefaultModelObject(mark);
		commentContainer.replace(commentPanel);
		commentContainer.setVisible(true);
		target.add(commentContainer);
		
		CodeComment prevComment = annotationSupport.getOpenComment();
		if (prevComment != null) {
			BlobDiffPanel blobDiffPanel = getBlobDiffPanel(prevComment.getMark().getPath());
			if (blobDiffPanel != null) 
				blobDiffPanel.onCommentClosed(target, prevComment);
		} 
		
		Mark prevMark = annotationSupport.getMark();
		if (prevMark != null) {
			BlobDiffPanel blobDiffPanel = getBlobDiffPanel(prevMark.getPath());
			if (blobDiffPanel != null)
				blobDiffPanel.unmark(target);
		}
		annotationSupport.onCommentOpened(target, comment);
	}
	
	private Mark getMark(BlobChange change, DiffPlanarRange range) {
		Mark mark = new Mark();
		mark.setRange(new PlanarRange(range));
		if (range.isLeftSide()) {
			mark.setCommitHash(getOldCommitId().name());
			mark.setPath(change.getOldBlobIdent().path);
		} else {
			mark.setCommitHash(getNewCommitId().name());
			mark.setPath(change.getNewBlobIdent().path);
		}
		return mark;
	}
	
	private DiffPlanarRange getMarkRange(@Nullable Mark mark) {
		if (mark != null)
			return new DiffPlanarRange(mark.getCommitHash().equals(getOldCommitId().name()), mark.getRange());
		else
			return null;
	}
	
	private String encodePath(String path) {
		return path.replace("/", "-").replace(" ", "-");
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
	
	@Nullable
	private DiffPlanarRange getCommentRange(CodeComment comment) {
		PlanarRange range = annotationSupport.getNewComments().get(comment);
		if (range != null) {
			return new DiffPlanarRange(false, range);
		} else {
			range = annotationSupport.getOldComments().get(comment);
			if (range != null)
				return new DiffPlanarRange(true, range);
			else
				return null;
		}
	}
	
	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new WebMarkupContainer("comment", Model.of((Mark)null)) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.revisionDiff.initComment();"));
			}
			
		};
		commentContainer.setOutputMarkupPlaceholderTag(true);
		
		WebMarkupContainer head = new WebMarkupContainer("head");
		commentContainer.add(head);
		
		head.add(new WebMarkupContainer("outdated") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentContainer.getDefaultModelObject() == null);
			}
			
		});
		
		head.add(new AjaxLink<Void>("locate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Mark mark = (Mark) commentContainer.getDefaultModelObject();
				BlobDiffPanel blobDiffPanel = getBlobDiffPanel(mark.getPath());
				if (blobDiffPanel != null)
					blobDiffPanel.mark(target, getMarkRange(mark));
				((AnnotationSupport)annotationSupport).onMark(target, mark);
				target.appendJavaScript(String.format("$('#%s').blur();", getMarkupId()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentContainer.getDefaultModelObject() != null);
			}

		}.setOutputMarkupId(true));
		
		head.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(commentContainer));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				CodeComment comment = annotationSupport.getOpenComment();
				clearComment(target);
				if (comment != null) {
					BlobDiffPanel blobDiffPanel = getBlobDiffPanel(comment.getMark().getPath());
					if (blobDiffPanel != null) 
						blobDiffPanel.onCommentClosed(target, comment);
					annotationSupport.onCommentClosed(target);
				}
			}
			
		});

		if (annotationSupport != null) {
			CodeComment openComment = annotationSupport.getOpenComment();
			if (openComment != null) {
				DiffPlanarRange commentRange = getCommentRange(openComment);
				if (commentRange != null) {
					BlobChange blobChange = getBlobChange(commentRange.isLeftSide(), openComment.getMark().getPath());
					if (blobChange != null && blobChange.isVisible(commentRange))
						commentContainer.setDefaultModelObject(getMark(blobChange, commentRange));
				}
				CodeCommentPanel commentPanel = new CodeCommentPanel(BODY_ID, openComment.getId()) {

					@Override
					protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
						onCommentDeleted(target, comment, (Mark) commentContainer.getDefaultModelObject());
					}
					
					@Override
					protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
						annotationSupport.onSaveComment(comment);
						target.add(commentContainer.get("head"));
					}

					@Override
					protected PullRequest getPullRequest() {
						return requestModel.getObject();
					}

					@Override
					protected void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply) {
						annotationSupport.onSaveCommentReply(reply);
					}
					
				};
				commentContainer.add(commentPanel);
			} else {
				commentContainer.add(new WebMarkupContainer(BODY_ID));
				commentContainer.setVisible(false);
			}
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
			return projectModel.getObject().getRevCommit(oldRev, true);
		}
	}
	
	private ObjectId getNewCommitId() {
		if (newRev.equals(ObjectId.zeroId().name().toString())) {
			return ObjectId.zeroId();
		} else {
			return projectModel.getObject().getRevCommit(newRev, true);
		}
	}
	
	@Nullable
	private BlobDiffPanel getBlobDiffPanel(String blobPath) {
		return diffsView.visitChildren(new IVisitor<Component, BlobDiffPanel>() {

			@SuppressWarnings("unchecked")
			@Override
			public void component(Component object, IVisit<BlobDiffPanel> visit) {
				if (object instanceof ListItem) {
					ListItem<BlobChange> item = (ListItem<BlobChange>) object;
					if (item.getModelObject().getPaths().contains(blobPath)) {
						visit.stop((BlobDiffPanel) item.get(DIFF_ID));
					} else {
						visit.dontGoDeeper();
					}
				} 
			}

		});
	}
	
	@Nullable
	private BlobChange getBlobChange(boolean leftSide, String path) {
		for (BlobChange change: changesAndCountModel.getObject().getChanges()) {
			if (leftSide && path.equals(change.getOldBlobIdent().path) 
					|| !leftSide && path.equals(change.getNewBlobIdent().path)) {
				return change;
			}
		}
		return null;
	}
	
	private void onCommentDeleted(AjaxRequestTarget target, CodeComment comment, @Nullable Mark mark) {
		clearComment(target);
		BlobDiffPanel blobDiffPanel = getBlobDiffPanel(comment.getMark().getPath());
		if (blobDiffPanel != null)
			blobDiffPanel.onCommentDeleted(target, comment, getMarkRange(mark));
		annotationSupport.onCommentClosed(target);
		mark = annotationSupport.getMark();
		if (mark != null) {
			blobDiffPanel = getBlobDiffPanel(mark.getPath());
			if (blobDiffPanel != null) 
				blobDiffPanel.mark(target, getMarkRange(mark));
		}
	}
	
	private void clearComment(AjaxRequestTarget target) {
		commentContainer.replace(new WebMarkupContainer(BODY_ID));
		commentContainer.setVisible(false);
		target.add(commentContainer);
		((BasePage)getPage()).resizeWindow(target);
	}
	
	@Override
	protected void onDetach() {
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
	
	private Set<String> getWebSocketObservables() {
		Project project = projectModel.getObject();
		return Sets.newHashSet(
				CommitIndexed.getWebSocketObservable(project.getObjectId(oldRev, true).name()), 
				CommitIndexed.getWebSocketObservable(project.getObjectId(newRev, true).name()));
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

	public static interface AnnotationSupport extends Serializable {
		
		@Nullable 
		Mark getMark();
		
		@Nullable
		String getMarkUrl(Mark mark);
		
		void onMark(AjaxRequestTarget target, Mark mark);
		
		void onUnmark(AjaxRequestTarget target);
		
		@Nullable 
		CodeComment getOpenComment();
		
		Map<CodeComment, PlanarRange> getOldComments();
		
		Map<CodeComment, PlanarRange> getNewComments();

		void onCommentOpened(AjaxRequestTarget target, CodeComment comment);
		
		void onCommentClosed(AjaxRequestTarget target);
		
		void onAddComment(AjaxRequestTarget target, Mark mark);

		void onSaveComment(CodeComment comment);
		
		void onSaveCommentReply(CodeCommentReply reply);
		
	}
	
}
