package io.onedev.server.web.page.project.pullrequests.detail;

import static io.onedev.server.model.Build.FIELD_JOB;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.AssociatedWithPullRequest;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;
import static io.onedev.server.util.criteria.Criteria.quote;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.APPROVE;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.DELETE_SOURCE_BRANCH;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.DISCARD;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.REOPEN;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.REQUEST_FOR_CHANGES;
import static io.onedev.server.web.page.project.pullrequests.detail.PullRequestOperation.RESTORE_SOURCE_BRANCH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.infomanager.UserInfoManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
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
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.codecomments.PullRequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.detail.mergepreview.MergePreviewPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.ProjectAttachmentSupport;
import io.onedev.server.web.util.PullRequestAware;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.web.util.ReferenceTransformer;
import io.onedev.server.web.util.WicketUtils;

@SuppressWarnings("serial")
public abstract class PullRequestDetailPage extends ProjectPage implements PullRequestAware {

	public static final String PARAM_REQUEST = "request";
	
	private static final String HINT_ID = "hint";
	
	protected final IModel<PullRequest> requestModel;
	
	private final Cursor cursor;
	
	private boolean isEditingTitle;
	
	private String title;
	
	private Long reviewUpdateId;
	
	private MergeStrategy mergeStrategy;
	
	private PullRequestOperation activeOperation;
	
	public PullRequestDetailPage(PageParameters params) {
		super(params);
		
		String requestNumberString = params.get(PARAM_REQUEST).toString();
		if (StringUtils.isBlank(requestNumberString))
			throw new RestartResponseException(ProjectPullRequestsPage.class, ProjectPullRequestsPage.paramsOf(getProject(), null, 0));
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				Long requestNumber = Long.valueOf(requestNumberString);
				PullRequest request = getPullRequestManager().find(getProject(), requestNumber);
				if (request == null)
					throw new EntityNotFoundException("Unable to find pull request #" + requestNumber + " in project " + getProject());
				else if (!request.getTargetProject().equals(getProject()))
					throw new RestartResponseException(getPageClass(), paramsOf(request, cursor));
				else
					return request;
			}

		};
		
		if (!getPullRequest().isValid())
			throw new RestartResponseException(InvalidPullRequestPage.class, InvalidPullRequestPage.paramsOf(getPullRequest()));
			
		reviewUpdateId = requestModel.getObject().getLatestUpdate().getId();
		
		cursor = Cursor.from(params);
	}

	private PullRequestManager getPullRequestManager() {
		return OneDev.getInstance(PullRequestManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer requestHead = new WebMarkupContainer("requestHead");
		requestHead.setOutputMarkupId(true);
		add(requestHead);
		
		requestHead.add(new Label("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = getPullRequest();
				ReferenceTransformer transformer = new ReferenceTransformer(request.getTargetProject(), null);
				String transformed = transformer.apply(request.getTitle());
				return "#" + request.getNumber() + " - " + transformed;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isEditingTitle);
			}
			
		}.setEscapeModelStrings(false));
		
		requestHead.add(new AjaxLink<Void>("edit") {

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
		
		requestHead.add(new BookmarkablePageLink<Void>("create", 
				NewPullRequestPage.class, NewPullRequestPage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isEditingTitle);
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
			
		}).add(new ReferenceInputBehavior(false) {

			@Override
			protected Project getProject() {
				return PullRequestDetailPage.this.getProject();
			}
			
		}));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (StringUtils.isNotBlank(title)) {
					OneDev.getInstance(PullRequestChangeManager.class).changeTitle(getPullRequest(), title);
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
			protected void onBeforeRender() {
				super.onBeforeRender();
				WicketUtils.markLastVisibleChild(this);
			}

		};
		summaryContainer.add(new WebSocketObserver() {

			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
			}

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				for (Component child: summaryContainer) {
					if (child instanceof MarkupContainer) {
						MarkupContainer container = (MarkupContainer) child;
						Form<?> form = container.visitChildren(Form.class, new IVisitor<Form<?>, Form<?>>() {

							@Override
							public void component(Form<?> object, IVisit<Form<?>> visit) {
								visit.stop(object);
							}
							
						});
						if (form == null) {
							handler.add(child);
						}
					} else if (!(child instanceof Form)) {
						handler.add(child);
					}
				}
				WicketUtils.markLastVisibleChild(summaryContainer);
				handler.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
		});
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
		if (getPullRequest().isOpen())
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
				Fragment fragment = new Fragment(componentId, "moreInfoFrag", PullRequestDetailPage.this);
				fragment.add(new EntityNavPanel<PullRequest>("requestNav") {

					@Override
					protected EntityQuery<PullRequest> parse(String queryString) {
						return PullRequestQuery.parse(getProject(), queryString);
					}

					@Override
					protected PullRequest getEntity() {
						return getPullRequest();
					}

					@Override
					protected List<PullRequest> query(EntityQuery<PullRequest> query, int offset, int count) {
						return getPullRequestManager().query(getProject(), query, offset, count);
					}

					@Override
					protected CursorSupport<PullRequest> getCursorSupport() {
						return new CursorSupport<PullRequest>() {

							@Override
							public Cursor getCursor() {
								return cursor;
							}

							@Override
							public void navTo(AjaxRequestTarget target, PullRequest entity, Cursor cursor) {
								PageParameters params = PullRequestDetailPage.paramsOf(entity, cursor);
								setResponsePage(getPageClass(), params);
							}
							
						};
					}
					
				});
				
				fragment.add(newMergeStrategyContainer());
				fragment.add(new ReviewListPanel("reviews", requestModel));
				
				fragment.add(new ListView<JobBuilds>("jobs", new LoadableDetachableModel<List<JobBuilds>>() {

					@Override
					protected List<JobBuilds> load() {
						PullRequest request = getPullRequest();
						Map<String, List<PullRequestBuild>> map = new HashMap<>();
						for (PullRequestBuild pullRequestBuild: request.getPullRequestBuilds()) {
							String jobName = pullRequestBuild.getBuild().getJobName();
							List<PullRequestBuild> list = map.get(jobName);
							if (list == null) {
								list = new ArrayList<>();
								map.put(jobName, list);
							}
							list.add(pullRequestBuild);
						}
						List<JobBuilds> listOfJobBuilds = new ArrayList<>();
						for (Map.Entry<String, List<PullRequestBuild>> entry: map.entrySet()) {
							List<Build> builds = entry.getValue().stream().map(it->it.getBuild()).collect(Collectors.toList());
							Collections.sort(builds);
							listOfJobBuilds.add(new JobBuilds(entry.getKey(), entry.getValue().iterator().next().isRequired(), builds));
						}
						Collections.sort(listOfJobBuilds, new Comparator<JobBuilds>() {

							@Override
							public int compare(JobBuilds o1, JobBuilds o2) {
								return o1.getBuilds().iterator().next().getId().compareTo(o2.getBuilds().iterator().next().getId());
							}
							
						});
						return listOfJobBuilds;
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<JobBuilds> item) {
						JobBuilds jobBuilds = item.getModelObject();

						String jobName = jobBuilds.getJobName();
						Status status = Status.getOverallStatus(jobBuilds.getBuilds()
								.stream()
								.map(it->it.getStatus())
								.collect(Collectors.toSet()));
						
						WebMarkupContainer link = new DropdownLink("job") {

							@Override
							protected Component newContent(String id, FloatingPanel dropdown) {
								return new SimpleBuildListPanel(id, new LoadableDetachableModel<List<Build>>() {

									@Override
									protected List<Build> load() {
										return item.getModelObject().getBuilds();
									}
									
								}) {
									
									@Override
									protected Component newListLink(String componentId) {
										String query = "" 
												+ getRuleName(AssociatedWithPullRequest) + " " + quote("#" + getPullRequest().getNumber()) 
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

						if (jobBuilds.isRequired())
							link.add(new Label("name", jobName + " (required)"));
						else
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
				
				fragment.add(new WebSocketObserver() {

					@Override
					public Collection<String> getObservables() {
						return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
					}

					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler) {
						handler.add(component);
					}
					
				});

				fragment.setOutputMarkupId(true);
				return fragment;
			}
			
		});
	}
	
	private WebMarkupContainer newManageContainer() {
		WebMarkupContainer container = new WebMarkupContainer("manage");
		container.setVisible(SecurityUtils.canManage(getPullRequest().getTargetProject()));
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
						Cursor.getQuery(cursor), 
						Cursor.getPage(cursor) + 1);
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
				OneDev.getInstance(PullRequestChangeManager.class).changeMergeStrategy(getPullRequest(), mergeStrategy);
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

				add(new WebSocketObserver() {
					
					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler) {
						handler.add(component);
					}
					
					@Override
					public Collection<String> getObservables() {
						return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
					}
					
				});
				
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
				setOutputMarkupId(true);
			}
			
		});
		
		User submitter = User.from(request.getSubmitter(), request.getSubmitterName());
		statusAndBranchesContainer.add(new UserIdentPanel("user", submitter, Mode.NAME));
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
		
		statusAndBranchesContainer.add(new SideInfoLink("moreInfo"));
		
		return statusAndBranchesContainer;
	}

	private WebMarkupContainer newMergeStatusContainer() {
		WebMarkupContainer mergeStatusContainer = new WebMarkupContainer("mergeStatus") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				setVisible(request.isOpen());
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
						return new ConflictResolveInstructionPanel(id) {

							@Override
							protected PullRequest getPullRequest() {
								return PullRequestDetailPage.this.getPullRequest();
							}
							
						};
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
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().getMergePreview();
				setVisible(preview != null && preview.getMerged() != null);
			}

		});
		
		return mergeStatusContainer;
	}
	
	private AttributeAppender newOperationAppender(PullRequestOperation operation) {
		return AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return operation == activeOperation?"active":""; 
			}
			
		});
	}
	
	private WebMarkupContainer newOperationsContainer() {
		WebMarkupContainer operationsContainer = new WebMarkupContainer("operations") {

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
				activeOperation = APPROVE;
				reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				operationsContainer.replace(newOperationConfirm(confirmId, APPROVE, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(APPROVE.canOperate(getPullRequest()));
			}
			
		}.add(newOperationAppender(APPROVE)));
		
		operationsContainer.add(new AjaxLink<Void>("requestForChanges") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				activeOperation = REQUEST_FOR_CHANGES;
				reviewUpdateId = getPullRequest().getLatestUpdate().getId();
				operationsContainer.replace(newOperationConfirm(confirmId, REQUEST_FOR_CHANGES, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(REQUEST_FOR_CHANGES.canOperate(getPullRequest()));
			}
			
		}.add(newOperationAppender(REQUEST_FOR_CHANGES)));
		
		operationsContainer.add(new AjaxLink<Void>("discard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				activeOperation = DISCARD;
				operationsContainer.replace(newOperationConfirm(confirmId, DISCARD, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DISCARD.canOperate(getPullRequest()));
			}

		}.add(newOperationAppender(DISCARD)));
		
		operationsContainer.add(new AjaxLink<Void>("reopen") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				activeOperation = REOPEN;
				operationsContainer.replace(newOperationConfirm(confirmId, REOPEN, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(REOPEN.canOperate(getPullRequest()));
			}

		}.add(newOperationAppender(REOPEN)));
		
		operationsContainer.add(new AjaxLink<Void>("deleteSourceBranch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				activeOperation = DELETE_SOURCE_BRANCH;
				operationsContainer.replace(newOperationConfirm(confirmId, DELETE_SOURCE_BRANCH, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DELETE_SOURCE_BRANCH.canOperate(getPullRequest()));
			}

		}.add(newOperationAppender(DELETE_SOURCE_BRANCH)));
		
		operationsContainer.add(new AjaxLink<Void>("restoreSourceBranch") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				activeOperation = RESTORE_SOURCE_BRANCH;
				operationsContainer.replace(newOperationConfirm(confirmId, RESTORE_SOURCE_BRANCH, operationsContainer));
				target.add(operationsContainer);
				target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(RESTORE_SOURCE_BRANCH.canOperate(getPullRequest()));
			}

		}.add(newOperationAppender(RESTORE_SOURCE_BRANCH)));
		
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
			protected List<User> getMentionables() {
				return OneDev.getInstance(UserManager.class).queryAndSort(getPullRequest().getParticipants());
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
						setResponsePage(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(getPullRequest(), cursor));
					} catch (OneException e) {
						error(e.getMessage());
						target.add(feedback);
						target.add(hint);
						target.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
					}
				}
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				activeOperation = null;
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

	public static PageParameters paramsOf(PullRequest request, @Nullable Cursor cursor) {
		return paramsOf(request.getFQN(), cursor);
	}
	
	public static PageParameters paramsOf(ProjectScopedNumber requestFQN, @Nullable Cursor cursor) {
		PageParameters params = ProjectPage.paramsOf(requestFQN.getProject());
		params.add(PARAM_REQUEST, requestFQN.getNumber());
		if (cursor != null)
			cursor.fill(params);
		return params;
	}
	
	@Override
	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PullRequestDetailResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.pullRequestDetail.onDomReady();"));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}
	
	private class RequestTab extends PageTab {

		public RequestTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			if (getMainPageClass() == PullRequestCodeCommentsPage.class) {
				Fragment fragment = new Fragment(componentId, "codeCommentsTabLinkFrag", PullRequestDetailPage.this);
				Link<Void> link = new ViewStateAwarePageLink<Void>("link", 
						PullRequestCodeCommentsPage.class, paramsOf(getPullRequest(), cursor));
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
				link.add(new WebSocketObserver() {

					@Override
					public Collection<String> getObservables() {
						return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
					}

					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler) {
						handler.add(component);
					}
					
				});
				link.setOutputMarkupId(true);
				fragment.add(link);
				return fragment;
			} else {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getPullRequest(), cursor));
					}
					
				};
			}
		}
		
	}
	
	private static class JobBuilds {
		private final String jobName;

		private final boolean required;
		
		private final List<Build> builds;
		
		public JobBuilds(String jobName, boolean required, List<Build> builds) {
			this.jobName = jobName;
			this.required = required;
			this.builds = builds;
		}

		public String getJobName() {
			return jobName;
		}

		public boolean isRequired() {
			return required;
		}

		public List<Build> getBuilds() {
			return builds;
		}
		
	}
}