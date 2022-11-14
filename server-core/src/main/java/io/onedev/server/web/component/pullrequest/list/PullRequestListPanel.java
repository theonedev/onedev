package io.onedev.server.web.component.pullrequest.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
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

import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entitytext.PullRequestTextManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.PullRequestQueryBehavior;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.pullrequest.RequestStatusBadge;
import io.onedev.server.web.component.pullrequest.build.PullRequestJobsPanel;
import io.onedev.server.web.component.pullrequest.review.ReviewerAvatar;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class PullRequestListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<Object> queryModel = new LoadableDetachableModel<Object>() {

		@Override
		protected Object load() {
			return parse(queryStringModel.getObject(), getBaseQuery());
		}
		
	};
	
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

	private PullRequestManager getPullRequestManager() {
		return OneDev.getInstance(PullRequestManager.class);		
	}
	
	private PullRequestTextManager getPullRequestTextManager() {
		return OneDev.getInstance(PullRequestTextManager.class);		
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
	private Object parse(@Nullable String queryString, PullRequestQuery baseQuery) {
		try {
			return PullRequestQuery.merge(baseQuery, PullRequestQuery.parse(getProject(), queryString, true));
		} catch (ExplicitException e) {
			getFeedbackMessages().clear();
			error(e.getMessage());
			return null;
		} catch (Exception e) {
			if (getBaseQuery().toString() != null) {
				getFeedbackMessages().clear();
				error("Malformed query");
				return null;
			} else {
				getFeedbackMessages().clear();
				info("Performing fuzzy query");
				return queryString;
			}
		}
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
		target.add(body);
		if (selectionColumn != null)
			selectionColumn.getSelections().clear();
		querySubmitted = true;
		if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
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
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.append("class", "disabled", " ");
				if (!querySubmitted)
					tag.put("title", "Query not submitted");
				else if (queryModel.getObject() == null)
					tag.put("title", "Can not save malformed query");
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
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Discard Selected Pull Requests";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								String errorMessage = null;
								for (IModel<PullRequest> each: selectionColumn.getSelections()) {
									PullRequest request = each.getObject();
									if (!request.isOpen()) {
										errorMessage = "Pull request #" + request.getNumber() + " already closed";
										break;
									}
								}
								
								if (errorMessage != null) {
									getSession().error(errorMessage);
								} else {
									new ConfirmModalPanel(target) {
										
										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											for (IModel<PullRequest> each: selectionColumn.getSelections())
												OneDev.getInstance(PullRequestManager.class).discard(each.getObject(), null);
											target.add(body);
											selectionColumn.getSelections().clear();
										}
										
										@Override
										protected String getConfirmMessage() {
											return "Type <code>yes</code> below to discard selected pull requests";
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
									tag.put("title", "Please select pull requests to discard");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete Selected Pull Requests";
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
										Collection<PullRequest> requests = new ArrayList<>();
										for (IModel<PullRequest> each: selectionColumn.getSelections())
											requests.add(each.getObject());
										OneDev.getInstance(PullRequestManager.class).delete(requests);
										target.add(body);
										selectionColumn.getSelections().clear();
									}
									
									@Override
									protected String getConfirmMessage() {
										return "Type <code>yes</code> below to delete selected builds";
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
									tag.put("title", "Please select pull requests to delete");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Discard All Queried Pull Requests";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@SuppressWarnings("unchecked")
							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();

								String errorMessage = null;
								for (Iterator<PullRequest> it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext();) {
									PullRequest request = it.next();
									if (!request.isOpen()) {
										errorMessage = "Pull request #" + request.getNumber() + " already closed";
										break;
									}
								}
								
								if (errorMessage != null) {
									getSession().error(errorMessage);
								} else {
									new ConfirmModalPanel(target) {
										
										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											for (Iterator<PullRequest> it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext();)
												OneDev.getInstance(PullRequestManager.class).discard(it.next(), null);
											target.add(body);
											selectionColumn.getSelections().clear();
										}
										
										@Override
										protected String getConfirmMessage() {
											return "Type <code>yes</code> below to discard all queried pull requests";
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
									tag.put("title", "No pull requests to discard");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete All Queried Pull Requests";
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
										Collection<PullRequest> requests = new ArrayList<>();
										for (Iterator<PullRequest> it = (Iterator<PullRequest>) dataProvider.iterator(0, requestsTable.getItemCount()); it.hasNext();)
											requests.add(it.next());
										OneDev.getInstance(PullRequestManager.class).delete(requests);
										target.add(body);
										selectionColumn.getSelections().clear();
									}
									
									@Override
									protected String getConfirmMessage() {
										return "Type <code>yes</code> below to delete all queried pull requests";
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
									tag.put("title", "No pull requests to delete");
								}
							}
							
						};
					}
					
				});
				
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null && SecurityUtils.canManagePullRequests(getProject()));
			}
			
		});
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				List<String> orderFields = new ArrayList<>(PullRequest.ORDER_FIELDS.keySet());
				if (getProject() != null)
					orderFields.remove(PullRequest.NAME_TARGET_PROJECT);
				
				return new OrderEditPanel<PullRequest>(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						Object query = parse(queryStringModel.getObject(), new PullRequestQuery());
						PullRequestListPanel.this.getFeedbackMessages().clear();
						if (query instanceof PullRequestQuery) 
							return ((PullRequestQuery)query).getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						Object query = parse(queryStringModel.getObject(), new PullRequestQuery());
						PullRequestListPanel.this.getFeedbackMessages().clear();
						if (!(query instanceof PullRequestQuery))
							query = new PullRequestQuery();
						((PullRequestQuery)query).getSorts().clear();
						((PullRequestQuery)query).getSorts().addAll(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});	
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.add(new PullRequestQueryBehavior(new AbstractReadOnlyModel<Project>() {

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
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				List<String> hints = super.getHints(terminalExpect);
				hints.add("Free input for fuzzy query on number/title/description");
				return hints;
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
							ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
							List<Project> projects = new ArrayList<Project>(projectManager.getPermittedProjects(new ReadCode()));
							projects.sort(projectManager.cloneCache().comparingPath());
							return projects;
						}
						
					}) {
	
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
		
		if (getProject() != null && SecurityUtils.canManagePullRequests(getProject())) 
			columns.add(selectionColumn = new SelectionColumn<PullRequest, Void>());
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "contentFrag", PullRequestListPanel.this);
				
				Item<?> row = cellItem.findParent(Item.class);
				Cursor cursor = new Cursor(queryModel.getObject().toString(), (int)requestsTable.getItemCount(), 
						(int)requestsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProject());

				String label;
				Project currentProject = null;
				if (getPage() instanceof ProjectPage)
					currentProject = ((ProjectPage) getPage()).getProject();
				if (currentProject == null || !currentProject.equals(request.getProject()))
					label = request.getProject().getPath() + "#" + request.getNumber();
				else
					label = "#" + request.getNumber();
					
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
				
				ReferenceTransformer transformer = new ReferenceTransformer(request.getTargetProject(), url);
				
				String title = Emojis.getInstance().apply(transformer.apply(request.getTitle()));
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
								return OneDev.getInstance(PullRequestReviewManager.class).load(reviewId);
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
				
				LastUpdate lastUpdate = request.getLastUpdate();
				if (lastUpdate.getUser() != null) 
					fragment.add(new UserIdentPanel("user", lastUpdate.getUser(), Mode.NAME));
				else 
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				fragment.add(new Label("activity", lastUpdate.getActivity()));
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate()))
					.add(new AttributeAppender("title", DateUtils.formatDateTime(lastUpdate.getDate()))));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "summary";
			}
			
		});
		
		dataProvider = new LoadableDetachableDataProvider<PullRequest, Void>() {

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				try {
					Object query = queryModel.getObject();
					if (query instanceof PullRequestQuery) {
						return getPullRequestManager().query(getProject(), (PullRequestQuery)query, 
								true, (int)first, (int)count).iterator();
					} else if (query instanceof String) {
						return getPullRequestTextManager().query(getProject(), (String)query, 
								true, (int)first, (int)count).iterator();
					}
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return new ArrayList<PullRequest>().iterator();
			}

			@Override
			public long calcSize() {
				try {
					Object query = queryModel.getObject();
					if (query instanceof PullRequestQuery) 
						return getPullRequestManager().count(getProject(), ((PullRequestQuery)query).getCriteria());
					else if (query instanceof String)
						return getPullRequestTextManager().count(getProject(), (String)query);
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
						return getPullRequestManager().load(requestId);
					}
					
				};
			}
			
		};
		
		body.add(requestsTable = new DataTable<PullRequest, Void>("requests", columns, dataProvider, WebConstants.PAGE_SIZE) {

			@Override
			protected Item<PullRequest> newRowItem(String id, int index, IModel<PullRequest> model) {
				Item<PullRequest> item = super.newRowItem(id, index, model);
				PullRequest request = model.getObject();
				item.add(AttributeAppender.append("class", 
						request.isVisitedAfter(request.getLastUpdate().getDate())?"request":"request new"));
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

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new PullRequestListCssResourceReference()));
	}

}
