package io.onedev.server.web.component.pullrequest.list;

import static io.onedev.server.entityreference.ReferenceUtils.transformReferences;
import static io.onedev.server.search.entity.pullrequest.PullRequestQuery.merge;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.entityreference.LinkTransformer;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.pullrequest.FuzzyCriteria;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.watch.WatchStatus;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.PullRequestQueryBehavior;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.pullrequest.RequestStatusBadge;
import io.onedev.server.web.component.pullrequest.build.PullRequestJobsPanel;
import io.onedev.server.web.component.pullrequest.review.ReviewerAvatar;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.sortedit.SortEditPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.watchstatus.WatchStatusPanel;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.xodus.VisitInfoService;

public abstract class PullRequestListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<PullRequestQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected PullRequestQuery load() {
			return parse(queryStringModel.getObject(), getBaseQuery());
		}

	};
	
	private Component countLabel;
	
	private DataTable<PullRequest, Void> requestsTable;
	
	private SelectionColumn<PullRequest, Void> selectionColumn;
	
	private SortableDataProvider<PullRequest, Void> dataProvider;	
	
	private TextField<String> queryInput;
	
	private Component saveQueryLink;
	
	private WebMarkupContainer body;
	
	private boolean querySubmitted = true;
	
	public PullRequestListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}

	private PullRequestService getPullRequestService() {
		return OneDev.getInstance(PullRequestService.class);		
	}

	private TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}

	protected PullRequestQuery getBaseQuery() {
		return new PullRequestQuery();
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	@Nullable
	private PullRequestQuery parse(@Nullable String queryString, PullRequestQuery baseQuery) {
		PullRequestQuery parsedQuery;
		try {
			parsedQuery = PullRequestQuery.parse(getProject(), queryString, true);
		} catch (Exception e) {
			getFeedbackMessages().clear();
			if (e instanceof ExplicitException) {
				error(e.getMessage());
				return null;
			} else {
				info(_T("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and open"));
				parsedQuery = new PullRequestQuery(new FuzzyCriteria(queryString));
			}
		}
		return merge(baseQuery, parsedQuery);
	}
	
	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();

	private void doQuery(AjaxRequestTarget target) {
		requestsTable.setCurrentPage(0);
		target.add(countLabel);
		target.add(body);
		if (selectionColumn != null)
			selectionColumn.getSelections().clear();
		querySubmitted = true;
		if (SecurityUtils.getAuthUser() != null && getQuerySaveSupport() != null)
			target.add(saveQueryLink);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) {
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));

		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(querySubmitted && queryModel.getObject() != null);
				setVisible(SecurityUtils.getAuthUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.append("class", "disabled", " ");
				if (!querySubmitted)
					tag.put("data-tippy-content", _T("Query not submitted"));
				else if (queryModel.getObject() == null)
					tag.put("data-tippy-content", _T("Can not save malformed query"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject().toString());
			}		
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(new MenuLink("operations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();

				if (!SecurityUtils.getAuthUser().isServiceAccount()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Watch/Unwatch Selected Pull Requests");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new WatchStatusPanel(id) {

										@Override
										protected WatchStatus getWatchStatus() {
											return null;
										}

										@Override
										protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
											dropdown.close();
											dropdown2.close();

											var requests = selectionColumn.getSelections().stream()
													.map(it->it.getObject()).collect(toList());
											getWatchService().setWatchStatus(SecurityUtils.getAuthUser(), requests, watchStatus);
											selectionColumn.getSelections().clear();
											Session.get().success(_T("Watch status changed"));
										}
									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select pull requests to watch/unwatch"));
									}
								}

							};
						}

					});
				}

				if (getProject() != null && SecurityUtils.canManagePullRequests(getProject())) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Discard Selected Pull Requests");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();

									String errorMessage = null;
									for (IModel<PullRequest> each : selectionColumn.getSelections()) {
										PullRequest request = each.getObject();
										if (!request.isOpen()) {
											errorMessage = MessageFormat.format(_T("Pull request #{0} already closed"), request.getNumber());
											break;
										}
									}

									if (errorMessage != null) {
										getSession().error(errorMessage);
									} else {
										new ConfirmModalPanel(target) {

											@Override
											protected void onConfirm(AjaxRequestTarget target) {
												var user = SecurityUtils.getUser();
												for (IModel<PullRequest> each : selectionColumn.getSelections())
													OneDev.getInstance(PullRequestService.class).discard(user, each.getObject(), null);
												target.add(countLabel);
												target.add(body);
												selectionColumn.getSelections().clear();
											}

											@Override
											protected String getConfirmMessage() {
												return _T("Type <code>yes</code> below to discard selected pull requests");
											}

											@Override
											protected String getConfirmInput() {
												return "yes";
											}

										};
									}

								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select pull requests to discard"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Delete Selected Pull Requests");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();

									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(()-> {
												Collection<PullRequest> requests = new ArrayList<>();
												for (IModel<PullRequest> each : selectionColumn.getSelections())
													requests.add(each.getObject());
												getPullRequestService().delete(requests, getProject());
												for (var request: requests) {
													var oldAuditContent = VersionedXmlDoc.fromBean(request).toXML();
													getAuditService().audit(request.getProject(), "deleted pull request \"" + request.getReference().toString(request.getProject()) + "\"", oldAuditContent, null);
												}													
											});
											target.add(countLabel);
											target.add(body);
											selectionColumn.getSelections().clear();
										}

										@Override
										protected String getConfirmMessage() {
											return _T("Type <code>yes</code> below to delete selected pull requests");
										}

										@Override
										protected String getConfirmInput() {
											return "yes";
										}

									};

								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select pull requests to delete"));
									}
								}

							};
						}

					});
				}

				if (!SecurityUtils.getAuthUser().isServiceAccount()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Watch/Unwatch All Queried Pull Requests");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new WatchStatusPanel(id) {

										@Override
										protected WatchStatus getWatchStatus() {
											return null;
										}

										@Override
										protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
											dropdown.close();
											dropdown2.close();

											Collection<PullRequest> requests = new ArrayList<>();
											for (@SuppressWarnings("unchecked") var it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext(); )
												requests.add(it.next());
											getWatchService().setWatchStatus(SecurityUtils.getAuthUser(), requests, watchStatus);
											Session.get().success(_T("Watch status changed"));
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(requestsTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No pull requests to watch/unwatch"));
									}
								}
							};
						}

					});
				}
				
				if (getProject() != null && SecurityUtils.canManagePullRequests(getProject())) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Discard All Queried Pull Requests");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@SuppressWarnings("unchecked")
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();

									String errorMessage = null;
									for (Iterator<PullRequest> it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext(); ) {
										PullRequest request = it.next();
										if (!request.isOpen()) {
											errorMessage = MessageFormat.format(_T("Pull request #{0} already closed"), request.getNumber());
											break;
										}
									}

									if (errorMessage != null) {
										getSession().error(errorMessage);
									} else {
										new ConfirmModalPanel(target) {

											@Override
											protected void onConfirm(AjaxRequestTarget target) {
												var user = SecurityUtils.getUser();
												for (Iterator<PullRequest> it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext(); )
													OneDev.getInstance(PullRequestService.class).discard(user, it.next(), null);
												dataProvider.detach();
												target.add(countLabel);
												target.add(body);
												selectionColumn.getSelections().clear();
											}

											@Override
											protected String getConfirmMessage() {
												return _T("Type <code>yes</code> below to discard all queried pull requests");
											}

											@Override
											protected String getConfirmInput() {
												return "yes";
											}

										};
									}

								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(requestsTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No pull requests to discard"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Delete All Queried Pull Requests");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@SuppressWarnings("unchecked")
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();

									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(()-> {
												Collection<PullRequest> requests = new ArrayList<>();
												for (Iterator<PullRequest> it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext(); )
													requests.add(it.next());
												getPullRequestService().delete(requests, getProject());
												for (var request: requests) {
													var oldAuditContent = VersionedXmlDoc.fromBean(request).toXML();
													getAuditService().audit(request.getProject(), "deleted pull request \"" + request.getReference().toString(request.getProject()) + "\"", oldAuditContent, null);
												}													
											});
											dataProvider.detach();
											target.add(countLabel);
											target.add(body);
											selectionColumn.getSelections().clear();
										}

										@Override
										protected String getConfirmMessage() {
											return _T("Type <code>yes</code> below to delete all queried pull requests");
										}

										@Override
										protected String getConfirmInput() {
											return "yes";
										}

									};

								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(requestsTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No pull requests to delete"));
									}
								}

							};
						}

					});
				}

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set All Queried Pull Requests as Read");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(requestsTable.getItemCount() != 0);
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No pull requests to set as read"));
								}
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								var visitInfoService = OneDev.getInstance(VisitInfoService.class);
								for (@SuppressWarnings("unchecked") var it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext(); )
									visitInfoService.visitPullRequest(SecurityUtils.getAuthUser(), it.next());
								target.add(body);
							}

						};
					}

				});
				
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAuthUser() != null);
			}
			
		});
		
		add(new DropdownLink("filter") {
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new PullRequestFilterPanel(id, new IModel<EntityQuery<PullRequest>>() {
					@Override
					public void detach() {
					}
					@Override
					public EntityQuery<PullRequest> getObject() {
						var query = parse(queryStringModel.getObject(), new PullRequestQuery());
						return query!=null? query : new PullRequestQuery();
					}
					@Override
					public void setObject(EntityQuery<PullRequest> object) {
						PullRequestListPanel.this.getFeedbackMessages().clear();
						queryStringModel.setObject(object.toString());
						var target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}
				});
			}
		});
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Map<String, Direction> sortFields = new LinkedHashMap<>();
				for (var entry: PullRequest.SORT_FIELDS.entrySet())
					sortFields.put(entry.getKey(), entry.getValue().getDefaultDirection());
				if (getProject() != null)
					sortFields.remove(PullRequest.NAME_TARGET_PROJECT);
				
				return new SortEditPanel<PullRequest>(id, sortFields, new IModel<>() {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						var query = parse(queryStringModel.getObject(), new PullRequestQuery());
						return query!=null? query.getSorts() : new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						var query = parse(queryStringModel.getObject(), new PullRequestQuery());
						PullRequestListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new PullRequestQuery();
						query.setSorts(object);
						queryStringModel.setObject(query.toString());
						var target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}

				});
			}
			
		});

		var extraActionsView = new RepeatingView("extraActions");
		add(extraActionsView);
		for (var renderer: OneDev.getExtensions(PullRequestListActionRenderer.class))
			extraActionsView.add(renderer.render(extraActionsView.newChildId()));
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.add(new PullRequestQueryBehavior(new AbstractReadOnlyModel<>() {

			@Override
			public Project getObject() {
				return getProject();
			}

		}, true, true) {
			
			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				PullRequestListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
		});
		queryInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				doQuery(target);
			}
			
		});
		
		Form<?> queryForm = new Form<Void>("query");
		queryForm.add(queryInput);
		queryForm.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				PullRequestListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
			
		});
		add(queryForm);

		if (getProject() == null) {
			add(new DropdownLink("newPullRequest") {
	
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {
	
						@Override
						protected List<Project> load() {
							ProjectService projectService = OneDev.getInstance(ProjectService.class);
							List<Project> projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(new ReadCode()));
							projects.sort(projectService.cloneCache().comparingPath());
							return projects;
						}
						
					}) {
						@Override
						protected String getTitle() {
							return _T("Select Project");
						}
						
						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							setResponsePage(NewPullRequestPage.class, NewPullRequestPage.paramsOf(project));
						}
	
					}.add(AttributeAppender.append("class", "no-current"));
				}
				
			});
		} else {
			add(new BookmarkablePageLink<Void>("newPullRequest", NewPullRequestPage.class,
					NewPullRequestPage.paramsOf(getProject())));		
		}
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));

		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}
			
		});
		
		if (SecurityUtils.getAuthUser() != null)
			columns.add(selectionColumn = new SelectionColumn<PullRequest, Void>());
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "contentFrag", PullRequestListPanel.this);
				
				Item<?> row = cellItem.findParent(Item.class);
				Cursor cursor = new Cursor(queryModel.getObject().toString(), (int)requestsTable.getItemCount(), 
						(int)requestsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProject());

				String label = "(" + request.getReference().toString(getProject()) + ")";
					
				ActionablePageLink numberLink;
				fragment.add(numberLink = new ActionablePageLink("number", 
						PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(request)) {

					@Override
					public IModel<?> getBody() {
						return Model.of(label);
					}

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						WebSession.get().setPullRequestCursor(cursor);
						
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(PullRequest.class, redirectUrlAfterDelete);
					}
					
				});

				String url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(request)).toString();
				
				var transformed = transformReferences(request.getTitle(), request.getTargetProject(), 
						new LinkTransformer(url));
				String title = Emojis.getInstance().apply(transformed);
				fragment.add(new Label("title", title) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format(""
								+ "$('#%s a:not(.embedded-reference)').click(function(e) {\n"
								+ "  if (!e.ctrlKey && !e.metaKey) {\n"
								+ "    $('#%s').click();\n"
								+ "    return false;\n"
								+ "  }\n"
								+ "});", 
								getMarkupId(), numberLink.getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				}.setEscapeModelStrings(false).setOutputMarkupId(true));
				
				fragment.add(new EntityLabelsPanel<PullRequestLabel>("labels", rowModel));

				RepeatingView reviewsView = new RepeatingView("reviews");
				for (PullRequestReview review: request.getSortedReviews()) {
					Long reviewId = review.getId();
					if (review.getStatus() != Status.EXCLUDED) {
						reviewsView.add(new ReviewerAvatar(reviewsView.newChildId()) {
	
							@Override
							protected PullRequestReview getReview() {
								return OneDev.getInstance(PullRequestReviewService.class).load(reviewId);
							}
							
						});
					}
				}
				fragment.add(reviewsView);
				
				fragment.add(new PullRequestJobsPanel("jobs") {
					
					@Override
					protected PullRequest getPullRequest() {
						return rowModel.getObject();
					}
					
				});

				fragment.add(new Label("comments", request.getCommentCount()));
				
				fragment.add(new RequestStatusBadge("status", rowModel));
				fragment.add(new BranchLink("targetBranch", request.getTarget()));
				if (request.getSourceProject() != null) {
					fragment.add(new BranchLink("sourceBranch", request.getSource()));
				} else {
					fragment.add(new Label("sourceBranch", "unknown") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("em");
						}

					});
				}
				
				LastActivity lastActivity = request.getLastActivity();
				if (lastActivity.getUser() != null) 
					fragment.add(new UserIdentPanel("user", lastActivity.getUser(), Mode.NAME));
				else 
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				fragment.add(new Label("activity", _T(lastActivity.getDescription())));
				fragment.add(new Label("date", DateUtils.formatAge(lastActivity.getDate()))
					.add(new AttributeAppender("data-tippy-content", DateUtils.formatDateTime(lastActivity.getDate()))));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "summary";
			}
			
		});

		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return MessageFormat.format(_T("found {0} pull requests"), dataProvider.size());
				else
					return _T("found 1 pull request");
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(dataProvider.size() != 0);
			}
		}.setOutputMarkupPlaceholderTag(true));
		
		dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				try {
					var query = queryModel.getObject();
					if (query != null) 
						return getPullRequestService().query(SecurityUtils.getSubject(), getProject(), query, true, (int) first, (int) count).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return new ArrayList<PullRequest>().iterator();
			}

			@Override
			public long calcSize() {
				try {
					var query = queryModel.getObject();
					if (query != null)
						return getPullRequestService().count(SecurityUtils.getSubject(), getProject(), query.getCriteria());
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				Long requestId = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return getPullRequestService().load(requestId);
					}

				};
			}

		};
		
		body.add(requestsTable = new DataTable<>("requests", columns, dataProvider, WebConstants.PAGE_SIZE) {

			@Override
			protected Item<PullRequest> newRowItem(String id, int index, IModel<PullRequest> model) {
				Item<PullRequest> item = super.newRowItem(id, index, model);
				PullRequest request = model.getObject();
				item.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
					@Override
					protected String load() {
						var request = item.getModelObject();
						return request.isVisitedAfter(request.getLastActivity().getDate()) ? "request" : "request new";
					}
				}));
				var requestId = request.getId();
				item.add(new ChangeObserver() {
					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(PullRequest.getChangeObservable(requestId));
					}

				});
				return item;
			}

		});
		
		if (getPagingHistorySupport() != null)
			requestsTable.setCurrentPage(getPagingHistorySupport().getCurrentPage());
		
		requestsTable.addBottomToolbar(new NavigationToolbar(requestsTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new OnePagingNavigator(navigatorId, table, getPagingHistorySupport());
			}
			
		});
		requestsTable.addBottomToolbar(new NoRecordsToolbar(requestsTable));
		requestsTable.add(new NoRecordsBehavior());
		
		setOutputMarkupId(true);
	}
	
	private PullRequestWatchService getWatchService() {
		return OneDev.getInstance(PullRequestWatchService.class);
	}

	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new PullRequestListCssResourceReference()));
	}

}
