package io.onedev.server.web.page.project.pullrequests.detail;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.search.entity.EntityQuery.quote;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;
import static io.onedev.server.search.entity.build.BuildQueryLexer.RequiredByPullRequest;
import static io.onedev.server.util.BuildConstants.FIELD_JOB;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.APPROVE;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.DELETE_SOURCE_BRANCH;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.DISCARD;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.REOPEN;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.REQUEST_FOR_CHANGES;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.RESTORE_SOURCE_BRANCH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.cache.UserInfoManager;
import io.onedev.server.ci.CISpec;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.build.simplelist.SimpleBuildListPanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.component.review.ReviewListPanel;
import io.onedev.server.web.component.sideinfo.SideInfoClosed;
import io.onedev.server.web.component.sideinfo.SideInfoOpened;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.codecomments.PullRequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.detail.mergepreview.MergePreviewPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QueryPositionSupport;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public abstract class PullRequestDetailPage extends ProjectPage {

	public static final String PARAM_REQUEST = "request";
	
	private static final String HINT_ID = "hint";
	
	protected final IModel<PullRequest> requestModel;
	
	private final QueryPosition position;
	
	private boolean isEditingTitle;
	
	private String title;
	
	private Long reviewUpdateId;
	
	private MergeStrategy mergeStrategy;
	
	public PullRequestDetailPage(PageParameters params) {
		super(params);
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				Long requestNumber = params.get(PARAM_REQUEST).toLong();
				PullRequest request = getPullRequestManager().find(getProject(), requestNumber);
				if (request == null)
					throw new EntityNotFoundException("Unable to find request #" + requestNumber + " in project " + getProject());
				return request;
			}

		};
		
		if (!getPullRequest().isValid())
			throw new RestartResponseException(InvalidPullRequestPage.class, InvalidPullRequestPage.paramsOf(getPullRequest()));
			
		reviewUpdateId = requestModel.getObject().getLatestUpdate().getId();
		
		position = QueryPosition.from(params);
	}

	private PullRequestManager getPullRequestManager() {
		return OneDev.getInstance(PullRequestManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		PullRequest request = getPullRequest();

		WebMarkupContainer requestHead = new WebMarkupContainer("requestHead");
		requestHead.setOutputMarkupId(true);
		add(requestHead);
		
		requestHead.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "#" + getPullRequest().getNumber() + " - " + getPullRequest().getTitle();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isEditingTitle);
			}
			
		});
		
		requestHead.add(new AjaxLink<Void>("editLink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				isEditingTitle = true;
				
				target.add(requestHead);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!isEditingTitle && SecurityUtils.canModify(getPullRequest()));
			}
			
		});

		Form<?> form = new Form<Void>("editForm") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isEditingTitle);
			}
			
		};
		requestHead.add(form);
		
		title = getPullRequest().getTitle();
		form.add(new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return title;
			}

			@Override
			public void setObject(String object) {
				title = object;
			}
			
		}));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (StringUtils.isNotBlank(title)) {
					OneDev.getInstance(PullRequestChangeManager.class).changeTitle(getPullRequest(), title, SecurityUtils.getUser());
					send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
					isEditingTitle = false;
				}

				target.add(requestHead);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				isEditingTitle = false;
				target.add(requestHead);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
		});
		
		add(newStatusAndBranchesContainer());

		WebMarkupContainer summaryContainer = new WebMarkupContainer("requestSummary") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					for (Component child: this) {
						if (child instanceof MarkupContainer) {
							MarkupContainer container = (MarkupContainer) child;
							Form<?> form = container.visitChildren(Form.class, new IVisitor<Form<?>, Form<?>>() {

								@Override
								public void component(Form<?> object, IVisit<Form<?>> visit) {
									visit.stop(object);
								}
								
							});
							if (form == null) {
								pageDataChanged.getHandler().add(child);
							}
						} else if (!(child instanceof Form)) {
							pageDataChanged.getHandler().add(child);
						}
					}
					WicketUtils.markLastVisibleChild(this);
					pageDataChanged.getHandler().appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
				}
			}

			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
				WicketUtils.markLastVisibleChild(this);
			}

		};
		summaryContainer.setOutputMarkupPlaceholderTag(true);
		add(summaryContainer);

		summaryContainer.add(new Label("checkError", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getCheckError();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getCheckError() != null);
			}
			
		}.setOutputMarkupPlaceholderTag(true));

		summaryContainer.add(new WebMarkupContainer("discardedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isDiscarded());
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		summaryContainer.add(newMergedNoteContainer());
		summaryContainer.add(newMergeStatusContainer());
		summaryContainer.add(new WebMarkupContainer("doNotMerge") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen() && getPullRequest().getMergeStrategy() == MergeStrategy.DO_NOT_MERGE);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		summaryContainer.add(newOperationsContainer());
		
		WicketUtils.markLastVisibleChild(summaryContainer);
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new RequestTab("Activities", PullRequestActivitiesPage.class) {

			@Override
			protected Component renderOptions(String componentId) {
				PullRequestActivitiesPage page = (PullRequestActivitiesPage) getPage();
				return page.renderOptions(componentId);
			}
			
		});
		tabs.add(new RequestTab("File Changes", PullRequestChangesPage.class));
		tabs.add(new RequestTab("Code Comments", PullRequestCodeCommentsPage.class));
		if (request.isOpen())
			tabs.add(new RequestTab("Merge Preview", MergePreviewPage.class));
		
		add(new Tabbable("requestTabs", tabs).setOutputMarkupId(true));
		
		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) 
					OneDev.getInstance(UserInfoManager.class).visitPullRequest(SecurityUtils.getUser(), getPullRequest());
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});
		
		add(new SideInfoPanel("moreInfo") {

			@Override
			protected Component newContent(String componentId) {
				Fragment fragment = new Fragment(componentId, "moreInfoFrag", PullRequestDetailPage.this) {

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);

						if (event.getPayload() instanceof PageDataChanged) {
							PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
							IPartialPageRequestHandler partialPageRequestHandler = pageDataChanged.getHandler();
							partialPageRequestHandler.add(this);
						}
						
					}

				};
				fragment.add(new EntityNavPanel<PullRequest>("requestNav") {

					@Override
					protected EntityQuery<PullRequest> parse(String queryString) {
						return PullRequestQuery.parse(getProject(), queryString, true);
					}

					@Override
					protected PullRequest getEntity() {
						return getPullRequest();
					}

					@Override
					protected List<PullRequest> query(EntityQuery<PullRequest> query, int offset, int count) {
						return getPullRequestManager().query(getProject(), getLoginUser(), query, offset, count);
					}

					@Override
					protected QueryPositionSupport<PullRequest> getQueryPositionSupport() {
						return new QueryPositionSupport<PullRequest>() {

							@Override
							public QueryPosition getPosition() {
								return position;
							}

							@Override
							public void navTo(AjaxRequestTarget target, PullRequest entity, QueryPosition position) {
								PageParameters params = PullRequestDetailPage.paramsOf(entity, position);
								setResponsePage(getPageClass(), params);
							}
							
						};
					}
					
				});
				
				fragment.add(newMergeStrategyContainer());
				fragment.add(new ReviewListPanel("reviews", requestModel));
				
				fragment.add(new ListView<String>("jobs", new LoadableDetachableModel<List<String>>() {

					@Override
					protected List<String> load() {
						PullRequest request = getPullRequest();
						MergePreview preview = request.getMergePreview();
						if (preview != null && preview.getMerged() != null) {
							CISpec ciSpec = request.getTargetProject().getCISpec(ObjectId.fromString(preview.getMerged()));
							if (ciSpec != null) {
								Set<String> pullRequestJobNames = request.getPullRequestBuilds()
										.stream()
										.map(it->it.getBuild().getJobName())
										.collect(Collectors.toSet());
								return ciSpec.getSortedJobs()
										.stream()
										.map(it->it.getName())
										.filter(it->pullRequestJobNames.contains(it))
										.collect(Collectors.toList());
							}
						} 
						return new ArrayList<>();
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<String> item) {
						PullRequest request = getPullRequest();
						MergePreview preview = request.getMergePreview();
						Preconditions.checkState(preview != null && preview.getMerged() != null);
						String jobName = item.getModelObject();

						Status status = Status.getOverallStatus(request.getPullRequestBuilds()
								.stream()
								.filter(it->it.getBuild().getJobName().equals(jobName))
								.map(it->it.getBuild().getStatus())
								.collect(Collectors.toSet()));
						
						WebMarkupContainer link = new DropdownLink("job") {

							@Override
							protected Component newContent(String id, FloatingPanel dropdown) {
								return new SimpleBuildListPanel(id, new LoadableDetachableModel<List<Build>>() {

									@Override
									protected List<Build> load() {
										List<Build> builds = getPullRequest().getPullRequestBuilds()
												.stream()
												.map(it->it.getBuild())
												.filter(it->it.getJobName().equals(jobName))
												.collect(Collectors.toList());
										Collections.sort(builds);
										return builds;
									}
									
								}) {
									
									@Override
									protected Component newListLink(String componentId) {
										String query = "" 
												+ getRuleName(RequiredByPullRequest) + " " + quote("#" + getPullRequest().getNumber()) 
												+ " " + getRuleName(And) + " "
												+ quote(FIELD_JOB) + " " + getRuleName(Is) + " " + quote(jobName);
										return new BookmarkablePageLink<Void>(componentId, ProjectBuildsPage.class, 
												ProjectBuildsPage.paramsOf(getPullRequest().getTargetProject(), query, 0));
									}
									
								};
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								String title;
								if (status != null) {
									if (status != Status.SUCCESSFUL)
										title = "Some builds are "; 
									else
										title = "Builds are "; 
									title += status.getDisplayName().toLowerCase() + ", click for details";
								} else {
									title = "No builds";
								}
								tag.put("title", title);
							}
							
						};
						
						link.add(new BuildStatusIcon("status", Model.of(status)));
						item.add(link);
						
						link.add(new Label("name", jobName));
						item.add(link);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!getModelObject().isEmpty());
					}
					
				});
				
				fragment.add(new EntityWatchesPanel("watches") {

					@Override
					protected void onSaveWatch(EntityWatch watch) {
						OneDev.getInstance(PullRequestWatchManager.class).save((PullRequestWatch) watch);
					}

					@Override
					protected void onDeleteWatch(EntityWatch watch) {
						OneDev.getInstance(PullRequestWatchManager.class).delete((PullRequestWatch) watch);
					}

					@Override
					protected AbstractEntity getEntity() {
						return getPullRequest();
					}
					
				});
				
				fragment.add(newManageContainer());

				fragment.setOutputMarkupId(true);
				return fragment;
			}
			
		});
	}
	
	private WebMarkupContainer newManageContainer() {
		WebMarkupContainer container = new WebMarkupContainer("manage");
		container.setVisible(SecurityUtils.canAdministrate(getPullRequest().getTargetProject().getFacade()));
		container.add(new Link<Void>("synchronize") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen());
			}

			@Override
			public void onClick() {
				getPullRequestManager().check(getPullRequest());
				if (getPullRequest().getCheckError() == null)
					Session.get().success("Pull request is synchronized");
			}
			
		});
		container.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				PullRequest request = getPullRequest();
				getPullRequestManager().delete(request);
				Session.get().success("Pull request #" + request.getNumber() + " is deleted");
				PageParameters params = ProjectPullRequestsPage.paramsOf(
						getProject(), 
						QueryPosition.getQuery(position), 
						QueryPosition.getPage(position) + 1);
				setResponsePage(ProjectPullRequestsPage.class, params);
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete this pull request?")));
		return container;
	}
	
	private WebMarkupContainer newMergeStrategyContainer() {
		WebMarkupContainer mergeStrategyContainer = new WebMarkupContainer("mergeStrategy");
		mergeStrategyContainer.setOutputMarkupId(true);

		mergeStrategy = getPullRequest().getMergeStrategy();
		
		IModel<MergeStrategy> mergeStrategyModel = new PropertyModel<MergeStrategy>(this, "mergeStrategy");
		
		List<MergeStrategy> mergeStrategies = Arrays.asList(MergeStrategy.values());
		DropDownChoice<MergeStrategy> editor = 
				new DropDownChoice<MergeStrategy>("editor", mergeStrategyModel, mergeStrategies) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getPullRequest().isMerged() && SecurityUtils.canModify(getPullRequest()));						
			}
			
		};
		editor.add(new OnChangeAjaxBehavior() {
					
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				OneDev.getInstance(PullRequestChangeManager.class).changeMergeStrategy(getPullRequest(), mergeStrategy, SecurityUtils.getUser());
				send(getPage(), Broadcast.BREADTH, new PageDataChanged(target));								
			}
			
		});
		mergeStrategyContainer.add(editor);
		
		mergeStrategyContainer.add(new Label("viewer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().toString();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().isMerged() || !SecurityUtils.canModify(getPullRequest()));						
			}
			
		});

		mergeStrategyContainer.add(new Label("help", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().getDescription();
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getPullRequest().isMerged() && SecurityUtils.canModify(getPullRequest()));						
			}
			
		});
		
		return mergeStrategyContainer;
	}
	
	private WebMarkupContainer newStatusAndBranchesContainer() {
		WebMarkupContainer statusAndBranchesContainer = new WebMarkupContainer("statusAndBranches");
		
		PullRequest request = getPullRequest();
		
		statusAndBranchesContainer.add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (request.isOpen())
					return "OPEN";
				else
					return request.getCloseInfo().getStatus().toString();
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				setOutputMarkupId(true);
				
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						if (request.isDiscarded())
							return " label-danger";
						else if (request.isMerged())
							return " label-success";
						else
							return " label-warning";
					}
					
				}));
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PageDataChanged) {
					PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
					pageDataChanged.getHandler().add(this);
				}
			}
			
		});
		
		UserIdent userIdent = UserIdent.of(UserFacade.of(request.getSubmitter()), request.getSubmitterName());
		statusAndBranchesContainer.add(new UserIdentPanel("user", userIdent, Mode.NAME));
		statusAndBranchesContainer.add(new Label("date", DateUtils.formatAge(request.getSubmitDate())));
		
		statusAndBranchesContainer.add(new BranchLink("target", request.getTarget(), null));
		if (request.getSourceProject() != null) {
			statusAndBranchesContainer.add(new BranchLink("source", request.getSource(), getPullRequest()));
		} else {
			statusAndBranchesContainer.add(new Label("source", "unknown") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			});
		}
		
		statusAndBranchesContainer.add(new AjaxLink<Void>("moreInfo") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SideInfoClosed) {
					SideInfoClosed moreInfoSideClosed = (SideInfoClosed) event.getPayload();
					setVisible(true);
					moreInfoSideClosed.getHandler().add(this);
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				target.add(this);
				send(getPage(), Broadcast.BREADTH, new SideInfoOpened(target));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		return statusAndBranchesContainer;
	}

	private WebMarkupContainer newMergeStatusContainer() {
		WebMarkupContainer mergeStatusContainer = new WebMarkupContainer("mergeStatus") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				setVisible(request.isOpen() && request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE);
			}
			
		};
		mergeStatusContainer.setOutputMarkupPlaceholderTag(true);
		
		mergeStatusContainer.add(new WebMarkupContainer("calculating") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getMergePreview() == null);
			}
			
		});
		mergeStatusContainer.add(new WebMarkupContainer("conflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new DropdownLink("resolveInstructions") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().getSource() != null);
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new ConflictResolveInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().getMergePreview();
				setVisible(preview != null && preview.getMerged() == null);
			}

		});
		mergeStatusContainer.add(new WebMarkupContainer("noConflict") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new DropdownLink("checkoutInstructions") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new CheckoutRequestInstructionPanel(id, new EntityModel<PullRequest>(getPullRequest()));
					}
					
				});
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().getMergePreview();
				setVisible(preview != null && preview.getMerged() != null);
			}

		});
		
		return mergeStatusContainer;
	}
	
	private WebMarkupContainer newOperationsContainer() {
		final WebMarkupContainer operationsContainer = new WebMarkupContainer("operations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasVisibleChildren = false;
				for (int i=0; i<size(); i++) {
					@SuppressWarnings("deprecation")
					Component child = get(i);
					child.configure();
					if (child.isVisible()) {
						hasVisibleChildren = true;
						break;
					}
				}
				
				setVisible(hasVisibleChildren);
			}
			
		};
		operationsContainer.setOutputMarkupPlaceholderTag(true);
		
		String confirmId = "confirm";
		
		operationsContainer.add(new AjaxLink<Void>("approve") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				operationsContainer.replace(newOperationConfirm(confirmId, APPROVE, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(APPROVE.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("requestForChanges") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				operationsContainer.replace(newOperationConfirm(confirmId, REQUEST_FOR_CHANGES, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(REQUEST_FOR_CHANGES.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}
			
		});
		
		operationsContainer.add(new AjaxLink<Void>("discard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, DISCARD, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DISCARD.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new AjaxLink<Void>("reopen") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, REOPEN, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(REOPEN.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new AjaxLink<Void>("deleteSourceBranch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, DELETE_SOURCE_BRANCH, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DELETE_SOURCE_BRANCH.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		operationsContainer.add(new AjaxLink<Void>("restoreSourceBranch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				operationsContainer.replace(newOperationConfirm(confirmId, RESTORE_SOURCE_BRANCH, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(RESTORE_SOURCE_BRANCH.canOperate(getPullRequest()) && !operationsContainer.get(confirmId).isVisible());
			}

		});
		
		operationsContainer.add(new WebMarkupContainer(confirmId).setVisible(false));
		
		return operationsContainer;
	}
	
	private Component newOperationConfirm(String id, PullRequestOperation operation, 
			WebMarkupContainer operationsContainer) {
		PullRequest request = getPullRequest();

		Fragment fragment = new Fragment(id, "operationConfirmFrag", this);
		Form<?> form = new Form<Void>("form");
		fragment.add(form);

		ProjectAndBranch source = request.getSource();
		Preconditions.checkNotNull(source);
		
		FormComponent<String> noteInput;
		form.add(noteInput = new CommentInput("note", Model.of(""), false) {

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(requestModel.getObject().getTargetProject(), 
						requestModel.getObject().getUUID());
			}

			@Override
			protected Project getProject() {
				return requestModel.getObject().getTargetProject();
			}

			@Override
			protected List<AttributeModifier> getInputModifiers() {
				return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a note"));
			}
			
		});
		
		WebMarkupContainer hint = new WebMarkupContainer(HINT_ID);
		hint.setOutputMarkupPlaceholderTag(true);
		hint.setVisible(false);
		fragment.add(hint);
		
		NotificationPanel feedback = new NotificationPanel("feedback", form);
		feedback.setOutputMarkupPlaceholderTag(true);
		fragment.add(feedback);
		
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				PullRequest request = getPullRequest();
				if ((operation == APPROVE || operation == REQUEST_FOR_CHANGES) && 
						!getPullRequest().getLatestUpdate().getId().equals(reviewUpdateId)) {
					Long prevReviewUpdateId = reviewUpdateId;
					WebMarkupContainer hint = new UnreviewedChangesPanel(HINT_ID, 
							new LoadableDetachableModel<PullRequestUpdate>() {

						@Override
						protected PullRequestUpdate load() {
							return OneDev.getInstance(PullRequestUpdateManager.class).load(prevReviewUpdateId);
						}
						
					});
					hint.setOutputMarkupPlaceholderTag(true);
					fragment.replace(hint);
					
					target.add(feedback);
					target.add(hint);
					target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
					reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				} else if (!operation.canOperate(request)) {
					error("Not allowed to " + getOperationName(operation) + " at this point");
					target.add(feedback);
					target.add(hint);
					target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
				} else {
					try {
						operation.operate(request, noteInput.getModelObject());
						setResponsePage(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(getPullRequest(), position));
					} catch (OneException e) {
						error(e.getMessage());
						target.add(feedback);
						target.add(hint);
						target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
					}
				}
			}

		}.add(AttributeModifier.replace("value", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Confirm " + getOperationName(operation);
			}
			
		})).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (operation == DISCARD)
					return "btn-danger";
				else 
					return "btn-primary";
			}
			
		})));
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				fragment.replaceWith(new WebMarkupContainer(id).setVisible(false));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
		});		
		
		return fragment;
	}
	
	private String getOperationName(PullRequestOperation operation) {
		return WordUtils.capitalizeFully(operation.name()).replace("_", " ").toLowerCase();		
	}

	private WebMarkupContainer newMergedNoteContainer() {
		WebMarkupContainer mergedNoteContainer = new WebMarkupContainer("mergedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isMerged());
			}
			
		};
		mergedNoteContainer.setOutputMarkupPlaceholderTag(true);
		
		mergedNoteContainer.add(new WebMarkupContainer("fastForwarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getLastMergePreview();
				setVisible(preview != null && preview.getRequestHead().equals(preview.getMerged()));
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("merged") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getLastMergePreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getMerged())
						&& (preview.getMergeStrategy() == CREATE_MERGE_COMMIT || preview.getMergeStrategy() == CREATE_MERGE_COMMIT_IF_NECESSARY));
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("mergedOutside") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getLastMergePreview() == null);
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("squashed") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getLastMergePreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getMerged())
						&& preview.getMergeStrategy() == SQUASH_SOURCE_BRANCH_COMMITS);
			}
			
		});
		mergedNoteContainer.add(new WebMarkupContainer("rebased") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				MergePreview preview = request.getMergePreview();
				setVisible(preview != null 
						&& !preview.getRequestHead().equals(preview.getMerged())
						&& preview.getMergeStrategy() == REBASE_SOURCE_BRANCH_COMMITS);
			}
			
		});
		
		return mergedNoteContainer;
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(PullRequest request, @Nullable QueryPosition position) {
		PageParameters params = ProjectPage.paramsOf(request.getTargetProject());
		params.add(PARAM_REQUEST, request.getNumber());
		if (position != null)
			position.fill(params);
		return params;
	}
	
	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	public QueryPosition getPosition() {
		return position;
	}
	
	@Override
	public Collection<String> getWebSocketObservables() {
		Collection<String> observables = super.getWebSocketObservables();
		observables.add(PullRequest.getWebSocketObservable(getPullRequest().getId()));
		return observables;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new PullRequestDetailCssResourceReference()));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	private class RequestTab extends PageTab {

		public RequestTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			if (getMainPageClass() == PullRequestCodeCommentsPage.class) {
				Fragment fragment = new Fragment(componentId, "codeCommentsTabLinkFrag", PullRequestDetailPage.this);
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", PullRequestCodeCommentsPage.class, paramsOf(getPullRequest(), position)) {

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof PageDataChanged) {
							((PageDataChanged)event.getPayload()).getHandler().add(this);
						}
					}
					
				};
				link.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						PullRequest request = getPullRequest();
						if (request.getLastCodeCommentActivityDate() != null 
								&& !request.isCodeCommentsVisitedAfter(request.getLastCodeCommentActivityDate())) {
							return "new";
						} else {
							return "";
						}
					}
					
				}));
				link.setOutputMarkupId(true);
				fragment.add(link);
				return fragment;
			} else {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getPullRequest(), position));
					}
					
				};
			}
		}
		
	}
	
}