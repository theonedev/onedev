package io.onedev.server.web.page.admin.buildsetting.agent;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.AuditService;
import io.onedev.server.model.Agent;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.search.entity.agent.FuzzyCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.AgentQueryBehavior;
import io.onedev.server.web.component.AgentStatusBadge;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.sortedit.SortEditPanel;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;

class AgentListPanel extends Panel {
	
	private final IModel<String> queryStringModel;
	
	private final IModel<AgentQuery> queryModel = new LoadableDetachableModel<AgentQuery>() {

		@Override
		protected AgentQuery load() {
			String queryString = queryStringModel.getObject();
			try {
				return AgentQuery.parse(queryString, false);
			} catch (Exception e) {
				if (e instanceof ExplicitException) {
					error(e.getMessage());
					return null;
				} else {
					info(_T("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and online"));
					return new AgentQuery(new FuzzyCriteria(queryString));
				}
			}
		}
		
	};

	private Component countLabel;
	
	private DataTable<Agent, Void> agentsTable;
	
	private SelectionColumn<Agent, Void> selectionColumn;
	
	private SortableDataProvider<Agent, Void> dataProvider;	
	
	private WebMarkupContainer body;
	
	private Component saveQueryLink;	
	
	private TextField<String> queryInput;
	
	private boolean querySubmitted = true;
	
	public AgentListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}
	
	private AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}
	
	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}

	private void doQuery(AjaxRequestTarget target) {
		agentsTable.setCurrentPage(0);
		target.add(countLabel);
		target.add(body);
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
				if (event.getPayload() instanceof SavedQueriesClosed) 
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
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
				setVisible(getQuerySaveSupport() != null);
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
		
		add(new DropdownLink("filter") {
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new AgentFilterPanel(id, new IModel<EntityQuery<Agent>>() {
					@Override
					public void detach() {
					}
					@Override
					public EntityQuery<Agent> getObject() {
						return queryModel.getObject()!=null? queryModel.getObject() : new AgentQuery();
					}
					@Override
					public void setObject(EntityQuery<Agent> object) {
						AgentListPanel.this.getFeedbackMessages().clear();
						queryModel.setObject((AgentQuery) object);
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
				for (var entry: Agent.SORT_FIELDS.entrySet())
					sortFields.put(entry.getKey(), entry.getValue().getDefaultDirection());
				return new SortEditPanel<Agent>(id, sortFields, new IModel<>() {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						var query = queryModel.getObject();
						return query!=null? query.getSorts() : new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						var query = queryModel.getObject();
						AgentListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new AgentQuery();
						query.setSorts(object);
						queryModel.setObject(query);
						queryStringModel.setObject(query.toString());
						var target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}

				});
			}
			
		});
		
		add(new DropdownLink("tokens") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new TokenListPanel(id);
			}
			
		});
		
		add(new MenuLink("operations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Pause Selected Agents");
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								getTransactionService().run(() -> {
									dropdown.close();								
									for (var model: selectionColumn.getSelections()) {
										var agent = model.getObject();
										getAgentService().pause(agent);	
										getAuditService().audit(null, "paused agent \"" + agent.getName() + "\"", null, null);
									}							
									target.add(countLabel);
									target.add(body);
									selectionColumn.getSelections().clear();
									Session.get().success(_T("Paused selected agents"));	
								});
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
									tag.put("data-tippy-content", _T("Please select agents to pause"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Resume Selected Agents");
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								getTransactionService().run(() -> {
									dropdown.close();
									for (var model: selectionColumn.getSelections()) {
										var agent = model.getObject();
										getAgentService().resume(agent);
										getAuditService().audit(null, "resumed agent \"" + agent.getName() + "\"", null, null);
									}
									target.add(countLabel);
									target.add(body);
									selectionColumn.getSelections().clear();
									Session.get().success(_T("Resumed selected agents"));
								});
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
									tag.put("data-tippy-content", _T("Please select agents to resume"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Restart Selected Agents");
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
										getTransactionService().run(() -> {
											for (IModel<Agent> each: selectionColumn.getSelections()) {
												var agent = each.getObject();
												getAgentService().restart(agent);
												getAuditService().audit(null, "restarted agent \"" + agent.getName() + "\"", null, null);
											}
											target.add(countLabel);
											target.add(body);
											selectionColumn.getSelections().clear();
											Session.get().success(_T("Restart command issued to selected agents"));
										});
									}
									
									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to restart selected agents");
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
									tag.put("data-tippy-content", _T("Please select agents to restart"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Remove Selected Agents");
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
										getTransactionService().run(() -> {
											for (var model: selectionColumn.getSelections()) {
												var agent = model.getObject();
												getAgentService().delete(agent);
												getAuditService().audit(null, "removed agent \"" + agent.getName() + "\"", null, null);
											}
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
										});
									}
									
									@Override
									protected String getConfirmMessage() {
										return _T("Removed selected agents. Type <code>yes</code> below to confirm");
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
									tag.put("data-tippy-content", _T("Please select agents to remove"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Pause All Queried Agents");
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
										getTransactionService().run(() -> {
											for (var it = (Iterator<Agent>) dataProvider.iterator(0, agentsTable.getItemCount()); it.hasNext();) {
												var agent = it.next();
												getAgentService().pause(agent);
												getAuditService().audit(null, "paused agent \"" + agent.getName() + "\"", null, null);
											}
											selectionColumn.getSelections().clear();
											dataProvider.detach();
											target.add(countLabel);
											target.add(body);
											Session.get().success(_T("Paused all queried agents"));
										});
									}
									
									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to pause all queried agents");
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
								setEnabled(agentsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No agents to pause"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Resume All Queried Agents");
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
										getTransactionService().run(() -> {
											for (var it = (Iterator<Agent>) dataProvider.iterator(0, agentsTable.getItemCount()); it.hasNext();) {
												var agent = it.next();
												getAgentService().resume(agent);
												getAuditService().audit(null, "resumed agent \"" + agent.getName() + "\"", null, null);
											}
											dataProvider.detach();
											target.add(countLabel);
											target.add(body);
											selectionColumn.getSelections().clear();
											Session.get().success(_T("Resumed all queried agents"));
										});
									}
									
									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to resume all queried agents");
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
								setEnabled(agentsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No agents to resume"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Restart All Queried Agents");
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
										getTransactionService().run(() -> {
											for (var it = (Iterator<Agent>) dataProvider.iterator(0, agentsTable.getItemCount()); it.hasNext();) {
												var agent = it.next();
												getAgentService().restart(agent);
												getAuditService().audit(null, "restarted agent \"" + agent.getName() + "\"", null, null);
											}
											dataProvider.detach();
											target.add(countLabel);
											target.add(body);
											selectionColumn.getSelections().clear();
											Session.get().success(_T("Restart command issued to all queried agents"));
										});
									}
									
									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to restart all queried agents");
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
								setEnabled(agentsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No agents to restart"));
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Remove All Queried Agents");
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
										getTransactionService().run(() -> {
											for (var it = (Iterator<Agent>) dataProvider.iterator(0, agentsTable.getItemCount()); it.hasNext();) {
												var agent = it.next();
												getAgentService().delete(agent);
												getAuditService().audit(null, "removed agent \"" + agent.getName() + "\"", null, null);
											}
											dataProvider.detach();
											target.add(countLabel);
											target.add(body);
											selectionColumn.getSelections().clear();
										});
									}
									
									@Override
									protected String getConfirmMessage() {
										return _T("Removed all queried agents. Type <code>yes</code> below to confirm");
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
								setEnabled(agentsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No agents to remove"));
								}
							}
							
						};
					}
					
				});
				
				return menuItems;
			}
			
		});
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.setOutputMarkupId(true);
		queryInput.add(new AgentQueryBehavior(false) {

			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				AgentListPanel.this.getFeedbackMessages().clear();
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
				doQuery(target);
			}
			
		});
		add(queryForm);
		
		add(new DropdownLink("addAgent") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new AddAgentPanel(id);
			}

		});

		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return MessageFormat.format(_T("found {0} agents"), dataProvider.size());
				else
					return _T("found 1 agent");
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(dataProvider.size() != 0);
			}
		}.setOutputMarkupPlaceholderTag(true));
		
		dataProvider = new LoadableDetachableDataProvider<Agent, Void>() {

			@Override
			public Iterator<? extends Agent> iterator(long first, long count) {
				try {
					AgentQuery query = queryModel.getObject();
					if (query != null)
						return getAgentService().query(query, (int)first, (int)count).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return new ArrayList<Agent>().iterator();
			}

			@Override
			public long calcSize() {
				try {
					AgentQuery query = queryModel.getObject();
					if (query != null) 
						return getAgentService().count(query.getCriteria());
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<Agent> model(Agent object) {
				Long agentId = object.getId();
				return new LoadableDetachableModel<Agent>() {

					@Override
					protected Agent load() {
						return getAgentService().load(agentId);
					}
					
				};
			}
			
		};
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Agent, Void>> columns = new ArrayList<>();
		
		columns.add(selectionColumn = new SelectionColumn<Agent, Void>());
		
		columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Agent>> cellItem, String componentId, IModel<Agent> rowModel) {
				Fragment fragment = new Fragment(componentId, "agentLinkFrag", AgentListPanel.this);
				Agent agent = rowModel.getObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link",
						AgentOverviewPage.class, AgentOverviewPage.paramsOf(agent));
				link.add(new AgentIcon("icon", rowModel));
				link.add(new Label("label", agent.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("IP Address"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Agent>> cellItem, String componentId, IModel<Agent> rowModel) {
				Agent agent = rowModel.getObject();
				cellItem.add(new Label(componentId, agent.getIpAddress()));
			}

			@Override
			public String getCssClass() {
				return "d-none d-xl-table-cell";
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("Status"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Agent>> cellItem, String componentId, IModel<Agent> rowModel) {
				cellItem.add(new AgentStatusBadge(componentId, rowModel));
			}

		});
		
		body.add(agentsTable = new DefaultDataTable<>("agents", columns, dataProvider,
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));
		
		setOutputMarkupId(true);
	}

	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}

	private TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}
	
}
