package io.onedev.server.web.component.workspace.list;

import static io.onedev.server.model.Workspace.SORT_FIELDS;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.workspace.FuzzyCriteria;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.WorkspaceQueryBehavior;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.branch.picker.BranchSelector;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.Alignment;
import io.onedev.server.web.component.floating.ComponentTarget;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.sortedit.SortEditPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.workspace.invalidspec.InvalidWorkspaceSpecIcon;
import io.onedev.server.web.component.workspace.specselector.WorkspaceSpecSelector;
import io.onedev.server.web.component.workspace.status.WorkspaceStatusIcon;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.workspace.WorkspaceService;

public abstract class WorkspaceListPanel extends Panel {

	private final IModel<String> queryStringModel;

	private final IModel<WorkspaceQuery> queryModel = new LoadableDetachableModel<>() {
		@Override
		protected WorkspaceQuery load() {
			return parse(queryStringModel.getObject());
		}
	};

	private Component countLabel;

	private DataTable<Workspace, Void> workspacesTable;

	private SelectionColumn<Workspace, Void> selectionColumn;

	private LoadableDetachableDataProvider<Workspace, Void> dataProvider;

	private TextField<String> queryInput;

	private Component createWorkspaceLink;

	private WebMarkupContainer body;

	private Component saveQueryLink;

	private boolean querySubmitted = true;

	public WorkspaceListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}

	private WorkspaceService getWorkspaceService() {
		return OneDev.getInstance(WorkspaceService.class);
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	@Nullable
	private WorkspaceQuery parse(@Nullable String queryString) {
		WorkspaceQuery parsedQuery;
		try {
			parsedQuery = WorkspaceQuery.parse(getProject(), queryString, true);
		} catch (Exception e) {
			getFeedbackMessages().clear();
			if (e instanceof ExplicitException) {
				error(e.getMessage());
				return null;
			} else {
				info(_T("Performing fuzzy query. Enclose search text with '~' to add more conditions, for instance: ~branch name~ and \"Status\" is \"STARTED\""));
				parsedQuery = new WorkspaceQuery(new FuzzyCriteria(queryString));
			}
		}
		return parsedQuery;
	}

	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}

	@Nullable
	protected abstract Project getProject();

	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}

	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}

	private void doQuery(AjaxRequestTarget target) {
		workspacesTable.setCurrentPage(0);
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

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Delete Selected Workspaces");
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
										Collection<Workspace> workspaces = new ArrayList<>();
										for (IModel<Workspace> each : selectionColumn.getSelections())
											workspaces.add(each.getObject());
										getWorkspaceService().delete(workspaces);
										target.add(countLabel);
										target.add(body);
										selectionColumn.getSelections().clear();
									}

									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to delete selected workspaces");
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
									tag.put("data-tippy-content", _T("Please select workspaces to delete"));
								}
							}

						};
					}

				});

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Delete All Queried Workspaces");
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
										Collection<Workspace> workspaces = new ArrayList<>();
										for (Iterator<Workspace> it = (Iterator<Workspace>) dataProvider.iterator(0, workspacesTable.getItemCount()); it.hasNext(); )
											workspaces.add(it.next());
										getWorkspaceService().delete(workspaces);
										dataProvider.detach();
										target.add(countLabel);
										target.add(body);
										selectionColumn.getSelections().clear();
									}

									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to delete all queried workspaces");
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
								setEnabled(workspacesTable.getItemCount() != 0);
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No workspaces to delete"));
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
				setVisible(getProject() != null && SecurityUtils.canManageWorkspaces(getProject()));
			}

		});

		add(new DropdownLink("filter") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new WorkspaceFilterPanel(id, new IModel<>() {

					@Override
					public void detach() {
					}

					@Override
					public WorkspaceQuery getObject() {
						var query = parse(queryStringModel.getObject());
						return query != null ? query : new WorkspaceQuery();
					}

					@Override
					public void setObject(EntityQuery<Workspace> object) {
						WorkspaceListPanel.this.getFeedbackMessages().clear();
						queryStringModel.setObject(object.toString());
						var target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}

				}) {

					@Override
					protected Project getProject() {
						return WorkspaceListPanel.this.getProject();
					}

				};
			}

		});

		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Map<String, Direction> sortFields = new LinkedHashMap<>();
				for (var entry : SORT_FIELDS.entrySet())
					sortFields.put(entry.getKey(), entry.getValue().getDefaultDirection());
				if (getProject() != null)
					sortFields.remove(Workspace.NAME_PROJECT);

				return new SortEditPanel<Workspace>(id, sortFields, new IModel<>() {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						var query = parse(queryStringModel.getObject());
						return query != null ? query.getSorts() : new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						WorkspaceQuery query = parse(queryStringModel.getObject());
						WorkspaceListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new WorkspaceQuery();
						query.setSorts(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}

				});
			}

		});

		queryInput = new TextField<>("input", queryStringModel);
		queryInput.add(new WorkspaceQueryBehavior(
				new AbstractReadOnlyModel<>() {
					@Override
					public Project getObject() {
						return getProject();
					}
				}, true) {

			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				WorkspaceListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}

			@Override
			protected boolean isSelectOnFocus() {
				return true;
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
				WorkspaceListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
		});
		add(queryForm);

		if (getProject() == null) {
			add(createWorkspaceLink = new DropdownLink("createWorkspace") {

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new ProjectSelector(id, new LoadableDetachableModel<>() {

						@Override
						protected List<Project> load() {
							List<Project> projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(new WriteCode()));
							projects.removeIf(p -> p.getHierarchyWorkspaceSpecs().isEmpty());
							projects.sort(getProjectService().cloneCache().comparingPath());
							return projects;
						}

					}) {
						@Override
						protected String getTitle() {
							return _T("Select Project");
						}

						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							dropdown.close();
							newBranchSelector(target, project);
						}

					}.add(AttributeAppender.append("class", "no-current"));
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.getAuthUser() != null);
				}

			});
		} else {
			add(createWorkspaceLink = new AjaxLink<Void>("createWorkspace") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					newBranchSelector(target, getProject());
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.getAuthUser() != null);
				}

			});
		}

		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return MessageFormat.format(_T("found {0} workspaces"), dataProvider.size());
				else
					return _T("found 1 workspace");
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
			public Iterator<? extends Workspace> iterator(long first, long count) {
				try {
					var query = queryModel.getObject();
					if (query != null) {
						return getWorkspaceService().query(SecurityUtils.getSubject(), getProject(),
								query, (int) first, (int) count).iterator();
					}
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return new ArrayList<Workspace>().iterator();
			}

			@Override
			public long calcSize() {
				try {
					WorkspaceQuery query = queryModel.getObject();
					if (query != null)
						return getWorkspaceService().count(SecurityUtils.getSubject(), getProject(),
								query.getCriteria());
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<Workspace> model(Workspace object) {
				Long workspaceId = object.getId();
				return new LoadableDetachableModel<>() {
					@Override
					protected Workspace load() {
						return getWorkspaceService().load(workspaceId);
					}
				};
			}

		};

		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));

		body.add(new FencedFeedbackPanel("feedback", this));

		List<IColumn<Workspace, Void>> columns = new ArrayList<>();

		if (getProject() != null && SecurityUtils.canWriteCode(getProject()))
			columns.add(selectionColumn = new SelectionColumn<>());

		columns.add(new AbstractColumn<>(Model.of(_T("Workspace"))) {

			@Override
			public String getCssClass() {
				return "workspace";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId,
                                     IModel<Workspace> rowModel) {
				Fragment fragment = new Fragment(componentId, "workspaceFrag", WorkspaceListPanel.this);
				Workspace workspace = rowModel.getObject();
				var link = new ActionablePageLink("link", WorkspaceDashboardPage.class,
						WorkspaceDashboardPage.paramsOf(workspace)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
						Cursor cursor = new Cursor(queryModel.getObject().toString(), (int) workspacesTable.getItemCount(),
								(int) workspacesTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProject());
						WebSession.get().setWorkspaceCursor(cursor);
					}
				};

				link.add(new WorkspaceStatusIcon("icon", new AbstractReadOnlyModel<>() {
					@Override
					public Workspace.Status getObject() {
						return rowModel.getObject().getStatus();
					}
				}));
				link.add(new Label("label", workspace.getReference().toString(getProject())));

				fragment.add(link);
				cellItem.add(fragment);
			}
		});

		columns.add(new AbstractColumn<>(Model.of(_T("User"))) {

			@Override
			public String getCssClass() {
				return "user d-none d-md-table-cell";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId,
                                     IModel<Workspace> rowModel) {
				Fragment fragment = new Fragment(componentId, "userFrag", WorkspaceListPanel.this);
				fragment.add(new UserIdentPanel("link", rowModel.getObject().getUser(), Mode.AVATAR_AND_NAME));
				cellItem.add(fragment);
			}
		});

		columns.add(new AbstractColumn<>(Model.of(_T("Branch"))) {

			@Override
			public String getCssClass() {
				return "branch";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId,
                                     IModel<Workspace> rowModel) {
				Fragment fragment = new Fragment(componentId, "branchFrag", WorkspaceListPanel.this);
				Workspace workspace = rowModel.getObject();
				fragment.add(new BranchLink("branch",
						new ProjectAndBranch(workspace.getProject(), workspace.getBranch()), true));
				cellItem.add(fragment);
			}
		});

		columns.add(new AbstractColumn<>(Model.of(_T("Spec"))) {

			@Override
			public String getCssClass() {
				return "spec d-none d-md-table-cell";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId,
                                     IModel<Workspace> rowModel) {
				Fragment fragment = new Fragment(componentId, "specFrag", WorkspaceListPanel.this);
				Workspace workspace = rowModel.getObject();
				fragment.add(new Label("label", workspace.getSpecName()));
				fragment.add(new InvalidWorkspaceSpecIcon("invalidSpec", rowModel));

				cellItem.add(fragment);
			}

		});

		columns.add(new AbstractColumn<>(Model.of(_T("Created"))) {

			@Override
			public String getCssClass() {
				return "date d-none d-xl-table-cell";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId,
                                     IModel<Workspace> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatAge(rowModel.getObject().getCreateDate())));
			}
		});

		body.add(workspacesTable = new DefaultDataTable<>("workspaces", columns, dataProvider,
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));

		setOutputMarkupId(true);
	}

	private void newBranchSelector(AjaxRequestTarget target, Project project) {
		var projectId = project.getId();

		var placement = new AlignPlacement(100, 100, 100, 0, 0);
		Alignment alignment = new Alignment(new ComponentTarget(createWorkspaceLink), placement);
		new FloatingPanel(target, alignment, true, true, null) {

			private Project getSelectedProject() {
				return getProjectService().load(projectId);
			}

			@Override
			protected Component newContent(String id) {
				return new BranchSelector(id, new LoadableDetachableModel<>() {
					@Override
					protected Project load() {
						return getSelectedProject();
					}
				}, null) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String branch) {
						close();

						new FloatingPanel(target, alignment, true, true, null) {

							@Override
							protected Component newContent(String id) {
								return new WorkspaceSpecSelector(id, branch) {
									@Override
									protected void onSelect(AjaxRequestTarget target, WorkspaceSpec spec) {
										close();
									}

									@Override
									protected Project getProject() {
										return getSelectedProject();
									}
								};
							}

						};
					}
				};
			}

		};
	}

}
