package io.onedev.server.web.component.diff.revision;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;

import org.apache.commons.codec.binary.Hex;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.nested.BranchItem;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.event.project.CommitIndexed;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.git.service.DiffEntryFacade;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.PendingSuggestionApply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.search.code.CodeIndexService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.PendingSuggestionApplyService;
import io.onedev.server.util.Pair;
import io.onedev.server.util.PathComparator;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.codecomment.CodeCommentPanel;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.diff.blob.BlobAnnotationSupport;
import io.onedev.server.web.component.diff.blob.BlobDiffPanel;
import io.onedev.server.web.component.diff.blob.BlobDiffReviewSupport;
import io.onedev.server.web.component.diff.blob.text.BlobTextDiffPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.markdown.OutdatedSuggestionException;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.suggestionapply.PullRequestSuggestionApplyBean;
import io.onedev.server.web.component.suggestionapply.SuggestionApplyBean;
import io.onedev.server.web.component.suggestionapply.SuggestionApplyModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.resource.PatchResource;
import io.onedev.server.web.resource.PatchResourceReference;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.DiffPlanarRange;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.TextUtils;
import io.onedev.server.web.util.WicketUtils;

/**
 * Make sure to add only one revision diff panel on a page
 * 
 * @author robin
 *
 */
public abstract class RevisionDiffPanel extends Panel {

	private static final String COOKIE_VIEW_MODE = "onedev.server.diff.viewmode";
	
	private static final String COOKIE_TREE = "revisionDiff.navigation";
	
	private static final String COOKIE_TREE_WIDTH = "revisionDiff.navigation.width";
	
	@Inject
	private PendingSuggestionApplyService pendingSuggestionApplyService;

	@Inject
	private CodeIndexService codeIndexService;

	@Inject
	private GitService gitService;

	private final String oldRev;
	
	private final String newRev;

	private final IModel<String> blameFileModel;
	
	private final RevisionAnnotationSupport annotationSupport;
	
	private final IModel<String> pathFilterModel;
	
	private final IModel<WhitespaceOption> whitespaceOptionModel;
	
	private final IModel<List<DiffEntryFacade>> diffEntriesModel = new LoadableDetachableModel<>() {

		@Override
		protected List<DiffEntryFacade> load() {
			AnyObjectId oldRevId = getProject().getObjectId(oldRev, true);
			AnyObjectId newRevId = getProject().getObjectId(newRev, true);
			return gitService.diff(getProject(), oldRevId, newRevId);
		}

	};
	
	private List<BlobChange> changes;
	
	private final IModel<List<PendingSuggestionApply>> pendingSuggestionAppliesModel =
			new LoadableDetachableModel<>() {

				@Override
				protected List<PendingSuggestionApply> load() {
					return pendingSuggestionApplyService.query(SecurityUtils.getAuthUser(), getPullRequest());
				}

			};


	private DiffViewMode viewMode;

	private String selectedPath;
	
	private transient Map<String, Optional<BlobAnnotationSupport>> blobAnnotationSupportCache;
	
	private WebMarkupContainer reviewProgress;
	
	private WebMarkupContainer commentContainer;
	
	private WebMarkupContainer treeContainer;

	private NestedTree<String> fileTree;

	private WebMarkupContainer navigation;
	
	private WebMarkupContainer body;

	private WebMarkupContainer content;
	
	public RevisionDiffPanel(String id, String oldRev, String newRev, 
							 IModel<String> pathFilterModel, IModel<WhitespaceOption> whitespaceOptionModel, 
							 @Nullable IModel<String> blameModel, 
							 @Nullable RevisionAnnotationSupport annotationSupport) {
		super(id);
		
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.pathFilterModel = pathFilterModel;
		this.blameFileModel = blameModel;
		this.whitespaceOptionModel = whitespaceOptionModel;
		this.annotationSupport = annotationSupport;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_VIEW_MODE);
		if (cookie == null)
			viewMode = DiffViewMode.UNIFIED;
		else
			viewMode = DiffViewMode.valueOf(cookie.getValue());
	}

	private void doFilter(AjaxRequestTarget target) {
		changes = null;
		blobAnnotationSupportCache = null;
		if (getSelectedChange() == null)
			selectFirstFile();
		body.replace(commentContainer = newCommentContainer());
		content.replace(treeContainer = newTreeContainer());
		content.replace(newDiff());
		target.add(body, navigation, reviewProgress);
	}

	private void initializeSelection() {
		CodeComment comment = annotationSupport != null ? annotationSupport.getOpenComment() : null;
		Mark mark = comment != null ? comment.getMark() : annotationSupport != null ? annotationSupport.getMark() : null;
		String path = mark != null ? mark.getPath() : blameFileModel != null ? blameFileModel.getObject() : null;
		BlobChange change = path != null ? getBlobChange(path) : null;
		if (change != null)
			selectedPath = change.getPath();
		else
			selectFirstFile();
	}

	private void selectFirstFile() {
		var changes = getChanges();
		selectedPath = changes.isEmpty() ? null : changes.get(0).getPath();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		initializeSelection();

		add(new WebMarkupContainer("revisionsIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new ChangeObserver() {

					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
						super.onObservableChanged(handler, changedObservables);
						RevisionDiffPanel.this.visitChildren(BlobTextDiffPanel.class, (IVisitor<BlobTextDiffPanel, Void>) (panel, visit) -> {
							if (panel.isVisibleInHierarchy())
								panel.refreshSymbolContext(handler);
						});
					}
					
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

				if (WicketUtils.isBot()
						|| (getProject().findRequireLoginForAutoIndexing() && SecurityUtils.getAuthUser() == null)) {
					setVisible(false);
					return;
				}

				ObjectId oldCommit = getOldCommitId();
				ObjectId newCommit = getNewCommitId();
				boolean oldCommitIndexed = oldCommit.equals(ObjectId.zeroId()) 
						|| codeIndexService.isIndexed(getProject().getId(), oldCommit);
				boolean newCommitIndexed = newCommit.equals(ObjectId.zeroId()) 
						|| codeIndexService.isIndexed(getProject().getId(), newCommit);
				if (oldCommitIndexed && newCommitIndexed) {
					setVisible(false);
				} else {
					if (!oldCommitIndexed)
						codeIndexService.indexAsync(getProject().getId(), oldCommit);
					if (!newCommitIndexed)
						codeIndexService.indexAsync(getProject().getId(), newCommit);
					setVisible(true);
				}
			}
			
		});

		add(new WebMarkupContainer("operations") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new AjaxLink<Void>("toggleTree") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						toggleTree(target);
						target.add(this);
					}

				}.add(AttributeAppender.append("class", new IModel<String>() {
					@Override
					public String getObject() {
						treeContainer.configure();
						return treeContainer.isVisible()? "active": "";
					}
				})));

				add(new MenuLink("batchedSuggestions") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("count", new IModel<String>() {

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
								&& SecurityUtils.getAuthUser() != null 
								&& !pendingSuggestionAppliesModel.getObject().isEmpty());
					}

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						menuItems.add(new MenuItem() {

							@Override
							public String getLabel() {
								return _T("Commit");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										new BeanEditModalPanel<SuggestionBatchApplyBean>(target, new SuggestionBatchApplyBean()) {
											@Override
											protected boolean isDirtyAware() {
												return false;
											}

											@Override
											protected String onSave(AjaxRequestTarget target, SuggestionBatchApplyBean bean) {
												String commitMessage = bean.getCommitMessage(); 
												PullRequest request = getPullRequest();
												ObjectId commitId = request.getLatestUpdate().getHeadCommit().copy();
												try {
													ObjectId newCommitId = pendingSuggestionApplyService.apply(SecurityUtils.getAuthUser(), request, commitMessage);
													
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
												return null;
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
								return _T("Discard");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmClickListener(
												_T("Do you really want to discard batched suggestions?")));
									}

									@Override
									public void onClick(AjaxRequestTarget target) {
										pendingSuggestionApplyService.discard(SecurityUtils.getAuthUser(), getPullRequest());
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

				add(new MenuLink("option") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						var menuItems = new ArrayList<MenuItem>();
						for (var value: DiffViewMode.values()) {
							menuItems.add(new MenuItem() {
								@Override
								public boolean isSelected() {
									return value == viewMode;
								}

								@Override
								public String getLabel() {
									return _T(TextUtils.getDisplayValue(value) + " view");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new AjaxLink<Void>(id) {

										@Override
										public void onClick(AjaxRequestTarget target) {
											if (value != viewMode) {
												viewMode = value;
												WebResponse response = (WebResponse) RequestCycle.get().getResponse();
												Cookie cookie = new Cookie(COOKIE_VIEW_MODE, value.name());
												cookie.setMaxAge(Integer.MAX_VALUE);
												cookie.setPath("/");
												response.addCookie(cookie);
												refreshFileDiff(target);
											}
											dropdown.close();
										}
									};
								}
							});
						}
						menuItems.add(null);
						for (var value: WhitespaceOption.values()) {
							menuItems.add(new MenuItem() {
								@Override
								public boolean isSelected() {
									return value == whitespaceOptionModel.getObject();
								}

								@Override
								public String getLabel() {
									return _T(TextUtils.getDisplayValue(value) + " whitespace");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new AjaxLink<Void>(id) {

										@Override
										public void onClick(AjaxRequestTarget target) {
											if (value != whitespaceOptionModel.getObject()) {
												whitespaceOptionModel.setObject(value);
												changes = null;
												blobAnnotationSupportCache = null;
												refreshFileDiff(target);
												CodeComment openComment = annotationSupport != null ? annotationSupport.getOpenComment() : null;
												if (openComment != null) {
													commentContainer.setDefaultModelObject(getCommentMark(openComment));
													target.add(commentContainer.get("head"));
												}
											}
											dropdown.close();
										}
									};
								}
							});
						}
						return menuItems;
					}
					
				});

				var params = PatchResource.paramsOf(getProject().getId(), getOldCommitId(), getNewCommitId());
				add(new ResourceLink<Void>("downloadPatch", new PatchResourceReference(), params)
						.add(new ConfirmClickModifier(_T("Download this patch?"))));
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
				return SuggestionUtils.suggestPathsByPathPattern(listOfInvolvedPaths, matchWith, true);
			}

			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						_T("Path containing spaces or starting with dash needs to be quoted"),
						_T("Use '**', '*' or '?' for <a href='${docsite}/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude")
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
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				doFilter(target);
			}
			
		});
		
		add(pathFilterForm);

		navigation = new WebMarkupContainer("navigation");
		navigation.setOutputMarkupId(true);
		navigation.add(newNavigationLink("previousFile", -1));
		navigation.add(newNavigationLink("nextFile", 1));
		add(navigation);
		
		reviewProgress = new WebMarkupContainer("reviewProgress") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getReviewSupport() != null && !getChanges().isEmpty());
			}
		};
		reviewProgress.add(new WebMarkupContainer("bar") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				var reviewSupport = getReviewSupport(); 
				var percentage = getChanges().stream().filter(it->reviewSupport.isReviewed(it.getPath())).count()*100/ getChanges().size();
				tag.put("style", "width:" + percentage + "%");
			}
		});
		reviewProgress.add(new Label("label", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				var reviewSupport = getReviewSupport();
				return MessageFormat.format(_T("{0} reviewed"), getChanges().stream().filter(it->reviewSupport.isReviewed(it.getPath())).count() + "/" + getChanges().size());
			}
		}));
		reviewProgress.setOutputMarkupPlaceholderTag(true);
		add(reviewProgress);
		
		body = new WebMarkupContainer("body");
		body.setOutputMarkupId(true);
		add(body);

		content = new WebMarkupContainer("content");
		body.add(content);

		body.add(new FencedFeedbackPanel("feedback", this));
		body.add(commentContainer = newCommentContainer());
		content.add(treeContainer = newTreeContainer());

		content.add(newDiff());

		add(AttributeAppender.append("class", new IModel<String>() {

			@Override
			public String getObject() {
				return "diff-mode-" + viewMode.name().toLowerCase();
			}
			
		}));

		setOutputMarkupId(true);
	}

	private Component newDiff() {
		BlobChange change = getSelectedChange();
		Component diff;
		if (change == null) {
			diff = new Label("diff", _T("No file to show diff"));
			diff.add(AttributeAppender.append("class", "alert alert-notice alert-light mb-0 align-self-start"));
		} else {
			diff = newBlobDiffPanel(change);
		}
		diff.setOutputMarkupId(true);
		diff.add(AttributeAppender.replace("data-selected-file", change != null ? encodePath(change.getPath()) : ""));
		diff.add(new Behavior() {
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.revisionDiff.onFileDiffReady();"));
			}
		});
		return diff;
	}

	private BlobDiffPanel newBlobDiffPanel(BlobChange change) {
		return new BlobDiffPanel("diff", change, viewMode, getBlobBlameModel(change)) {
			@Override
			protected PullRequest getPullRequest() {
				return RevisionDiffPanel.this.getPullRequest();
			}

			@Override
			protected BlobAnnotationSupport getAnnotationSupport() {
				return getBlobAnnotationSupport(change);
			}

			@Override
			protected BlobDiffReviewSupport getReviewSupport() {
				var reviewSupport = RevisionDiffPanel.this.getReviewSupport();
				if (reviewSupport == null)
					return null;
				return new BlobDiffReviewSupport() {
					@Override
					public void setReviewed(AjaxRequestTarget target, boolean reviewed) {
						reviewSupport.setReviewed(change.getPath(), reviewed);
						target.add(reviewProgress);
					}

					@Override
					public boolean isReviewed() {
						return reviewSupport.isReviewed(change.getPath());
					}
				};
			}
		};
	}

	private void refreshFileDiff(AjaxRequestTarget target) {
		var diff = newDiff();
		content.replace(diff);
		target.add(diff);
	}

	private AjaxLink<Void> newNavigationLink(String id, int offset) {
		return new AjaxLink<>(id) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(getAdjacentPath(offset) != null);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				String path = getAdjacentPath(offset);
				if (path != null)
					selectFile(target, path);
			}
		};
	}

	private int getSelectedIndex(List<BlobChange> changes) {
		for (int i = 0; i < changes.size(); i++) {
			if (changes.get(i).getPath().equals(selectedPath))
				return i;
		}
		return -1;
	}

	@Nullable
	private BlobChange getSelectedChange() {
		var changes = getChanges();
		int index = getSelectedIndex(changes);
		return index >= 0 ? changes.get(index) : null;
	}

	@Nullable
	private String getAdjacentPath(int offset) {
		var changes = getChanges();
		int index = getSelectedIndex(changes) + offset;
		return index >= 0 && index < changes.size() ? changes.get(index).getPath() : null;
	}

	private void selectFile(AjaxRequestTarget target, String path) {
		if (path.equals(selectedPath))
			return;

		if (annotationSupport != null) {
			if (commentContainer.isVisible())
				clearComment(target);
			if (annotationSupport.getOpenComment() != null)
				annotationSupport.onCommentClosed(target);
			else if (annotationSupport.getMark() != null)
				annotationSupport.onUnmark(target);
		}
		selectedPath = path;
		if (fileTree != null) {
			for (var parent = Paths.get(path).getParent(); parent != null; parent = parent.getParent()) {
				String folder = parent + "/";
				if (fileTree.getState(folder) == NestedTree.State.COLLAPSED) {
					if (treeContainer.isVisible())
						fileTree.expand(folder);
					else
						fileTree.getModelObject().add(folder);
				}
			}
		}
		refreshFileDiff(target);
		target.add(navigation);
		target.appendJavaScript("onedev.server.revisionDiff.scrollFileDiffIntoView();");
	}

	private void toggleTree(AjaxRequestTarget target) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie;
		if (treeContainer.isVisible()) {
			cookie = new Cookie(COOKIE_TREE, "no");
			treeContainer.setVisible(false);
		} else {
			cookie = new Cookie(COOKIE_TREE, "yes");
			treeContainer.setVisible(true);
		}
		cookie.setPath("/");
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		target.add(treeContainer);
		target.appendJavaScript("$(window).resize();");
	}
	
	private boolean isTreeVisibleInitially() {
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_TREE);
		if (cookie != null) 
			return cookie.getValue().equals("yes");
		else 
			return !WicketUtils.isDevice();
	}
	
	private List<BlobChange> getChanges() {
		if (changes == null) {
			List<DiffEntryFacade> diffEntries = diffEntriesModel.getObject();

			List<BlobChange> unfilteredChanges = new ArrayList<>();
			for (DiffEntryFacade entry : diffEntries) {
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
				unfilteredChanges.add(newBlobChange(changeType, oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject()));
			}

			List<BlobChange> filteredChanges = new ArrayList<>();
			String patternSetString = pathFilterModel.getObject();
			if (StringUtils.isNotBlank(patternSetString)) {
				try {
					PatternSet patternSet = PatternSet.parse(patternSetString.toLowerCase());
					Matcher matcher = new PathMatcher();
					for (BlobChange change : unfilteredChanges) {
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
				filteredChanges.addAll(unfilteredChanges);
			}

			// for some unknown reason, some paths in the diff entries is DELETE/ADD
			// pair instead MODIFICATION, here we normalize those as a single
			// MODIFICATION entry
			Map<String, BlobIdent> deleted = new HashMap<>();
			Map<String, BlobIdent> added = new HashMap<>();
			for (BlobChange change : filteredChanges) {
				if (change.getType() == ChangeType.DELETE)
					deleted.put(change.getPath(), change.getOldBlobIdent());
				else if (change.getType() == ChangeType.ADD)
					added.put(change.getPath(), change.getNewBlobIdent());
			}

			changes = new ArrayList<>();
			for (BlobChange change : filteredChanges) {
				BlobIdent oldBlobIdent = deleted.get(change.getPath());
				BlobIdent newBlobIdent = added.get(change.getPath());
				if (oldBlobIdent != null && newBlobIdent != null) {
					if (change.getType() == ChangeType.DELETE) {
						BlobChange normalizedChange = newBlobChange(ChangeType.MODIFY,
								oldBlobIdent, newBlobIdent, whitespaceOptionModel.getObject());
						changes.add(normalizedChange);
					}
				} else {
					changes.add(change);
				}
			}

			PathComparator comparator = new PathComparator();
			changes.sort((change1, change2) -> comparator.compare(change1.getPath(), change2.getPath()));
		}
		return changes;
	}

	private BlobChange newBlobChange(ChangeType type, BlobIdent oldBlobIdent, BlobIdent newBlobIdent, 
			WhitespaceOption whitespaceOption) {
		return new BlobChange(type, oldBlobIdent, newBlobIdent, whitespaceOption) {

			@Override
			public Project getProject() {
				return RevisionDiffPanel.this.getProject();
			}

		};
	}
	
	private CompareContext getCompareContext() {
		CompareContext compareContext = new CompareContext();
		compareContext.setPullRequest(getPullRequest());
		compareContext.setOldCommitHash(getOldCommitId().name());
		compareContext.setNewCommitHash(getNewCommitId().name());
		compareContext.setPathFilter(pathFilterModel.getObject());
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
	
	private String encodePath(String path) {
		return Hex.encodeHexString(path.getBytes(StandardCharsets.UTF_8));
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
		
	private WebMarkupContainer newTreeContainer() {
		WebMarkupContainer treeContainer = new WebMarkupContainer("tree") {

			@Override
			protected void onBeforeRender() {
				WebRequest request = (WebRequest) RequestCycle.get().getRequest();
				Cookie cookie = request.getCookie(COOKIE_TREE_WIDTH);
				// cookie will not be sent for websocket request
				if (cookie != null)
					add(AttributeAppender.replace("style", "width:" + cookie.getValue() + "px"));
				else
					add(AttributeAppender.replace("style", "width:360px"));

				super.onBeforeRender();
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.revisionDiff.initTree();"));
			}
			
		};
		
		var changes = new TreeMap<String, ChangeType>();
		for (var change : getChanges())
			changes.put(change.getPath(), change.getType());

		var treeState = new HashSet<String>();
		if (selectedPath != null) {
			for (var parent = Paths.get(selectedPath).getParent(); parent != null; parent = parent.getParent())
				treeState.add(parent + "/");
		}
		if (!changes.isEmpty()) {
			treeContainer.add(fileTree = new NestedTree<String>("content", new ITreeProvider<>() {

				@Override
				public Iterator<? extends String> getRoots() {
					return getChildren("");
				}

				@Override
				public boolean hasChildren(String node) {
					return node.endsWith("/");
				}

				@Override
				public Iterator<? extends String> getChildren(String node) {
					var paths = changes.tailMap(node).keySet().stream().takeWhile(it -> it.startsWith(node)).toList();
					var children = RevisionDiffPanel.getChildren(paths, node);
					children.sort(new PathComparator());
					return children.iterator();
				}

				@Override
				public IModel<String> model(String object) {
					return Model.of(object);
				}

				@Override
				public void detach() {
				}

			}, Model.ofSet(treeState)) {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new HumanTheme());
				}

				@Override
				protected Component newContentComponent(String id, IModel<String> model) {
					var path = model.getObject();
					var fragment = new Fragment(id, "navTreeNodeFrag", RevisionDiffPanel.this);
					WebMarkupContainer link;
					if (path.endsWith("/")) {
						link = new AjaxLink<Void>("link") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								if (getState(path) == State.EXPANDED)
									collapse(path);
								else
									expand(path);
							}
						};
						link.add(new SpriteImage("icon", "folder"));
					} else {
						link = new AjaxLink<Void>("link") {
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(body) {
									@Override
									public CharSequence getPrecondition(Component component) {
										return "if (document.getElementById('" + component.getMarkupId()
												+ "')?.classList.contains('active')) return false; " + super.getPrecondition(component);
									}
								});
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								selectFile(target, path);
							}
						};
						link.setMarkupId("diff-link-" + encodePath(path));
						link.add(AttributeAppender.append("class", () -> path.equals(selectedPath) ? "active" : ""));
						String icon;
						String color;
						var changeType = changes.get(path);
						if (changeType == ChangeType.ADD || changeType == ChangeType.COPY) {
							icon = "plus-square";
							color = "text-success";
						} else if (changeType == ChangeType.DELETE) {
							icon = "minus-square";
							color = "text-danger";
						} else if (changeType == ChangeType.MODIFY) {
							icon = "dot-square";
							color = "text-warning";
						} else {
							icon = "arrow-square";
							color = "text-warning";
						}
						link.add(new SpriteImage("icon", icon).add(AttributeAppender.append("class", color)));
					}

					link.add(new Label("label", new LoadableDetachableModel<String>() {
						@Override
						protected String load() {
							String label = path;
							var branchItem = fragment.getParent().getParent().findParent(BranchItem.class);
							if (branchItem != null) {
								var parentPath = (String) branchItem.getModelObject();
								label = path.substring(parentPath.length());
							}
							return StringUtils.stripEnd(label, "/");
						}
					}));
					fragment.add(link);

					return fragment;
				}
			});
		} else {
			fileTree = null;
			treeContainer.add(new Label("content", "<i>" + _T("No diffs to navigate") + "</i>").setEscapeModelStrings(false));
		}

		treeContainer.setOutputMarkupPlaceholderTag(true);
		treeContainer.setVisible(isTreeVisibleInitially());
		return treeContainer;
	}
	
	public static List<String> getChildren(Collection<String> sortedPaths, String currentPath) {
		List<String> children = new ArrayList<>();
		for (var path: sortedPaths) {
			if (path.startsWith(currentPath)) {
				var childPath = path.substring(currentPath.length());
				if (children.isEmpty()) {
					children.add(childPath);
				} else {
					var lastChildPath = children.get(children.size()-1);
					var lastChildPathSegments = Splitter.on('/').splitToList(lastChildPath);
					var childPathSegments = Splitter.on('/').splitToList(childPath);
					int index = 0;
					while (true) {
						if (index<lastChildPathSegments.size()-1 && index<childPathSegments.size()-1 
								&& lastChildPathSegments.get(index).equals(childPathSegments.get(index))) {
							index++;
						} else {
							break;
						}
					}
					if (index != 0) 
						children.set(children.size()-1, Joiner.on('/').join(childPathSegments.subList(0, index)) + "/");
					else 
						children.add(childPath);
				}
			}
		}
		return children.stream().map(it->currentPath+it).collect(toList());
	}

	@Nullable
	private Mark getCommentMark(CodeComment comment) {
		BlobChange change = getBlobChange(comment.getMark().getPath());
		if (change == null)
			return null;
		DiffPlanarRange range = getBlobAnnotationSupport(change).getCommentRange(comment);
		return range != null ? change.getMark(range) : null;
	}

	private WebMarkupContainer newCommentContainer() {
		WebMarkupContainer commentContainer = new WebMarkupContainer("comment", Model.of((Mark)null));
		commentContainer.setOutputMarkupPlaceholderTag(true);

		WebMarkupContainer head = new WebMarkupContainer("head") {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				Mark mark = (Mark) commentContainer.getDefaultModelObject();
				BlobChange change = getSelectedChange();
				String diffId = mark != null && change != null && change.getPaths().contains(mark.getPath())
						? "'" + content.get("diff").getMarkupId() + "'" : "null";
				String script = String.format("onedev.server.revisionDiff.initComment('%s', %s, %s, %s, %s);",
						commentContainer.getMarkupId(), diffId,
						mark != null && mark.getCommitHash().equals(getOldCommitId().name()),
						mark != null ? mark.getRange().getFromRow() : "null",
						mark != null ? mark.getRange().getToRow() : "null");
				response.render(OnDomReadyHeaderItem.forScript(script));
				if (RequestCycle.get().find(AjaxRequestTarget.class).isEmpty())
					response.render(OnLoadHeaderItem.forScript("onedev.server.revisionDiff.onWindowLoad();"));
			}
		};
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

				add(AttributeAppender.replace("data-tippy-content", new IModel<String>() {

					@Override
					public String getObject() {
						if (annotationSupport.getOpenComment().isResolved())
							return _T("Resolved");
						else
							return _T("Unresolved");
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
				commentContainer.setDefaultModelObject(getCommentMark(openComment));
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
		BlobChange change = getSelectedChange();
		if (change != null && change.getPaths().contains(blobPath) && content.get("diff") instanceof BlobDiffPanel panel)
			return panel;
		return null;
	}

	@Nullable
	private BlobChange getBlobChange(String blobPath) {
		for (BlobChange change : getChanges()) {
			if (change.getPath().equals(blobPath))
				return change;
		}
		for (BlobChange change : getChanges()) {
			if (blobPath.equals(change.getOldBlobIdent().path))
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
		blobAnnotationSupportCache = null;
		if (blameFileModel != null)
			blameFileModel.detach();
		pathFilterModel.detach();
		whitespaceOptionModel.detach();
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
						if (SecurityUtils.canModifyFile(request.getSourceProject(), request.getSourceBranch(), mark.getPath())) {
							return new ApplySupport() {

								@Override
								public void applySuggestion(AjaxRequestTarget target, List<String> suggestion) {
									var bean = new PullRequestSuggestionApplyBean();
									bean.setBranch(getPullRequest().getSourceBranch());
									bean.setCommitMessage(_T("Apply suggested change from code comment"));
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
											pendingApply.setUser(SecurityUtils.getAuthUser());
											pendingApply.setSuggestion(new ArrayList<String>(suggestion));
											pendingSuggestionApplyService.create(pendingApply);
											onBatchChange(target);
										}

										@Override
										public void removeFromBatch(AjaxRequestTarget target) {
											target.add(RevisionDiffPanel.this.get("operations"));
											for (Iterator<PendingSuggestionApply> it = pendingSuggestionAppliesModel.getObject().iterator(); it.hasNext();) {
												PendingSuggestionApply pendingApply = it.next();
												if (pendingApply.getRequest().equals(getPullRequest())
														&& pendingApply.getUser().equals(SecurityUtils.getAuthUser())
														&& pendingApply.getComment().equals(annotationSupport.getOpenComment())) {
													it.remove();
													pendingSuggestionApplyService.delete(pendingApply);
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
	
	@Nullable
	protected RevisionDiffReviewSupport getReviewSupport() {
		return null;
	}
	
	protected abstract boolean isContextDifferent(CompareContext compareContext);
	
	@Nullable
	private BlobAnnotationSupport getBlobAnnotationSupport(BlobChange change) {
		if (blobAnnotationSupportCache == null)
			blobAnnotationSupportCache = new HashMap<>();
		var blobAnnotationSupport = blobAnnotationSupportCache.get(change.getPath());
		if (blobAnnotationSupport == null) {
			if (annotationSupport != null) {
				blobAnnotationSupport = Optional.of(new BlobAnnotationSupport() {

					@Override
					public DiffPlanarRange getMarkRange() {
						Mark mark = annotationSupport.getMark();
						if (mark != null && mark.getRange() != null) {
							if (change.getPaths().contains(mark.getPath())) {
								boolean leftSide = getOldCommitId().name().equals(mark.getCommitHash());
								DiffPlanarRange markRange = new DiffPlanarRange(leftSide, mark.getRange());
								if (change.isVisible(markRange))
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
						return annotationSupport.getMarkUrl(change.getMark(markRange));
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
						RevisionDiffPanel.this.onOpenComment(target, comment, change.getMark(commentRange));
						((BasePage)getPage()).resizeWindow(target);
					}

					@Override
					public void onMark(AjaxRequestTarget target, DiffPlanarRange markRange) {
						Mark mark = change.getMark(markRange);
						annotationSupport.onMark(target, mark);						
					}

					@Override
					public void onAddComment(AjaxRequestTarget target, DiffPlanarRange commentRange) {
						Mark mark = change.getMark(commentRange);
						commentContainer.setDefaultModelObject(mark);

						Fragment fragment = new Fragment("body", "newCommentFrag", RevisionDiffPanel.this);
						fragment.setOutputMarkupId(true);

						Form<?> form = new Form<Void>("form");

						String uuid = UUID.randomUUID().toString();

						CommentInput contentInput;

						StringBuilder mentions = new StringBuilder();

						int mode = commentRange.isLeftSide() ? change.getOldBlobIdent().mode : change.getNewBlobIdent().mode;
						if (getPullRequest() == null && (FileMode.TYPE_MASK & mode) != FileMode.TYPE_GITLINK) {
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
							protected String getAutosaveKey() {
								return "project:" + getProject().getId() + ":new-code-comment";
							}

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
						contentInput.setLabel(Model.of(_T("Comment")));

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
							 protected void onError(AjaxRequestTarget target) {
								 super.onError(target);
								 target.add(feedback);
							 }

							 @Override
							 protected void onSubmit(AjaxRequestTarget target) {
								 super.onSubmit(target);

								 String content = contentInput.getModelObject();
								 SecurityUtils.checkWorkOnProject(getProject());
								 if (content.length() > CodeComment.MAX_CONTENT_LEN) {
									 error(_T("Comment too long"));
									 target.add(feedback);
								 } else {
									 CodeComment comment = new CodeComment();
									 comment.setUUID(uuid);
									 comment.setProject(getProject());
									 comment.setUser(SecurityUtils.getAuthUser());
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
						target.appendJavaScript("$(window).resize();");
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
							if (change.getOldBlobIdent().path != null) {
								for (Map.Entry<CodeComment, PlanarRange> entry:
										annotationSupport.getOldComments(change.getOldBlobIdent().path).entrySet()) {
									if (change.isVisible(new DiffPlanarRange(true, entry.getValue())))
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
							if (change.getNewBlobIdent().path != null) {
								for (Map.Entry<CodeComment, PlanarRange> entry:
										annotationSupport.getNewComments(change.getNewBlobIdent().path).entrySet()) {
									if (change.isVisible(new DiffPlanarRange(false, entry.getValue())))
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
							if (change.getOldBlobIdent().path != null) {
								for (CodeProblem problem: annotationSupport.getOldProblems(change.getOldBlobIdent().path)) {
									if (problem.getTarget() instanceof BlobTarget) {
										var repoTarget = (BlobTarget) problem.getTarget();
										if (repoTarget.getLocation() != null && change.isVisible(new DiffPlanarRange(true, repoTarget.getLocation())))
											oldProblems.add(problem);
									}
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
							if (change.getNewBlobIdent().path != null) {
								for (CodeProblem problem: annotationSupport.getNewProblems(change.getNewBlobIdent().path)) {
									if (problem.getTarget() instanceof BlobTarget) {
										var repoTarget = (BlobTarget) problem.getTarget();
										if (repoTarget.getLocation() != null && change.isVisible(new DiffPlanarRange(false, repoTarget.getLocation())))
											newProblems.add(problem);
									}
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
							if (change.getOldBlobIdent().path != null) {
								for (Map.Entry<Integer, CoverageStatus> entry: annotationSupport.getOldCoverages(change.getOldBlobIdent().path).entrySet()) {
									if (change.isVisible(true, entry.getKey()))
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
							if (change.getNewBlobIdent().path != null) {
								for (Map.Entry<Integer, CoverageStatus> entry: annotationSupport.getNewCoverages(change.getNewBlobIdent().path).entrySet()) {
									if (change.isVisible(false, entry.getKey()))
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
				blobAnnotationSupport = Optional.empty();
			}
		}
		return blobAnnotationSupport.orElse(null);
	}
	
}
