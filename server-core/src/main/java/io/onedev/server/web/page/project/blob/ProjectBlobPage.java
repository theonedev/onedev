package io.onedev.server.web.page.project.blob;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.BlobEditException;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.search.code.CommitIndexed;
import io.onedev.server.search.code.IndexManager;
import io.onedev.server.search.code.SearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.TextQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.revisionpicker.RevisionPicker;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.navigator.BlobNavigator;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;
import io.onedev.server.web.page.project.blob.render.view.Positionable;
import io.onedev.server.web.page.project.blob.search.SearchMenuContributor;
import io.onedev.server.web.page.project.blob.search.advanced.AdvancedSearchPanel;
import io.onedev.server.web.page.project.blob.search.quick.QuickSearchPanel;
import io.onedev.server.web.page.project.blob.search.result.SearchResultPanel;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.resource.RawBlobResourceReference;
import io.onedev.server.web.util.EditParamsAware;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public class ProjectBlobPage extends ProjectPage implements BlobRenderContext, ScriptIdentityAware, EditParamsAware {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_INITIAL_NEW_PATH = "initial-new-path";
	
	private static final String PARAM_REQUEST = "request";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MODE = "mode";
	
	private static final String PARAM_VIEW_PLAIN = "view-plain";
	
	private static final String PARAM_URL_BEFORE_EDIT = "url-before-edit";
	
	private static final String PARAM_URL_AFTER_EDIT = "url-after-edit";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_POSITION = "position";
	
	private static final String PARAM_COVERAGE_REPORT = "coverage-report";
	
	private static final String PARAM_PROBLEM_REPORT = "problem-report";
	
	private static final String PARAM_RAW = "raw";
	
	private static final String REVISION_PICKER_ID = "revisionPicker";
	
	private static final String BLOB_NAVIGATOR_ID = "blobNavigator";

	private static final String BLOB_CONTENT_ID = "blobContent";
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectBlobPage.class);
	
	private State state = new State();
	
	private ObjectId resolvedRevision;
	
	private Component revisionIndexing;
	
	private WebMarkupContainer searchResult;
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public ProjectBlobPage(PageParameters params) {
		super(params);
		
		List<String> revisionAndPathSegments = new ArrayList<>();
		String segment = params.get(PARAM_REVISION).toString();
		if (segment != null && segment.length() != 0)
			revisionAndPathSegments.add(segment);
		segment = params.get(PARAM_PATH).toString();
		if (segment != null && segment.length() != 0)
			revisionAndPathSegments.add(segment);
		
		for (int i=0; i<params.getIndexedCount(); i++) {
			segment = params.get(i).toString();
			if (segment.length() != 0)
				revisionAndPathSegments.add(segment);
		}

		BlobIdent blobIdent = new BlobIdent(getProject(), revisionAndPathSegments); 
		state = new State(blobIdent);

		String modeStr = params.get(PARAM_MODE).toString();
		if (modeStr != null)
			state.mode = Mode.valueOf(modeStr.toUpperCase());
		
		String viewPlain = params.get(PARAM_VIEW_PLAIN).toString();
		state.viewPlain = "true".equals(viewPlain);
		
		state.urlBeforeEdit = params.get(PARAM_URL_BEFORE_EDIT).toString();
		state.urlAfterEdit = params.get(PARAM_URL_AFTER_EDIT).toString();

		if (state.blobIdent.revision != null)
			resolvedRevision = getProject().getRevCommit(state.blobIdent.revision, true).copy();
		
		state.position = params.get(PARAM_POSITION).toString();
		state.requestId = params.get(PARAM_REQUEST).toOptionalLong();
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.query = params.get(PARAM_QUERY).toString();
		state.initialNewPath = params.get(PARAM_INITIAL_NEW_PATH).toString();
		state.coverageReport = params.get(PARAM_COVERAGE_REPORT).toOptionalString();
		state.problemReport = params.get(PARAM_PROBLEM_REPORT).toOptionalString();
		
		if (state.mode == Mode.ADD || state.mode == Mode.EDIT || state.mode == Mode.DELETE) {
			if (!isOnBranch()) 
				throw new IllegalArgumentException("Files can only be edited on branch");
			
			String path = state.blobIdent.path;
			if (path != null && state.blobIdent.isTree())
				path += "/";
			if (!SecurityUtils.canModify(getProject(), state.blobIdent.revision, path))
				unauthorized();
		}
		
		if (params.get(PARAM_RAW).toBoolean(false)) {
			RequestCycle.get().scheduleRequestHandlerAfterCurrent(
					new ResourceReferenceRequestHandler(new RawBlobResourceReference(), getPageParameters()));
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		newRevisionPicker(null);
		newCommitStatus(null);
		newBlobNavigator(null);
		newBlobOperations(null);
		
		add(revisionIndexing = new WebMarkupContainer("revisionIndexing") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				if (resolvedRevision != null) {
					RevCommit commit = getProject().getRevCommit(resolvedRevision, true);
					IndexManager indexManager = OneDev.getInstance(IndexManager.class);
					if (!indexManager.isIndexed(getProject(), commit)) {
						OneDev.getInstance(IndexManager.class).indexAsync(getProject(), commit);
						setVisible(true);
					} else {
						setVisible(false);
					}
				} else {
					setVisible(false);
				}
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		revisionIndexing.add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(revisionIndexing);
				resizeWindow(handler);
			}
			
			@Override
			public Collection<String> getObservables() {
				Set<String> observables = new HashSet<>();
				if (resolvedRevision != null) 
					observables.add(CommitIndexed.getWebSocketObservable(getProject().getRevCommit(resolvedRevision, true).name()));
				return observables;
			}
			
		});

		newBuildSupportNote(null);
		newBlobContent(null);

		add(searchResult = new WebMarkupContainer("searchResult"));

		List<QueryHit> queryHits;
		if (state.query != null) {
			BlobQuery query = new TextQuery.Builder()
					.term(state.query)
					.wholeWord(true)
					.caseSensitive(true) 
					.count(SearchResultPanel.MAX_QUERY_ENTRIES)
					.build();
			try {
				SearchManager searchManager = OneDev.getInstance(SearchManager.class);
				queryHits = searchManager.search(projectModel.getObject(), getProject().getRevCommit(resolvedRevision, true), 
						query);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}								
		} else {
			queryHits = null;
		}
		
		newSearchResult(null, queryHits);

		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters(); 
				String action = params.getParameterValue("action").toString("");
				switch (action) {
				case "quickSearch": 
					new ModalPanel(target) {
						
						@Override
						protected Component newContent(String id) {
							return newQuickSearchPanel(id, this);
						}
						
					};
					break;
				case "advancedSearch":
					new ModalPanel(target) {
						
						@Override
						protected Component newContent(String id) {
							return newAdvancedSearchPanel(id, this);
						}
						
					};
					break;
				case "permalink":
					if (isOnBranch()) {
						BlobIdent newBlobIdent = new BlobIdent(state.blobIdent);
						newBlobIdent.revision = resolvedRevision.name();
						ProjectBlobPage.this.onSelect(target, newBlobIdent, null);
					}
					break;
				default:
					throw new IllegalStateException("Unexpected action: " + action);
				}
			}
			
		});
	}

	/*
	 * In case we are on a branch, this operation makes sure that the branch resolves
	 * to a certain commit during the life cycle of our page, unless the page is 
	 * refreshed. This can avoid the issue that displayed file content and subsequent 
	 * operations encounters different commit if someone commits to the branch while 
	 * we are staying on the page. 
	 */
	@Override
	protected Map<String, ObjectId> getObjectIdCache() {
		Map<String, ObjectId> objectIdCache = new HashMap<>();
		if (resolvedRevision != null)
			objectIdCache.put(state.blobIdent.revision, resolvedRevision);
		return objectIdCache;
	}

	@Override
	public CodeComment getOpenComment() {
		if (state.commentId != null)
			return OneDev.getInstance(CodeCommentManager.class).load(state.commentId);
		else
			return null;
	}
	
	@Override
	public PullRequest getPullRequest() {
		if (state.requestId != null)
			return OneDev.getInstance(PullRequestManager.class).load(state.requestId);
		else
			return null;
	}
	
	private void newBlobNavigator(@Nullable AjaxRequestTarget target) {
		Component blobNavigator = new BlobNavigator(BLOB_NAVIGATOR_ID, this);
		if (target != null) {
			replace(blobNavigator);
			target.add(blobNavigator);
		} else {
			add(blobNavigator);
		}
	}

	private void newBlobOperations(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer blobOperations = new WebMarkupContainer("blobOperations");
		blobOperations.setOutputMarkupId(true);
		
		String revision = state.blobIdent.revision;
		
		AtomicBoolean reviewRequired = new AtomicBoolean(true);
		try {
			reviewRequired.set(revision!=null && getProject().isReviewRequiredForModification(getLoginUser(), revision, null)); 
		} catch (Exception e) {
			logger.error("Error checking review requirement", e);
		}
		
		AtomicBoolean buildRequired = new AtomicBoolean(true);
		try {
			buildRequired.set(revision!=null && getProject().isBuildRequiredForModification(getLoginUser(), revision, null));
		} catch (Exception e) {
			logger.error("Error checking build requirement", e);
		}

		blobOperations.add(new MenuLink("add") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Create New File";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ViewStateAwareAjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								onModeChange(target, Mode.ADD, null);
								dropdown.close();
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Upload Files";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ModalLink(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								super.onClick(target);
								dropdown.close();
							}

							@Override
							protected Component newContent(String id, ModalPanel modal) {
								return new BlobUploadPanel(id, ProjectBlobPage.this) {

									@Override
									public void onCancel(AjaxRequestTarget target) {
										modal.close();
									}

									@Override
									public void onCommitted(AjaxRequestTarget target, RefUpdated refUpdated) {
										ProjectBlobPage.this.onCommitted(target, refUpdated, null);
										modal.close();
									}
									
								};
							}
							
						};
					}
					
				});
				return menuItems;
			}
			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (reviewRequired.get()) {
					tag.append("class", "disabled", " ");
					tag.put("title", "Review required for this change. Submit pull request instead");
				} else if (buildRequired.get()) {
					tag.append("class", "disabled", " ");
					tag.put("title", "Build required for this change. Submit pull request instead");
				} else {
					tag.put("title", "Add on branch " + state.blobIdent.revision);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				Project project = getProject();
				if ((state.mode == Mode.VIEW || state.mode == Mode.BLAME) 
						&& isOnBranch() && state.blobIdent.isTree() 
						&& SecurityUtils.canWriteCode(project)) {
					setVisible(true);
					setEnabled(!reviewRequired.get() && !buildRequired.get());
				} else {
					setVisible(false);
				}
			}
			
		});
		
		blobOperations.add(new MenuLink("search") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				menuItems.add(new MenuItem() {

					@Override
					public String getShortcut() {
						return "T";
					}

					@Override
					public String getLabel() {
						return "Quick Search";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ModalLink(id) {
							
							@Override
							public void onClick(AjaxRequestTarget target) {
								super.onClick(target);
								dropdown.close();
							}

							@Override
							protected Component newContent(String id, ModalPanel modal) {
								return newQuickSearchPanel(id, modal);
							}
							
						};
					}
					
				});
				menuItems.add(new MenuItem() {

					@Override
					public String getShortcut() {
						return "V";
					}

					@Override
					public String getLabel() {
						return "Advanced Search";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ModalLink(id) {
							
							@Override
							public void onClick(AjaxRequestTarget target) {
								super.onClick(target);
								dropdown.close();
							}

							@Override
							protected Component newContent(String id, ModalPanel modal) {
								return newAdvancedSearchPanel(id, modal);
							}
							
						};
					}
					
				});
				
				IVisitor<Component, Component> visitor = new IVisitor<Component, Component>() {

					@Override
					public void component(Component object, IVisit<Component> visit) {
						visit.stop(object);
					}
					
				};
				SearchMenuContributor contributor = (SearchMenuContributor) getPage().visitChildren(
						SearchMenuContributor.class, visitor);
				if (contributor != null)
					menuItems.addAll(contributor.getMenuItems(dropdown));
				
				return menuItems;
			}

		});
		
		String compareWith = resolvedRevision!=null?resolvedRevision.name():null;
		String query;
		if (state.blobIdent.path != null)
			query = String.format("path(%s)", state.blobIdent.path);
		else
			query = null;
		blobOperations.add(new ViewStateAwarePageLink<Void>("history", ProjectCommitsPage.class, 
				ProjectCommitsPage.paramsOf(getProject(), query, compareWith)));
		
		blobOperations.add(new DropdownLink("getCode") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(state.blobIdent.revision != null && state.blobIdent.path == null);
			}

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeAppender.append("class", "get-code"));
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new GetCodePanel(id, this) {
					
					@Override
					protected Project getProject() {
						return ProjectBlobPage.this.getProject();
					}

					@Override
					protected String getRevision() {
						return state.blobIdent.revision;
					}
					
				};
			}

		});
		
		if (target != null) {
			replace(blobOperations);
			target.add(blobOperations);
		} else {
			add(blobOperations);
		}
	}

	private QuickSearchPanel newQuickSearchPanel(String id, ModalPanel modal) {
		return new QuickSearchPanel(id, projectModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return state.blobIdent.revision;
			}
			
		}) {

			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				BlobIdent selected = new BlobIdent(state.blobIdent.revision, hit.getBlobPath(), 
						FileMode.REGULAR_FILE.getBits()); 
				ProjectBlobPage.this.onSelect(target, selected, SourceRendererProvider.getPosition(hit.getTokenPos()));
				modal.close();
			}
			
			@Override
			protected void onMoreQueried(AjaxRequestTarget target, List<QueryHit> hits) {
				newSearchResult(target, hits);
				resizeWindow(target);
				modal.close();
			}

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				modal.close();
			}
			
		};
	}
	
	private AdvancedSearchPanel advancedSearchPanel;
	
	private ModalPanel advancedSearchPanelModal;
	
	private AdvancedSearchPanel newAdvancedSearchPanel(String id, ModalPanel modal) {
		/*
		 * Re-use advanced search panel instance so that search options can be preserved in the page
		 */
		advancedSearchPanelModal = modal;
		if (advancedSearchPanel == null) {
			advancedSearchPanel = new AdvancedSearchPanel(id, projectModel, new AbstractReadOnlyModel<String>() {
	
				@Override
				public String getObject() {
					return state.blobIdent.revision;
				}
				
			}) {
	
				@Override
				protected void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
					newSearchResult(target, hits);
					resizeWindow(target);
					advancedSearchPanelModal.close();
				}
	
				@Override
				protected void onCancel(AjaxRequestTarget target) {
					advancedSearchPanelModal.close();
				}
	
				@Override
				protected BlobIdent getCurrentBlob() {
					return state.blobIdent;
				}
				
			};
		} 
		return advancedSearchPanel;
	}
	
	private void newBlobContent(@Nullable AjaxRequestTarget target) {
		PrioritizedComponentRenderer mostPrioritizedRenderer = null;
		for (BlobRendererContribution contribution: OneDev.getExtensions(BlobRendererContribution.class)) {
			PrioritizedComponentRenderer renderer = contribution.getRenderer(this);
			if (renderer != null) {
				if (mostPrioritizedRenderer == null || mostPrioritizedRenderer.getPriority() > renderer.getPriority())
					mostPrioritizedRenderer = renderer;
			}
		}
		Component blobContent = Preconditions.checkNotNull(mostPrioritizedRenderer).render(BLOB_CONTENT_ID);
		if (target != null) {
			replace(blobContent);
			target.add(blobContent);
		} else {
			add(blobContent);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getProject(), state);
		CharSequence url = RequestCycle.get().urlFor(ProjectBlobPage.class, params);
		pushState(target, url.toString(), state);
	}
	
	private void newCommitStatus(@Nullable AjaxRequestTarget target) {
		Component commitStatus;
		if (resolvedRevision != null) {
			commitStatus = new CommitStatusPanel("buildStatus", resolvedRevision, getRefName()) {

				@Override
				protected Project getProject() {
					return ProjectBlobPage.this.getProject();
				}

				@Override
				protected PullRequest getPullRequest() {
					return null;
				}
				
			};
		} else {
			commitStatus = new WebMarkupContainer("buildStatus").add(AttributeAppender.append("class", "d-none"));
		}
		
		commitStatus.setOutputMarkupPlaceholderTag(true);
		
		if (target != null) {
			replace(commitStatus);
			target.add(commitStatus);
		} else {
			add(commitStatus);
		}
	}
	
	private void newBuildSupportNote(@Nullable AjaxRequestTarget target) {
		Component buildSupportNote = new WebMarkupContainer("buildSupportNote") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				String branch = state.blobIdent.revision;
				if (branch == null)
					branch = "master";
				if (SecurityUtils.canModify(getProject(), branch, BuildSpec.BLOB_PATH)) {
					add(new ViewStateAwareAjaxLink<Void>("addFile") {
	
						@Override
						public void onClick(AjaxRequestTarget target) {
							onModeChange(target, Mode.ADD, BuildSpec.BLOB_PATH);
						}

						@Override
						public IModel<?> getBody() {
							return Model.of("adding " + BuildSpec.BLOB_PATH);
						}
						
					});
				} else {
					add(new Label("addFile", "adding " + BuildSpec.BLOB_PATH) {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					});
				}
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (resolvedRevision != null && isOnBranch() && state.blobIdent.path == null && state.mode == Mode.VIEW) {
					BlobIdent oldBlobIdent = new BlobIdent(resolvedRevision.name(), ".onedev-buildspec", FileMode.TYPE_FILE);
					BlobIdent blobIdent = new BlobIdent(resolvedRevision.name(), BuildSpec.BLOB_PATH, FileMode.TYPE_FILE);
					setVisible(getProject().getBlob(blobIdent, false) == null && getProject().getBlob(oldBlobIdent, false) == null);
				} else {
					setVisible(false);
				}
			}
			
		};
		
		if (target != null) {
			replace(buildSupportNote);
			target.add(buildSupportNote);
		} else {
			add(buildSupportNote);
		}
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		String revision = state.blobIdent.revision;
		boolean canCreateRef;
		if (revision == null) {
			revision = "master";
			canCreateRef = false;
		} else {
			canCreateRef = true;
		}
		
		Component revisionPicker = new RevisionPicker(REVISION_PICKER_ID, projectModel, revision, canCreateRef) {
	
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (isOnBranch())
					tag.put("title", "Press 'y' to get permalink");
			}

			@Override
			protected String getRevisionUrl(String revision) {
				BlobIdent blobIdent = new BlobIdent(revision, null, FileMode.TREE.getBits());
				State state = new State(blobIdent);
				PageParameters params = ProjectBlobPage.paramsOf(projectModel.getObject(), state);
				return urlFor(ProjectBlobPage.class, params).toString();
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				BlobIdent newBlobIdent = new BlobIdent(state.blobIdent);
				newBlobIdent.revision = revision;
				if (newBlobIdent.path != null) {
					try (RevWalk revWalk = new RevWalk(getProject().getRepository())) {
						RevTree revTree = getProject().getRevCommit(revision, true).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(getProject().getRepository(), newBlobIdent.path, revTree);
						if (treeWalk != null) {
							newBlobIdent.mode = treeWalk.getRawMode(0);
						} else {
							newBlobIdent.path = null;
							newBlobIdent.mode = FileMode.TREE.getBits();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
				ProjectBlobPage.this.onSelect(target, newBlobIdent, null);
			}
			
		}; 
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
		}
	}
	
	/*
	 * This method represents changing of resolved revision, instead of the named revision. 
	 * It is possible that resolved revision changes while named revision remains unchanged. 
	 * For instance when a file on a branch is edited from this page, the named revision 
	 * remains unchanged (still point to the branch), but the underlying resolved revision 
	 * (the commit) has been changed 
	 *   
	 * @param target
	 */
	private void onResolvedRevisionChange(AjaxRequestTarget target) {
		/* 
		 * A hack to reset resolved revision to null to disable getObjectIdCache()
		 * temporarily as otherwise getObjectId() method below will always 
		 * resolved to existing value of resolvedRevision
		 */
		resolvedRevision = null;
		resolvedRevision = getProject().getRevCommit(state.blobIdent.revision, true).copy();
		
		OneDev.getInstance(WebSocketManager.class).observe(this);
		newRevisionPicker(target);
		newCommitStatus(target);
		target.add(revisionIndexing);
		newBlobNavigator(target);
		newBlobOperations(target);
		newBuildSupportNote(target);
		newBlobContent(target);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new ProjectBlobResourceReference()));
		
		String callback = ajaxBehavior.getCallbackFunction(explicit("action")).toString();
		String script = String.format("onedev.server.projectBlob.onDomReady(%s);", callback);
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	public static State getState(CodeComment comment) {
		BlobIdent blobIdent = new BlobIdent(comment.getMark().getCommitHash(), comment.getMark().getPath(), 
				FileMode.REGULAR_FILE.getBits());
		State state = new State(blobIdent);
		state.commentId = comment.getId();
		state.position = SourceRendererProvider.getPosition(comment.getMark().getRange());
		return state;
	}	
	
	public static PageParameters paramsOf(CodeComment comment) {
		return paramsOf(comment.getProject(), getState(comment));
	}	
	
	public static PageParameters paramsOf(Project project, BlobIdent blobIdent) {
		return paramsOf(project, new State(blobIdent));
	}
	
	public static void fillParams(PageParameters params, State state) {
		if (state.blobIdent.revision != null)
			params.add(PARAM_REVISION, state.blobIdent.revision);
		if (state.blobIdent.path != null)
			params.add(PARAM_PATH, state.blobIdent.path);
		if (state.position != null)
			params.add(PARAM_POSITION, state.position.toString());
		if (state.requestId != null)
			params.add(PARAM_REQUEST, state.requestId);
		if (state.commentId != null)
			params.add(PARAM_COMMENT, state.commentId);
		if (state.mode != Mode.VIEW)
			params.add(PARAM_MODE, state.mode.name().toLowerCase());
		if (state.mode == Mode.VIEW && state.viewPlain)
			params.add(PARAM_VIEW_PLAIN, true);
		if (state.urlBeforeEdit != null)
			params.add(PARAM_URL_BEFORE_EDIT, state.urlBeforeEdit);
		if (state.urlAfterEdit != null)
			params.add(PARAM_URL_AFTER_EDIT, state.urlAfterEdit);
		if (state.coverageReport != null)
			params.add(PARAM_COVERAGE_REPORT, state.coverageReport);
		if (state.problemReport != null)
			params.add(PARAM_PROBLEM_REPORT, state.problemReport);
			
		if (state.query != null)
			params.add(PARAM_QUERY, state.query);
		if (state.initialNewPath != null)
			params.add(PARAM_INITIAL_NEW_PATH, state.initialNewPath);
	}
	
	public static PageParameters paramsOf(Project project, State state) {
		PageParameters params = paramsOf(project);
		fillParams(params, state);
		return params;
	}
	
	private void newSearchResult(@Nullable AjaxRequestTarget target, @Nullable List<QueryHit> hits) {
		Component content;
		if (hits != null) {
			content = new SearchResultPanel("content", this, hits) {
				
				@Override
				protected void onClose(AjaxRequestTarget target) {
					newSearchResult(target, null);
					resizeWindow(target);
				}
				
			};
			if (target != null) {
				target.appendJavaScript(""
						+ "$('.project-blob>.search-result').css('display', 'flex'); "
						+ "$('.project-blob .search-result>.body').focus();");
			}
		} else {
			content = new WebMarkupContainer("content").setOutputMarkupId(true);
			if (target != null) 
				target.appendJavaScript("$('.project-blob>.search-result').hide();");
			else 
				searchResult.add(AttributeAppender.replace("style", "display: none;"));
		}
		if (target != null) {
			searchResult.replace(content);
			target.add(content);
		} else {
			searchResult.add(content);
		}
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		State popState = (State) data;
		if (!popState.blobIdent.revision.equals(state.blobIdent.revision)) {
			state = popState;
			newSearchResult(target, null);
			onResolvedRevisionChange(target);
		} else {
			state = popState;
			newBlobNavigator(target);
			newBlobOperations(target);
			newBuildSupportNote(target);
			newBlobContent(target);
		}
	}
	
	@Override
	public BlobIdent getBlobIdent() {
		return state.blobIdent;
	}
	
	@Override
	public String getPosition() {
		return state.position;
	}

	@Override
	public void onPosition(AjaxRequestTarget target, String position) {
		state.position = position;
		pushState(target);
	}

	@Override
	public String getPositionUrl(String position) {
		State positionState = SerializationUtils.clone(state);
		positionState.blobIdent.revision = resolvedRevision.name();
		positionState.commentId = null;
		positionState.position = position;
		PageParameters params = paramsOf(getProject(), positionState);		
		return RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
	}
	
	@Override
	public Mode getMode() {
		return state.mode;
	}

	@Override
	public boolean isViewPlain() {
		return state.viewPlain;
	}

	@Override
	public String getUrlBeforeEdit() {
		return state.urlBeforeEdit;
	}

	@Override
	public String getUrlAfterEdit() {
		return state.urlAfterEdit;
	}
	
	@Override
	public PageParameters getParamsBeforeEdit() {
		return paramsOf(getProject(), state);
	}

	@Override
	public PageParameters getParamsAfterEdit() {
		return paramsOf(getProject(), state);
	}

	@Override
	public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable String position) {
		String prevPosition = state.position;
		state.position = position;
		if (!blobIdent.revision.equals(state.blobIdent.revision)) {
			state.blobIdent = blobIdent;
			state.mode = Mode.VIEW;
			state.commentId = null;
			state.requestId = null;
			newSearchResult(target, null);
			onResolvedRevisionChange(target);
			resizeWindow(target);
		} else if (!Objects.equal(state.blobIdent.path, blobIdent.path)) {
			state.blobIdent.path = blobIdent.path;
			state.blobIdent.mode = blobIdent.mode;
			state.mode = Mode.VIEW;
			state.commentId = null;
			newBlobNavigator(target);
			newBlobOperations(target);
			newBuildSupportNote(target);
			newBlobContent(target);
			resizeWindow(target);
			OneDev.getInstance(WebSocketManager.class).observe(this);
		} else if (state.position != null) {
			if (get(BLOB_CONTENT_ID) instanceof Positionable) {
				// This logic is added for performance reason, we do not want to 
				// reload the file if go to different mark positions in same file
				((Positionable)get(BLOB_CONTENT_ID)).position(target, state.position);
			} else {
				state.mode = Mode.VIEW;
				newBlobOperations(target);
				newBuildSupportNote(target);
				newBlobContent(target);
				resizeWindow(target);
			}
		} else if (prevPosition != null) {
			state.mode = Mode.VIEW;
			newBlobOperations(target);
			newBuildSupportNote(target);
			newBlobContent(target);
			resizeWindow(target);
		}
		pushState(target);
	}

	@Override
	public void pushState(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable String position) {
		state.blobIdent = blobIdent;
		state.position = position;
		pushState(target);
	}
	
	@Override
	public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
		newSearchResult(target, hits);
		resizeWindow(target);
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment, PlanarRange range) {
		state.commentId = comment.getId();
		state.position = SourceRendererProvider.getPosition(range);
		pushState(target);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		state.commentId = null;
		state.position = null;
		pushState(target);
	}
	
	@Override
	public void onAddComment(AjaxRequestTarget target, PlanarRange range) {
		state.commentId = null;
		state.position = SourceRendererProvider.getPosition(range);
		pushState(target);
	}
	
	@Override
	public boolean isOnBranch() {
		return state.blobIdent.revision == null || getProject().getBranchRef(state.blobIdent.revision) != null;
	}

	@Override
	public String getRefName() {
		if (state.blobIdent.revision != null)
			return getProject().getRefName(state.blobIdent.revision);
		else
			return null;
	}
	
	@Override
	public RevCommit getCommit() {
		if (resolvedRevision != null)
			return getProject().getRevCommit(resolvedRevision, true);
		else
			return null;
	}
	
	public static class State implements Serializable {
		
		private static final long serialVersionUID = 1L;

		public BlobIdent blobIdent;
		
		public Long requestId;
		
		public Long commentId;
		
		public String position;
		
		public Mode mode = Mode.VIEW;
		
		/*
		 * Some blob can be rendered in a way for easier understanding, such as .onedev-buildspec.yml, 
		 * In these cases, the VIEW_PLAIN mode enables to view plain text of the blob. Applicable
		 * only when mode is VIEW 
		 */
		public boolean viewPlain;
		
		public String coverageReport;
		
		public String problemReport;
		
		public String urlBeforeEdit;
		
		public String urlAfterEdit;
		
		public boolean renderSource;
		
		public String query;
		
		public String initialNewPath;

		public State(BlobIdent blobIdent) {
			this.blobIdent = blobIdent;
		}

		public State() {
			blobIdent = new BlobIdent();
		}
		
	}

	@Override
	public void onModeChange(AjaxRequestTarget target, Mode mode, @Nullable String newPath) {
		onModeChange(target, mode, false, newPath);
	}
	
	@Override
	public void onModeChange(AjaxRequestTarget target, Mode mode, boolean viewPlain, @Nullable String newPath) {
		state.viewPlain = viewPlain;
		state.initialNewPath = newPath;
		
		/*
		 * User might be changing blob name when adding a file, and onModeChange will be called. 
		 * In this case, we only need to re-create blob content
		 */
		if (mode != Mode.ADD || state.mode != Mode.ADD) {
			state.mode = mode;
			pushState(target);
			if (state.mode == Mode.VIEW || state.mode == Mode.EDIT || state.mode == Mode.ADD) {
				newBlobNavigator(target);
				newBlobOperations(target);
			}
		}			
		newBuildSupportNote(target);
		newBlobContent(target);
		resizeWindow(target);
	}

	@Override
	public void onCommitted(@Nullable AjaxRequestTarget target, RefUpdated refUpdated, @Nullable String position) {
		Project project = getProject();
		if (state.blobIdent.revision == null) {
			state.blobIdent.revision = "master";
			resolvedRevision = refUpdated.getNewCommitId();
			project.setDefaultBranch("master");
		}
		String branch = state.blobIdent.revision;
		
		getProject().cacheObjectId(branch, refUpdated.getNewCommitId());

		Long projectId = project.getId();
		String refName = refUpdated.getRefName();
		ObjectId oldCommitId = refUpdated.getOldCommitId();
		ObjectId newCommitId = refUpdated.getNewCommitId();
		
		/*
		 * Update pull request as this is a direct consequence of editing source branch. Also this 
		 * is necessary to show the latest changes of the pull request after editing 
		 */
		PullRequest request = getPullRequest();
		if (request != null)
			OneDev.getInstance(PullRequestUpdateManager.class).checkUpdate(request);
		
		OneDev.getInstance(TransactionManager.class).runAfterCommit(new Runnable() {

			@Override
			public void run() {
				OneDev.getInstance(SessionManager.class).runAsync(new Runnable() {

					@Override
					public void run() {
						Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
						project.cacheObjectId(branch, newCommitId);
						RefUpdated refUpdated = new RefUpdated(project, refName, oldCommitId, newCommitId);
						OneDev.getInstance(ListenerRegistry.class).post(refUpdated);
					}
					
				});
			}
			
		});
		
		if (target != null) {
			if (state.urlAfterEdit != null) {
				throw new RedirectToUrlException(state.urlAfterEdit);
			} else {
				BlobIdent newBlobIdent;
				if (state.mode == Mode.DELETE) {
					try (RevWalk revWalk = new RevWalk(getProject().getRepository())) {
						RevTree revTree = getProject().getRevCommit(refUpdated.getNewCommitId(), true).getTree();
						String parentPath = StringUtils.substringBeforeLast(state.blobIdent.path, "/");
						while (TreeWalk.forPath(getProject().getRepository(), parentPath, revTree) == null) {
							if (parentPath.contains("/")) {
								parentPath = StringUtils.substringBeforeLast(parentPath, "/");
							} else {
								parentPath = null;
								break;
							}
						}
						newBlobIdent = new BlobIdent(branch, parentPath, FileMode.TREE.getBits());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}	
				} else if (state.mode == Mode.ADD) {
					newBlobIdent = new BlobIdent(branch, getNewPath(), FileMode.REGULAR_FILE.getBits());
				} else if (state.mode == Mode.EDIT) {
					newBlobIdent = new BlobIdent(branch, getNewPath(), FileMode.REGULAR_FILE.getBits());
				} else {
					// We've uploaded some files
					newBlobIdent = null;
				}
				
				if (newBlobIdent != null) {
					state.blobIdent = newBlobIdent;
					state.position = position;
					state.commentId = null;
					state.mode = Mode.VIEW;
					onResolvedRevisionChange(target);
					pushState(target);
				} else {
					state.mode = Mode.VIEW;
					onResolvedRevisionChange(target);
				}
		
				// fix the issue that sometimes indexing indicator of new commit does not disappear 
				target.appendJavaScript("Wicket.WebSocket.send('RenderCallback');");	    			
			}
		}
	}
	
	@Override
	public String getInitialNewPath() {
		return state.initialNewPath;
	}
	
	@Override
	public String getDirectory() {
		String path;
		if (state.mode == Mode.ADD || state.mode == Mode.EDIT) {
			path = getNewPath();
			if (path != null) {
				if (path.contains("/"))
					path = StringUtils.substringBeforeLast(path, "/");
				else
					path = null;
			} else {
				throw new IllegalStateException();
			}
		} else if (state.blobIdent.isTree()) {
			path = state.blobIdent.path;
		} else if (state.blobIdent.path.contains("/")) {
			path = StringUtils.substringBeforeLast(state.blobIdent.path, "/");
		} else {
			path = null;
		}
		return path;
	}
	
	@Override
	public String getDirectoryUrl() {
		String revision = state.blobIdent.revision;
		BlobIdent blobIdent = new BlobIdent(revision, getDirectory(), FileMode.TREE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		return urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject(), state)).toString();
	}

	@Override
	public String getRootDirectoryUrl() {
		BlobIdent blobIdent = new BlobIdent(state.blobIdent.revision, null, FileMode.TREE.getBits());
		return RequestCycle.get().urlFor(ProjectBlobPage.class, 
				ProjectBlobPage.paramsOf(getProject(), blobIdent)).toString();
	}
	
	@Override
	public String getNewPath() {
		BlobNavigator blobNavigator = (BlobNavigator) get(BLOB_NAVIGATOR_ID);
		return blobNavigator.getNewPath();
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof RevisionResolved) {
			RevisionResolved revisionResolveEvent = (RevisionResolved) event.getPayload();
			resolvedRevision = revisionResolveEvent.getResolvedRevision();
		} 
	}

	@Override
	public String getCoverageReport() {
		return state.coverageReport;
	}

	@Override
	public String getProblemReport() {
		return state.problemReport;
	}

	@Override
	public String getAutosaveKey() {
		if (state.mode == Mode.ADD) {
			return String.format("autosave:addBlob:%d:%s:%s", 
					getProject().getId(), state.blobIdent.revision, getNewPath());
		} else if (state.mode == Mode.EDIT) {
			return String.format("autosave:editBlob:%d:%s:%s:%s", 
					getProject().getId(), state.blobIdent.revision, 
					state.blobIdent.path, getProject().getBlob(state.blobIdent, true).getBlobId());
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public RefUpdated uploadFiles(Collection<FileUpload> uploads, String directory, String commitMessage) {
		Map<String, BlobContent> newBlobs = new HashMap<>();
		
		String parentPath = getDirectory();
		
		if (directory != null) { 
			if (parentPath != null)
				parentPath += "/" + directory;
			else
				parentPath = directory;
		}
		
		User user = Preconditions.checkNotNull(SecurityUtils.getUser());
		BlobIdent blobIdent = getBlobIdent();
		
		for (FileUpload upload: uploads) {
			String blobPath = FilenameUtils.sanitizeFilename(upload.getClientFileName());
			if (parentPath != null)
				blobPath = parentPath + "/" + blobPath;
			
			if (getProject().isReviewRequiredForModification(user, blobIdent.revision, blobPath)) 
				throw new BlobEditException("Review required for this change. Please submit pull request instead");
			else if (getProject().isBuildRequiredForModification(user, blobIdent.revision, blobPath)) 
				throw new BlobEditException("Build required for this change. Please submit pull request instead");
			
			BlobContent blobContent = new BlobContent.Immutable(upload.getBytes(), FileMode.REGULAR_FILE);
			newBlobs.put(blobPath, blobContent);
		}

		BlobEdits blobEdits = new BlobEdits(Sets.newHashSet(), newBlobs);
		String refName = blobIdent.revision!=null?GitUtils.branch2ref(blobIdent.revision):"refs/heads/master";

		ObjectId prevCommitId;
		if (blobIdent.revision != null)
			prevCommitId = getProject().getRevCommit(blobIdent.revision, true).copy();
		else
			prevCommitId = ObjectId.zeroId();

		while (true) {
			try {
				ObjectId newCommitId = blobEdits.commit(getProject().getRepository(), refName, prevCommitId, 
						prevCommitId, user.asPerson(), commitMessage);
				return new RefUpdated(getProject(), refName, prevCommitId, newCommitId);
			} catch (ObjectAlreadyExistsException|NotTreeException e) {
				throw new BlobEditException(e.getMessage());
			} catch (ObsoleteCommitException e) {
				prevCommitId = e.getOldCommitId();
			}
		}
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	public String appendRaw(String url) {
		try {
			URIBuilder builder;
			builder = new URIBuilder(url);
			for (NameValuePair pair: builder.getQueryParams()) {
				if (pair.getName().equals(PARAM_RAW))
					return url;
			}
			return builder.addParameter(PARAM_RAW, "true").build().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ScriptIdentity getScriptIdentity() {
		if (getBlobIdent().revision != null)
			return new JobIdentity(getProject(), getCommit().copy());
		else // when we add file to an empty project
			return new JobIdentity(getProject(), null);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Files");
	}

}
