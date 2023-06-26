package io.onedev.server.web.component.diff.revision;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.entitymanager.PendingSuggestionApplyManager;
import io.onedev.server.event.project.CommitIndexed;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.git.service.DiffEntryFacade;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.*;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.search.code.CodeIndexManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.util.PathComparator;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.codecomment.CodeCommentPanel;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.diff.blob.BlobDiffPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.markdown.OutdatedSuggestionException;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.suggestionapply.SuggestionApplyBean;
import io.onedev.server.web.component.suggestionapply.SuggestionApplyModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.util.DiffPlanarRange;
import io.onedev.server.web.util.RevisionDiff;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import java.util.*;

/**
 * Make sure to add only one revision diff panel on a page
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public abstract class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "onedev.server.diff.viewmode";
	
	private static final String COOKIE_COMMENT_WIDTH = "revisionDiff.comment.width";

	private final String oldRev;
	
	private final String newRev;

	private final IModel<String> blameFileModel;
	
	private final RevisionDiff.AnnotationSupport annotationSupport;
	
	private final IModel<String> pathFilterModel;
	
	private final IModel<WhitespaceOption> whitespaceOptionModel;
	
	private DiffViewMode diffMode;
	
	private final IModel<String> currentFileModel;
	
	private final IModel<List<DiffEntryFacade>> diffEntriesModel = new LoadableDetachableModel<List<DiffEntryFacade>>() {

		@Override
		protected List<DiffEntryFacade> load() {
			AnyObjectId oldRevId = getProject().getObjectId(oldRev, true);
			AnyObjectId newRevId = getProject().getObjectId(newRev, true);
			return getGitService().diff(getProject(), oldRevId, newRevId);
		}
		
	};
	
	private IModel<List<BlobChange>> totalChangesModel = new LoadableDetachableModel<List<BlobChange>>() {

		@Override
		protected List<BlobChange> load() {
			List<DiffEntryFacade> diffEntries = diffEntriesModel.getObject();
			
			List<BlobChange> changes = new ArrayList<>();
			for (DiffEntryFacade entry: diffEntries) { 
				ChangeType changeType;
				if (entry.getChangeType() == ChangeType.RENAME 
						&& entry.getOldPath().equals(entry.getNewPath())) {
					// for some unknown reason, jgit detects rename even if path 
					// is the same
					changeType = ChangeType.MODIFY;
				} else {
					changeType = entry.getChangeType();
				}
				BlobIdent oldBlobIdent = GitUtils.getOldBlobIdent(entry, oldRev);
				BlobIdent newBlobIdent = GitUtils.getNewBlobIdent(entry, newRev);
	    		changes.add(newBlobChange(changeType, oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()));
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
	        			BlobChange normalizedChange = newBlobChange(ChangeType.MODIFY, 
	        					oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject());
	    				normalizedChanges.add(normalizedChange);
	    			}
	    		} else {
	    			normalizedChanges.add(change);
	    		}
	    	}

	    	PathComparator comparator = new PathComparator();
	    	normalizedChanges.sort((change1, change2)->comparator.compare(change1.getPath(), change2.getPath()));
	    	
	    	return normalizedChanges;
	    }
		
	};
	
	private final IModel<List<BlobChange>> displayChangesModel = new LoadableDetachableModel<List<BlobChange>>() {

		@Override
		protected List<BlobChange> load() {
			List<BlobChange> diffChanges = new ArrayList<>();
			if (getCurrentFile() != null) {
				for (BlobChange change: getTotalChanges()) {
					if (change.getPaths().contains(getCurrentFile()))
						diffChanges.add(change);
				}
			} else if (getTotalChanges().size() > WebConstants.MAX_DIFF_FILES) {
				diffChanges = getTotalChanges().subList(0, WebConstants.MAX_DIFF_FILES);
			} else {
				diffChanges = getTotalChanges();
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
	    	return displayChanges;	
	    }
		
	};
	
	private final IModel<List<PendingSuggestionApply>> pendingSuggestionAppliesModel = 
			new LoadableDetachableModel<List<PendingSuggestionApply>>() {

		@Override
		protected List<PendingSuggestionApply> load() {
			return OneDev.getInstance(PendingSuggestionApplyManager.class)
					.query(SecurityUtils.getUser(), getPullRequest());
		}
		
	};
	
	private float commentWidth = 360;
	
	private WebMarkupContainer commentContainer;

	private ListView<BlobChange> diffsView;
	
	private WebMarkupContainer navs;
	
	private WebMarkupContainer body;
	
	public RevisionDiffPanel(String id, String oldRev, String newRev, IModel<String> pathFilterModel, 
			IModel<String> currentFileModel, IModel<WhitespaceOption> whitespaceOptionModel, 
			@Nullable IModel<String> blameModel, @Nullable RevisionDiff.AnnotationSupport annotationSupport) {
		super(id);
		
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.pathFilterModel = pathFilterModel;
		this.blameFileModel = new IModel<String>() {

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
		
		this.currentFileModel = currentFileModel;
	}

	private void doFilter(AjaxRequestTarget target) {
		body.replace(commentContainer = newCommentContainer());
		target.add(body);
		target.add(navs);
	}
	
	@Nullable
	private String getCurrentFile() {
		return currentFileModel.getObject();
	}
	
	private void setCurrentFile(@Nullable String currentFile) {
		currentFileModel.setObject(currentFile);
		body.replace(commentContainer = newCommentContainer());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WebMarkupContainer("revisionsIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new ChangeObserver() {
					
					@Override
					public Collection<String> findObservables() {
						return getChangeObservables();
					}
				});
				
				setOutputMarkupPlaceholderTag(true);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();

				CodeIndexManager indexManager = OneDev.getInstance(CodeIndexManager.class);
				ObjectId oldCommit = getOldCommitId();
				ObjectId newCommit = getNewCommitId();
				boolean oldCommitIndexed = oldCommit.equals(ObjectId.zeroId()) 
						|| indexManager.isIndexed(getProject().getId(), oldCommit);
				boolean newCommitIndexed = newCommit.equals(ObjectId.zeroId()) 
						|| indexManager.isIndexed(getProject().getId(), newCommit);
				if (oldCommitIndexed && newCommitIndexed) {
					setVisible(false);
				} else {
					if (!oldCommitIndexed)
						indexManager.indexAsync(getProject().getId(), oldCommit);
					if (!newCommitIndexed)
						indexManager.indexAsync(getProject().getId(), newCommit);
					setVisible(true);
				}
			}
			
		});

		add(new WebMarkupContainer("operations") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new MenuLink("batchedSuggestions") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("count", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return String.valueOf(pendingSuggestionAppliesModel.getObject().size());
							}
							
						}));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest() != null 
								&& SecurityUtils.getUser() != null 
								&& !pendingSuggestionAppliesModel.getObject().isEmpty());
					}

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						menuItems.add(new MenuItem() {

							@Override
							public String getLabel() {
								return "Commit";
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										new BeanEditModalPanel<SuggestionBatchApplyBean>(target, new SuggestionBatchApplyBean()) {
											
											@Override
											protected void onSave(AjaxRequestTarget target, SuggestionBatchApplyBean bean) {
												String commitMessage = bean.getCommitMessage(); 
												PullRequest request = getPullRequest();
												ObjectId commitId = request.getLatestUpdate().getHeadCommit().copy();
												try {
													ObjectId newCommitId = OneDev.getInstance(PendingSuggestionApplyManager.class)
															.apply(SecurityUtils.getUser(), request, commitMessage);
													
													PullRequestChangesPage.State state = new PullRequestChangesPage.State();
													state.oldCommitHash = commitId.name();
													state.newCommitHash = newCommitId.name();
													setResponsePage(
															PullRequestChangesPage.class, 
															PullRequestChangesPage.paramsOf(request, state));
												} catch (Exception e) {
													ObsoleteCommitException obsoleteCommitException = 
															ExceptionUtils.find(e, ObsoleteCommitException.class);
													OutdatedSuggestionException outdatedSuggestionException = 
															ExceptionUtils.find(e, OutdatedSuggestionException.class);
													if (obsoleteCommitException != null) {
														Session.get().error("Pull request was updated by some others just now, please try again");
													} else if (outdatedSuggestionException != null) {
														Session.get().error("Please remove outdated suggestion on: " + outdatedSuggestionException.getMark());
														close();
													} else {
														throw ExceptionUtils.unchecked(e);
													}
												}
											}
										};
										dropdown.close();
									}
								};
							}

						});
						
						menuItems.add(new MenuItem() {

							@Override
							public String getLabel() {
								return "Discard";
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmClickListener(
												"Do you really want to discard batched suggestions?"));
									}

									@Override
									public void onClick(AjaxRequestTarget target) {
										OneDev.getInstance(PendingSuggestionApplyManager.class)
												.discard(SecurityUtils.getUser(), getPullRequest());
										target.add(commentContainer);
										target.add(RevisionDiffPanel.this.get("operations"));
										dropdown.close();
									}
									
								};
							}
							
						});
						return menuItems;
					}
					
				});

				add(new AjaxLink<Void>("option") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						DiffOption diffOption = new DiffOption();
						diffOption.setWhitespaceOption(whitespaceOptionModel.getObject());
						diffOption.setViewMode(diffMode);
						new BeanEditModalPanel<DiffOption>(target, diffOption) {
							
							@Override
							protected void onSave(AjaxRequestTarget target, DiffOption bean) {
								diffMode = bean.getViewMode();
								
								WebResponse response = (WebResponse) RequestCycle.get().getResponse();
								Cookie cookie = new Cookie(COOKIE_VIEW_MODE, diffMode.name());
								cookie.setMaxAge(Integer.MAX_VALUE);
								cookie.setPath("/");
								response.addCookie(cookie);
								
								whitespaceOptionModel.setObject(bean.getWhitespaceOption());
								
								target.add(navs);
								target.add(body);
								
								close();
							}
						};
					}

				});
			}			
			
		}.setOutputMarkupId(true));
		
		Form<?> pathFilterForm = new Form<Void>("pathFilter");
		TextField<String> filterInput;
		pathFilterForm.add(filterInput = new TextField<String>("input", pathFilterModel));
		
		Set<String> setOfInvolvedPaths = new HashSet<>();
		for (DiffEntryFacade diffEntry: diffEntriesModel.getObject()) {
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
				return SuggestionUtils.suggestByPattern(listOfInvolvedPaths, matchWith);
			}

			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						"Path containing spaces or starting with dash needs to be quoted",
						"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude"
						);
			}
			
		});

		filterInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				doFilter(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
				attributes.getAjaxCallListeners().add(new TrackViewStateListener(false));
			}
			
		});
		
		pathFilterForm.add(new AjaxButton("submit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
				attributes.getAjaxCallListeners().add(new TrackViewStateListener(false));
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				doFilter(target);
			}
			
		});
		
		add(pathFilterForm);
		
		navs = new WebMarkupContainer("navs");
		navs.setOutputMarkupId(true);
		add(navs);
		
		navs.add(new WebMarkupContainer("showAllFiles") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("totalFiles", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return "Total " + getTotalChanges().size() + " files";
					}
					
				}));
				add(new ViewStateAwareAjaxLink<Void>("showSingleFile") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						setCurrentFile(getTotalChanges().iterator().next().getPath());
						target.add(body);
						target.add(navs);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getTotalChanges().size() > 1);
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getCurrentFile() == null);
			}
			
		});
		
		navs.add(new WebMarkupContainer("showSingleFile") {

			private int getIndex() {
				int index = 0;
				for (BlobChange change: getTotalChanges()) {
					if (change.getPaths().contains(getCurrentFile()))
						return index;
					index++;
				}
				return -1;
			}
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new ViewStateAwareAjaxLink<Void>("prev") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						int index = getIndex() - 1;
						if (index >=0 && index < getTotalChanges().size()) {
							setCurrentFile(getTotalChanges().get(index).getPath());
							target.add(body);
							target.add(navs);
							target.appendJavaScript("onedev.server.revisionDiff.scrollToFilesTop();");							
						}
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled())
							tag.put("disabled", "disabled");
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						int index = getIndex();
						setEnabled(index > 0);
						setVisible(index != -1);
					}
					
				});
				
				add(new Label("message", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						int index = getIndex();
						if (index != -1)
							return "File " + (index +1) + " of " + getTotalChanges().size();
						else
							return "File '" + getCurrentFile() + "' not found in change set";
					}
					
				}));
				
				add(new ViewStateAwareAjaxLink<Void>("next") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						int index = getIndex() + 1;
						if (index >=0 && index < getTotalChanges().size()) {
							setCurrentFile(getTotalChanges().get(index).getPath());
							target.add(body);
							target.add(navs);
							target.appendJavaScript("onedev.server.revisionDiff.scrollToFilesTop();");							
						}
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled())
							tag.put("disabled", "disabled");
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						int index = getIndex();
						setEnabled(index < getTotalChanges().size()-1);
						setVisible(index != -1);
					}
					
				});
				
				add(new ViewStateAwareAjaxLink<Void>("showAllFiles") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						setCurrentFile(null);
						target.add(body);
						target.add(navs);
					}

				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getCurrentFile() != null);
			}
			
		});

		body = new WebMarkupContainer("body");
		body.setOutputMarkupId(true);
		add(body);

		body.add(new FencedFeedbackPanel("feedback", this));
		body.add(commentContainer = newCommentContainer());

		body.add(new Label("tooManyFiles", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Showing first " + getDisplayChanges().size() + " files as there are too many";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getCurrentFile() == null && getDisplayChanges().size() < getTotalChanges().size());
			}
			
		});		
		
		body.add(diffsView = new ListView<BlobChange>("diffs", new AbstractReadOnlyModel<List<BlobChange>>() {

			@Override
			public List<BlobChange> getObject() {
				return getDisplayChanges();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<BlobChange> item) {
				BlobChange change = item.getModelObject();
				item.setMarkupId("diff-" + encodePath(change.getPath()));
				item.add(new BlobDiffPanel("diff", change, diffMode, getBlobBlameModel(change)) {

					@Override
					protected PullRequest getPullRequest() {
						return RevisionDiffPanel.this.getPullRequest();
					}
					
				});
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
	
	private List<BlobChange> getTotalChanges() {
		return totalChangesModel.getObject();
	}
	
	private List<BlobChange> getDisplayChanges() {
		return displayChangesModel.getObject();
	}
	
	private BlobChange newBlobChange(ChangeType type, BlobIdent oldBlobIdent, BlobIdent newBlobIdent, 
			WhitespaceOption whitespaceOption) {
		return new BlobChange(type, oldBlobIdent, newBlobIdent, whitespaceOption) {

			@Override
			public Project getProject() {
				return RevisionDiffPanel.this.getProject();
			}

			private transient Optional<AnnotationSupport> annotationSupportCache;
			
			@Override
			public AnnotationSupport getAnnotationSupport() {
				if (annotationSupportCache == null) {
					if (annotationSupport != null) {
						annotationSupportCache = Optional.of(new BlobChange.AnnotationSupport() {
							
							@Override
							public DiffPlanarRange getMarkRange() {
								Mark mark = annotationSupport.getMark();
								if (mark != null) {
									if (getPaths().contains(mark.getPath())) {
										boolean leftSide = getOldCommitId().name().equals(mark.getCommitHash());
										DiffPlanarRange markRange = new DiffPlanarRange(leftSide, mark.getRange());
										if (isVisible(markRange))
											return markRange;
										else
											return null;
									} else {
										return null;
									}
								} else {
									Pair<CodeComment, DiffPlanarRange> openCommentPair = getOpenComment();
									if (openCommentPair != null) 
										return openCommentPair.getRight();
									else
										return null;
								}
							}
	
							@Override
							public String getMarkUrl(DiffPlanarRange markRange) {
								return annotationSupport.getMarkUrl(getMark(markRange));
							}
	
							@Override
							public Pair<CodeComment, DiffPlanarRange> getOpenComment() {
								CodeComment openComment = annotationSupport.getOpenComment();
								if (openComment != null) {
									DiffPlanarRange commentRange = getCommentRange(openComment);
									if (commentRange != null)
										return new Pair<>(openComment, commentRange);
								}
								return null;
							}
	
							@Override
							public void onOpenComment(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange commentRange) {
								RevisionDiffPanel.this.onOpenComment(target, comment, getMark(commentRange));
								((BasePage)getPage()).resizeWindow(target);
							}
	
							@Override
							public void onAddComment(AjaxRequestTarget target, DiffPlanarRange commentRange) {
								Mark mark = getMark(commentRange);
								commentContainer.setDefaultModelObject(mark);
								
								Fragment fragment = new Fragment("body", "newCommentFrag", RevisionDiffPanel.this);
								fragment.setOutputMarkupId(true);
								
								Form<?> form = new Form<Void>("form");
								
								String uuid = UUID.randomUUID().toString();
								
								CommentInput contentInput;
								
								StringBuilder mentions = new StringBuilder();
	
								if (getPullRequest() == null) {
									/*
									 * Outside of pull request, no one will be notified of the comment. So we automatically 
									 * mention authors of commented lines
									 */
									for (User user: getProject().getAuthors(mark.getPath(), 
											ObjectId.fromString(mark.getCommitHash()), 
											new LinearRange(commentRange.getFromRow(), commentRange.getToRow()))) {
										mentions.append("@").append(user.getName()).append(" ");
									}
								}
								
								form.add(contentInput = new CommentInput("content", Model.of(mentions.toString()), true) {
	
									@Override
									protected ProjectAttachmentSupport getAttachmentSupport() {
										return new ProjectAttachmentSupport(getProject(), uuid, 
												SecurityUtils.canManageCodeComments(getProject()));
									}
	
									@Override
									protected SuggestionSupport getSuggestionSupport() {
										return RevisionDiffPanel.this.getSuggestionSupport(mark);
									}

									@Override
									protected Project getProject() {
										return RevisionDiffPanel.this.getProject();
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
											annotationSupport.onUnmark(target);
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
										
										String content = contentInput.getModelObject();
										if (content.length() > CodeComment.MAX_CONTENT_LEN) {
											error("Comment too long");
											target.add(feedback);
										} else {
											CodeComment comment = new CodeComment();
											comment.setUUID(uuid);
											comment.setProject(getProject());
											comment.setUser(SecurityUtils.getUser());
											comment.setMark(mark);
											comment.setCompareContext(getCompareContext());
											comment.setContent(content);
											
											annotationSupport.onSaveComment(comment);
											
											CodeCommentPanel commentPanel = new CodeCommentPanel(fragment.getId(), comment.getId()) {
		
												@Override
												protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
													RevisionDiffPanel.this.onCommentDeleted(target, comment);
												}
												
												@Override
												protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
													annotationSupport.onSaveComment(comment);
													target.add(commentContainer.get("head"));
												}
		
												@Override
												protected void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply) {
													reply.setCompareContext(getCompareContext());
													annotationSupport.onSaveCommentReply(reply);
												}

												@Override
												protected void onSaveCommentStatusChange(AjaxRequestTarget target, CodeCommentStatusChange change, String note) {
													change.setCompareContext(getCompareContext());
													annotationSupport.onSaveCommentStatusChange(change, note);
												}
												
												@Override
												protected boolean isContextDifferent(CompareContext compareContext) {
													return RevisionDiffPanel.this.isContextDifferent(compareContext);
												}

												@Override
												protected SuggestionSupport getSuggestionSupport() {
													return RevisionDiffPanel.this.getSuggestionSupport(mark);
												}
		
											};
											commentContainer.replace(commentPanel);
											target.add(commentContainer);
											
											BlobDiffPanel blobDiffPanel = getBlobDiffPanel(comment.getMark().getPath());
											if (blobDiffPanel != null) 
												blobDiffPanel.onCommentAdded(target, comment, commentRange);
		
											annotationSupport.onCommentOpened(target, comment);
										}
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
										blobDiffPanel.onCommentClosed(target);
								}  
								annotationSupport.onAddComment(target, mark);
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
							
							private transient Map<CodeComment, PlanarRange> oldComments;
	
							@Override
							public Map<CodeComment, PlanarRange> getOldComments() {
								if (oldComments == null) {
									oldComments = new HashMap<>();
									if (getOldBlobIdent().path != null) { 
										for (Map.Entry<CodeComment, PlanarRange> entry: 
												annotationSupport.getOldComments(getOldBlobIdent().path).entrySet()) {
											if (isVisible(new DiffPlanarRange(true, entry.getValue())))
												oldComments.put(entry.getKey(), entry.getValue());
										}
									}
								} 
								return oldComments;
							}
	
							private transient Map<CodeComment, PlanarRange> newComments;
							
							@Override
							public Map<CodeComment, PlanarRange> getNewComments() {
								if (newComments == null) {
									newComments = new HashMap<>();
									if (getNewBlobIdent().path != null) {
										for (Map.Entry<CodeComment, PlanarRange> entry: 
												annotationSupport.getNewComments(getNewBlobIdent().path).entrySet()) {
											if (isVisible(new DiffPlanarRange(false, entry.getValue())))
												newComments.put(entry.getKey(), entry.getValue());
										}
									}
								} 
								return newComments;
							}

							private transient Collection<CodeProblem> oldProblems;
							
							@Override
							public Collection<CodeProblem> getOldProblems() {
								if (oldProblems == null) {
									oldProblems = new HashSet<>();
									if (getOldBlobIdent().path != null) {
										for (CodeProblem problem: annotationSupport.getOldProblems(getOldBlobIdent().path)) {
											if (isVisible(new DiffPlanarRange(true, problem.getRange())))
												oldProblems.add(problem);
										}
									}
								}
								return oldProblems;
							}

							private transient Collection<CodeProblem> newProblems;
							
							@Override
							public Collection<CodeProblem> getNewProblems() {
								if (newProblems == null) {
									newProblems = new HashSet<>();
									if (getNewBlobIdent().path != null) {
										for (CodeProblem problem: annotationSupport.getNewProblems(getNewBlobIdent().path)) {
											if (isVisible(new DiffPlanarRange(false, problem.getRange())))
												newProblems.add(problem);
										}
									}
								}
								return newProblems;
							}

							private transient Map<Integer, CoverageStatus> oldCoverages;
							
							@Override
							public Map<Integer, CoverageStatus> getOldCoverages() {
								if (oldCoverages == null) {
									oldCoverages = new HashMap<>();
									if (getOldBlobIdent().path != null) {
										for (Map.Entry<Integer, CoverageStatus> entry: annotationSupport.getOldCoverages(getOldBlobIdent().path).entrySet()) {
											if (isVisible(true, entry.getKey()))
												oldCoverages.put(entry.getKey(), entry.getValue());
										}
									}
								}
								return oldCoverages;
							}

							private transient Map<Integer, CoverageStatus> newCoverages;
							
							@Override
							public Map<Integer, CoverageStatus> getNewCoverages() {
								if (newCoverages == null) {
									newCoverages = new HashMap<>();
									if (getNewBlobIdent().path != null) {
										for (Map.Entry<Integer, CoverageStatus> entry: annotationSupport.getNewCoverages(getNewBlobIdent().path).entrySet()) {
											if (isVisible(false, entry.getKey()))
												newCoverages.put(entry.getKey(), entry.getValue());
										}
									}
								} 
								return newCoverages;
							}

							@Override
							public DiffPlanarRange getCommentRange(CodeComment comment) {
								PlanarRange commentRange = getNewComments().get(comment);
								if (commentRange != null)
									return new DiffPlanarRange(false, commentRange);
								commentRange = getOldComments().get(comment);
								if (commentRange != null)
									return new DiffPlanarRange(true, commentRange);
								return null;
							}
	
						});		
					} else {
						annotationSupportCache = Optional.absent();
					}
				}
				return annotationSupportCache.orNull();
			}
			
		};
	}
	
	private CompareContext getCompareContext() {
		CompareContext compareContext = new CompareContext();
		compareContext.setPullRequest(getPullRequest());
		compareContext.setOldCommitHash(getOldCommitId().name());
		compareContext.setNewCommitHash(getNewCommitId().name());
		compareContext.setPathFilter(pathFilterModel.getObject());
		compareContext.setCurrentFile(currentFileModel.getObject());
		compareContext.setWhitespaceOption(whitespaceOptionModel.getObject());
		return compareContext;
	}
	
	private void onOpenComment(AjaxRequestTarget target, CodeComment comment, Mark mark) {
		CodeCommentPanel commentPanel = new CodeCommentPanel("body", comment.getId()) {

			@Override
			protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
				onCommentDeleted(target, comment);
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
				annotationSupport.onSaveComment(comment);
				target.add(commentContainer.get("head"));
			}

			@Override
			protected void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply) {
				reply.setCompareContext(getCompareContext());
				annotationSupport.onSaveCommentReply(reply);
			}
			
			@Override
			protected void onSaveCommentStatusChange(AjaxRequestTarget target, CodeCommentStatusChange change, String note) {
				change.setCompareContext(getCompareContext());
				annotationSupport.onSaveCommentStatusChange(change, note);
			}
			
			@Override
			protected boolean isContextDifferent(CompareContext compareContext) {
				return RevisionDiffPanel.this.isContextDifferent(compareContext);
			}

			@Override
			protected SuggestionSupport getSuggestionSupport() {
				return RevisionDiffPanel.this.getSuggestionSupport(mark);
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
				blobDiffPanel.onCommentClosed(target);
		} 
		
		Mark prevMark = annotationSupport.getMark();
		if (prevMark != null) {
			BlobDiffPanel blobDiffPanel = getBlobDiffPanel(prevMark.getPath());
			if (blobDiffPanel != null)
				blobDiffPanel.unmark(target);
		}
		annotationSupport.onCommentOpened(target, comment);
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
		if (blameFileModel != null) {
			return new IModel<Boolean>() {

				@Override
				public void detach() {
				}

				@Override
				public Boolean getObject() {
					return change.getPath().equals(blameFileModel.getObject());
				}

				@Override
				public void setObject(Boolean object) {
					if (object)
						blameFileModel.setObject(change.getPath());
					else
						blameFileModel.setObject(null);
				}
				
			};
		} else {
			return null;
		}
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
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
		
		commentContainer.add(AttributeAppender.append("style", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				WebRequest request = (WebRequest) RequestCycle.get().getRequest();
				Cookie cookie = request.getCookie(COOKIE_COMMENT_WIDTH);
				// cookie will not be sent for websocket request
				if (cookie != null) 
					commentWidth = Float.parseFloat(cookie.getValue());
				return "width:" + commentWidth + "px";
			}
			
		}));
		
		WebMarkupContainer head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		commentContainer.add(head);
		
		head.add(new WebMarkupContainer("outdated") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(commentContainer.getDefaultModelObject() == null);
			}
			
		});
		
		head.add(new Label("status", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (annotationSupport.getOpenComment().isResolved()) {
					return String.format(
							"<svg class='icon text-success mr-1'><use xlink:href='%s'/></svg>", 
							SpriteImage.getVersionedHref("tick-circle-o"));
				} else {
					return String.format(
							"<svg class='icon text-warning mr-1'><use xlink:href='%s'/></svg>", 
							SpriteImage.getVersionedHref("dot"));
				}
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(AttributeAppender.replace("title", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (annotationSupport.getOpenComment().isResolved()) 
							return "Resolved";
						else 
							return "Unresolved";
					}
					
				}));
				
				add(new ChangeObserver() {
					
					@Override
					public Collection<String> findObservables() {
						Set<String> observables = new HashSet<>();
						if (annotationSupport != null && annotationSupport.getOpenComment() != null)
							observables.add(CodeComment.getChangeObservable(annotationSupport.getOpenComment().getId()));
						return observables;
					}
					
				});
				setEscapeModelStrings(false);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(annotationSupport != null && annotationSupport.getOpenComment() != null);
			}
			
		}.setOutputMarkupId(true));
		
		head.add(new AjaxLink<Void>("locate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Mark mark = (Mark) commentContainer.getDefaultModelObject();
				BlobDiffPanel blobDiffPanel = getBlobDiffPanel(mark.getPath());
				if (blobDiffPanel != null)
					blobDiffPanel.mark(target, getMarkRange(mark));
				annotationSupport.onMark(target, mark);
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
						blobDiffPanel.onCommentClosed(target);
					annotationSupport.onCommentClosed(target);
				} else {
					Mark mark = annotationSupport.getMark();
					if (mark != null) {
						BlobDiffPanel blobDiffPanel = getBlobDiffPanel(mark.getPath());
						if (blobDiffPanel != null) 
							blobDiffPanel.unmark(target);
						annotationSupport.onUnmark(target);
					}
				}
			}
			
		});

		if (annotationSupport != null) {
			CodeComment openComment = annotationSupport.getOpenComment();
			if (openComment != null) {
				BlobChange change = getBlobChange(openComment.getMark().getPath());
				if (change != null) {
					DiffPlanarRange commentRange = change.getAnnotationSupport().getCommentRange(openComment);
					if (commentRange != null)
						commentContainer.setDefaultModelObject(change.getMark(commentRange));
				}
				CodeCommentPanel commentPanel = new CodeCommentPanel("body", openComment.getId()) {

					@Override
					protected void onDeleteComment(AjaxRequestTarget target, CodeComment comment) {
						onCommentDeleted(target, comment);
					}
					
					@Override
					protected void onSaveComment(AjaxRequestTarget target, CodeComment comment) {
						annotationSupport.onSaveComment(comment);
						target.add(commentContainer.get("head"));
					}

					@Override
					protected void onSaveCommentReply(AjaxRequestTarget target, CodeCommentReply reply) {
						reply.setCompareContext(getCompareContext());
						annotationSupport.onSaveCommentReply(reply);
					}
					
					@Override
					protected void onSaveCommentStatusChange(AjaxRequestTarget target, CodeCommentStatusChange change, String note) {
						change.setCompareContext(getCompareContext());
						annotationSupport.onSaveCommentStatusChange(change, note);
					}
					
					@Override
					protected boolean isContextDifferent(CompareContext compareContext) {
						return RevisionDiffPanel.this.isContextDifferent(compareContext);
					}
					
					@Override
					protected SuggestionSupport getSuggestionSupport() {
						return RevisionDiffPanel.this.getSuggestionSupport(getComment().getMark());
					}
					
				};
				commentContainer.add(commentPanel);
			} else {
				commentContainer.add(new WebMarkupContainer("body"));
				commentContainer.setVisible(false);
			}
		} else {
			commentContainer.add(new WebMarkupContainer("body"));
			commentContainer.setVisible(false);
		}
		
		return commentContainer;
	}
	
	protected abstract Project getProject();
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	private ObjectId getOldCommitId() {
		if (oldRev.equals(ObjectId.zeroId().name().toString())) 
			return ObjectId.zeroId();
		else 
			return getProject().getRevCommit(oldRev, true);
	}
	
	private ObjectId getNewCommitId() {
		if (newRev.equals(ObjectId.zeroId().name().toString()))
			return ObjectId.zeroId();
		else 
			return getProject().getRevCommit(newRev, true);
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
						visit.stop((BlobDiffPanel) item.get("diff"));
					} else {
						visit.dontGoDeeper();
					}
				} 
			}

		});
	}
	
	@Nullable
	private BlobChange getBlobChange(String blobPath) {
		for (BlobChange change: getDisplayChanges()) {
			if (change.getPaths().contains(blobPath))
				return change;
		}
		return null;
	}
	
	private void onCommentDeleted(AjaxRequestTarget target, CodeComment comment) {
		clearComment(target);
		BlobDiffPanel blobDiffPanel = getBlobDiffPanel(comment.getMark().getPath());
		if (blobDiffPanel != null)
			blobDiffPanel.onCommentDeleted(target);
		annotationSupport.onCommentClosed(target);
	}
	
	private void clearComment(AjaxRequestTarget target) {
		commentContainer.replace(new WebMarkupContainer("body"));
		commentContainer.setVisible(false);
		target.add(commentContainer);
		((BasePage)getPage()).resizeWindow(target);
	}
	
	@Override
	protected void onDetach() {
		diffEntriesModel.detach();
		totalChangesModel.detach();
		if (blameFileModel != null)
			blameFileModel.detach();
		pathFilterModel.detach();
		whitespaceOptionModel.detach();
		currentFileModel.detach();
		pendingSuggestionAppliesModel.detach();
		
		super.onDetach();
	}
	
	private SuggestionSupport getSuggestionSupport(Mark mark) {
		return new SuggestionSupport() {
			
			@Override
			public Selection getSelection() {
				return getProject().getBlob(mark.getBlobIdent(), true).getText()
						.getSelection(mark.getRange());
			}
			
			@Override
			public ApplySupport getApplySupport() {
				PullRequest request = getPullRequest();
				if (request != null) {
					if (request.isOpen()) {
						if (SecurityUtils.canModify(request.getSourceProject(), request.getSourceBranch(), mark.getPath())) {
							return new ApplySupport() {

								@Override
								public void applySuggestion(AjaxRequestTarget target, List<String> suggestion) {
									SuggestionApplyBean bean = new SuggestionApplyBean();
									bean.setBranch(getPullRequest().getSourceBranch());
									new SuggestionApplyModalPanel(target, bean) {

										@Override
										protected CodeComment getComment() {
											return annotationSupport.getOpenComment();
										}

										@Override
										protected PullRequest getPullRequest() {
											return RevisionDiffPanel.this.getPullRequest();
										}

										@Override
										protected List<String> getSuggestion() {
											return suggestion;
										}

									};								
								}

								@Override
								public BatchApplySupport getBatchSupport() {
									return new BatchApplySupport() {
										
										@Override
										public List<String> getInBatch() {
											for (PendingSuggestionApply pendingApply: pendingSuggestionAppliesModel.getObject()) {
												if (pendingApply.getComment().equals(annotationSupport.getOpenComment()))
													return pendingApply.getSuggestion();
											}
											return null;
										}

										private void onBatchChange(AjaxRequestTarget target) {
											target.add(RevisionDiffPanel.this.get("operations"));
											target.add(commentContainer);
											target.appendJavaScript("onedev.server.revisionDiff.onSuggestionBatchChanged();");
										}
										
										@Override
										public void addToBatch(AjaxRequestTarget target, List<String> suggestion) {
											PendingSuggestionApply pendingApply = new PendingSuggestionApply();
											pendingApply.setComment(annotationSupport.getOpenComment());
											pendingApply.setRequest(getPullRequest());
											pendingApply.setUser(SecurityUtils.getUser());
											pendingApply.setSuggestion(new ArrayList<String>(suggestion));
											OneDev.getInstance(PendingSuggestionApplyManager.class).create(pendingApply);
											onBatchChange(target);
										}

										@Override
										public void removeFromBatch(AjaxRequestTarget target) {
											target.add(RevisionDiffPanel.this.get("operations"));
											for (Iterator<PendingSuggestionApply> it = pendingSuggestionAppliesModel.getObject().iterator(); it.hasNext();) {
												PendingSuggestionApply pendingApply = it.next();
												if (pendingApply.getRequest().equals(getPullRequest())
														&& pendingApply.getUser().equals(SecurityUtils.getUser())
														&& pendingApply.getComment().equals(annotationSupport.getOpenComment())) {
													it.remove();
													OneDev.getInstance(PendingSuggestionApplyManager.class).delete(pendingApply);
													break;
												}
											}
											onBatchChange(target);
										}
										
									};
								}
								
							};
						} else {
							return null;
						}
					} else {
						return null;
					}
				} else if (SecurityUtils.canWriteCode(annotationSupport.getOpenComment().getProject())) {
					return new ApplySupport() {

						@Override
						public void applySuggestion(AjaxRequestTarget target, List<String> suggestion) {
							SuggestionApplyBean bean = new SuggestionApplyBean();
							String refName = getProject().getRefName(newRev);
							if (refName != null) {
								String branch = GitUtils.ref2branch(refName);
								if (branch != null)
									bean.setBranch(branch);
							}
							new SuggestionApplyModalPanel(target, bean) {

								@Override
								protected CodeComment getComment() {
									return annotationSupport.getOpenComment();
								}

								@Override
								protected List<String> getSuggestion() {
									return suggestion;
								}

							};								
						}

						@Override
						public BatchApplySupport getBatchSupport() {
							return null;
						}
						
					};
				} else {
					return null;
				}
			}

			@Override
			public boolean isOutdated() {
				if (getPullRequest() != null) {
					if (getPullRequest().isOpen() && getPullRequest().getSourceHead() != null) {
						try {
							new BlobEdits().applySuggestion(getPullRequest().getSourceProject(), mark, 
									new ArrayList<>(), getPullRequest().getSourceHead());
							return false;
						} catch (OutdatedSuggestionException e) {
							return true;
						}
					} else {
						return true;
					}
				} else {
					return false;
				}
			}

			@Override
			public String getFileName() {
				return mark.getPath();
			}
			
		};
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new RevisionDiffResourceReference()));
	}
	
	private Set<String> getChangeObservables() {
		return Sets.newHashSet(
				CommitIndexed.getChangeObservable(getProject().getObjectId(oldRev, true).name()), 
				CommitIndexed.getChangeObservable(getProject().getObjectId(newRev, true).name()));
	}
	
	protected abstract boolean isContextDifferent(CompareContext compareContext);
	
}
