package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.imports.IssueImporterContribution;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entitytext.IssueTextManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.LinkSide;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener.AttachMode;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.link.IssueLinkPanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.imports.IssueImportPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public abstract class IssueListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<Object> queryModel = new LoadableDetachableModel<Object>() {

		@Override
		protected Object load() {
			return parse(queryStringModel.getObject(), getBaseQuery());
		}
		
	};
	
	private DataTable<Issue, Void> issuesTable;
	
	private SelectionColumn<Issue, Void> selectionColumn;
	
	private SortableDataProvider<Issue, Void> dataProvider;	
	
	private WebMarkupContainer body;	
	
	private Component saveQueryLink;
	
	private TextField<String> queryInput;
	
	private boolean querySubmitted = true;
	
	public IssueListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueTextManager getIssueTextManager() {
		return OneDev.getInstance(IssueTextManager.class);
	}
	
	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();
	
	protected IssueQuery getBaseQuery() {
		return new IssueQuery();
	}
	
	@Nullable
	private Object parse(@Nullable String queryString, IssueQuery baseQuery) {
		IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
		try {
			return IssueQuery.merge(baseQuery, IssueQuery.parse(getProject(), queryString, option, true));
		} catch (Exception e) {
			if (e instanceof ExplicitException) {
				error(e.getMessage());
				return null;
			} else if (getBaseQuery().toString() != null) {
				error("Malformed issue query");
				return null;
			} else {
				info("Performing fuzzy query");
				return queryString;
			}
		}
	}

	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	private GlobalIssueSetting getGlobalIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private void doQuery(AjaxRequestTarget target) {
		issuesTable.setCurrentPage(0);
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
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				List<String> orderFields = new ArrayList<>(Issue.ORDER_FIELDS.keySet());
				if (getProject() != null)
					orderFields.remove(Issue.NAME_PROJECT);
				for (FieldSpec field: getGlobalIssueSetting().getFieldSpecs()) {
					if (field instanceof IntegerField || field instanceof ChoiceField || field instanceof DateField) 
						orderFields.add(field.getName());
				}
				
				return new OrderEditPanel<Issue>(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						Object query = parse(queryStringModel.getObject(), new IssueQuery());
						IssueListPanel.this.getFeedbackMessages().clear();
						if (query instanceof IssueQuery) 
							return ((IssueQuery)query).getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						Object query = parse(queryStringModel.getObject(), new IssueQuery());
						IssueListPanel.this.getFeedbackMessages().clear();
						if (!(query instanceof IssueQuery))
							query = new IssueQuery();
						((IssueQuery)query).getSorts().clear();
						((IssueQuery)query).getSorts().addAll(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});	
		
		add(new MenuLink("import") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				Collection<IssueImporter<? extends Serializable, ? extends Serializable, ? extends Serializable>> importers = new ArrayList<>();
				
				List<IssueImporterContribution> contributions = 
						new ArrayList<>(OneDev.getExtensions(IssueImporterContribution.class));
				Collections.sort(contributions, new Comparator<IssueImporterContribution>() {

					@Override
					public int compare(IssueImporterContribution o1, IssueImporterContribution o2) {
						return o1.getOrder() - o2.getOrder();
					}
					
				});
				
				for (IssueImporterContribution contribution: contributions)
					importers.addAll(contribution.getImporters());
				
				List<MenuItem> menuItems = new ArrayList<>();
				for (IssueImporter<? extends Serializable, ? extends Serializable, ? extends Serializable> importer: importers) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return "From " + importer.getName();
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new BookmarkablePageLink<Void>(id, IssueImportPage.class, 
									IssueImportPage.paramsOf(getProject(), importer.getName()));
						}
						
					});
				}
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (getPage() instanceof ProjectIssueListPage) {
					ProjectIssueListPage page = (ProjectIssueListPage) getPage();
					setVisible(SecurityUtils.canManageIssues(page.getProject()));
				} else {
					setVisible(false);
				}
			}
			
		});
		
		queryInput = new TextField<String>("input", queryStringModel);
		
		IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
		
		queryInput.add(new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, option) {
			
			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				IssueListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				List<String> hints = super.getHints(terminalExpect);
				if (getBaseQuery().toString() == null)
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
				IssueListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
			
		});
		add(queryForm);
		
		if (getProject() == null) {
			add(new DropdownLink("newIssue") {

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new ProjectSelector(id, new LoadableDetachableModel<Collection<Project>>() {
	
						@Override
						protected Collection<Project> load() {
							List<Project> projects = new ArrayList<>(OneDev.getInstance(ProjectManager.class)
									.getPermittedProjects(new AccessProject()));
							
							Predicate<Project> issueManagementEnabledPredicate = item -> item.isIssueManagement();
							CollectionUtils.filter(projects, issueManagementEnabledPredicate);							
							
							Collections.sort(projects, new Comparator<Project>() {
	
								@Override
								public int compare(Project o1, Project o2) {
									return o1.getPath().compareTo(o2.getPath());
								}
								
							});
							return projects;
						}
						
					}) {
	
						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							PageParameters params = NewIssuePage.paramsOf(project, getBaseQuery().toString());
							setResponsePage(NewIssuePage.class, params);
						}
	
					}.add(AttributeAppender.append("class", "no-current"));
				}
			
			});	
		} else {
			PageParameters params = NewIssuePage.paramsOf(getProject(), getBaseQuery().toString());
			add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, params));
		}
		
		add(new ModalLink("fieldsAndLinks") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldsAndLinksFrag", IssueListPanel.this);
				Form<?> form = new Form<Void>("form");
				
				FieldsAndLinksBean bean = new FieldsAndLinksBean();
				
				bean.setFields(getListFields());
				bean.setLinks(getListLinks());
				
				form.add(BeanContext.edit("editor", bean));
				
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						modal.close();
						if (getProject() != null) {
							getProject().getIssueSetting().setListFields(bean.getFields());
							getProject().getIssueSetting().setListLinks(bean.getLinks());
							OneDev.getInstance(ProjectManager.class).save(getProject());
						} else {
							getGlobalIssueSetting().setListFields(bean.getFields());
							getGlobalIssueSetting().setListLinks(bean.getLinks());
							OneDev.getInstance(SettingManager.class).saveIssueSetting(getGlobalIssueSetting());
						}
						target.add(body);
					}
					
				});
				
				form.add(new AjaxLink<Void>("useDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
						getProject().getIssueSetting().setListFields(null);
						getProject().getIssueSetting().setListLinks(null);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(body);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getProject() != null 
								&& getProject().getIssueSetting().getListFields() != null
								&& getProject().getIssueSetting().getListLinks() != null);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});

				fragment.add(form);
				
				return fragment;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator() || getProject() != null && SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		
		add(new MenuLink("operations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Batch Edit Selected Issues";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ModalLink(id) {

							@Override
							protected String getModalCssClass() {
								return "modal-lg";
							}

							@Override
							protected Component newContent(String id, ModalPanel modal) {
								dropdown.close();
								
								return new BatchEditPanel(id) {

									@Override
									protected Project getProject() {
										return IssueListPanel.this.getProject();
									}

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										modal.close();
									}

									@Override
									protected void onUpdated(AjaxRequestTarget target) {
										modal.close();
										selectionColumn.getSelections().clear();
										target.add(body);
									}

									@Override
									protected Iterator<Issue> getIssueIterator() {
										List<Issue> issues = new ArrayList<>();
										for (IModel<Issue> each: selectionColumn.getSelections())
											issues.add(each.getObject());
										return issues.iterator();
									}

									@Override
									protected int getIssueCount() {
										return selectionColumn.getSelections().size();
									}

									@Override
									protected IssueQuery getIssueQuery() {
										if (queryModel.getObject() instanceof IssueQuery)
											return (IssueQuery) queryModel.getObject();
										else
											return null;
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
									tag.put("title", "Please select issues to edit");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Move Selected Issues To...";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new DropdownLink(id) {

							@Override
							protected Component newContent(String id, FloatingPanel dropdown2) {
								return new ProjectSelector(id, new LoadableDetachableModel<Collection<Project>>() {
				
									@Override
									protected Collection<Project> load() {
										return getTargetProjects();
									}
									
								}) {
				
									@Override
									protected void onSelect(AjaxRequestTarget target, Project project) {
										dropdown.close();
										dropdown2.close();
										
										Long projectId = project.getId();
										new ConfirmModalPanel(target) {
											
											private Project getTargetProject() {
												return OneDev.getInstance(ProjectManager.class).load(projectId);
											}
											
											@Override
											protected void onConfirm(AjaxRequestTarget target) {
												Collection<Issue> issues = new ArrayList<>();
												for (IModel<Issue> each: selectionColumn.getSelections())
													issues.add(each.getObject());
												OneDev.getInstance(IssueManager.class).move(getTargetProject(), issues);
												selectionColumn.getSelections().clear();
												target.add(body);
											}
											
											@Override
											protected String getConfirmMessage() {
												return "Type <code>yes</code> below to move selected issues to project '" + getTargetProject() + "'";
											}
											
											@Override
											protected String getConfirmInput() {
												return "yes";
											}
											
										};
									}
				
								}.add(AttributeAppender.append("class", "no-current"));
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
									tag.put("title", "Please select issues to move");
								}
							}
							
						};	
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete Selected Issues";
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
										Collection<Issue> issues = new ArrayList<>();
										for (IModel<Issue> each: selectionColumn.getSelections())
											issues.add(each.getObject());
										OneDev.getInstance(IssueManager.class).delete(issues);
										selectionColumn.getSelections().clear();
										target.add(body);
									}
									
									@Override
									protected String getConfirmMessage() {
										return "Type <code>yes</code> below to delete selected issues";
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
									tag.put("title", "Please select issues to delete");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Batch Edit All Queried Issues";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ModalLink(id) {

							@Override
							protected String getModalCssClass() {
								return "modal-lg";
							}
							
							@Override
							protected Component newContent(String id, ModalPanel modal) {
								dropdown.close();
								
								return new BatchEditPanel(id) {

									@Override
									protected Project getProject() {
										return IssueListPanel.this.getProject();
									}

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										modal.close();
									}

									@Override
									protected void onUpdated(AjaxRequestTarget target) {
										modal.close();
										selectionColumn.getSelections().clear();
										target.add(body);
									}

									@Override
									protected Iterator<? extends Issue> getIssueIterator() {
										return dataProvider.iterator(0, issuesTable.getItemCount());
									}

									@Override
									protected int getIssueCount() {
										return (int) issuesTable.getItemCount();
									}

									@Override
									protected IssueQuery getIssueQuery() {
										if (queryModel.getObject() instanceof IssueQuery)
											return (IssueQuery) queryModel.getObject();
										else
											return null;
									}

								};
							}
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(issuesTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No issues to edit");
								}
							}
							
						};						
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Move All Queried Issues To...";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new DropdownLink(id) {

							@Override
							protected Component newContent(String id, FloatingPanel dropdown2) {
								return new ProjectSelector(id, new LoadableDetachableModel<Collection<Project>>() {
				
									@Override
									protected Collection<Project> load() {
										return getTargetProjects();
									}
									
								}) {
				
									@SuppressWarnings("unchecked")
									@Override
									protected void onSelect(AjaxRequestTarget target, Project project) {
										dropdown.close();
										dropdown2.close();
										
										Long projectId = project.getId();
										new ConfirmModalPanel(target) {
											
											private Project getTargetProject() {
												return OneDev.getInstance(ProjectManager.class).load(projectId);
											}
											
											@Override
											protected void onConfirm(AjaxRequestTarget target) {
												Collection<Issue> issues = new ArrayList<>();
												for (Iterator<Issue> it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext();) {
													issues.add(it.next());
												}
												OneDev.getInstance(IssueManager.class).move(getTargetProject(), issues);
												selectionColumn.getSelections().clear();
												target.add(body);
											}
											
											@Override
											protected String getConfirmMessage() {
												return "Type <code>yes</code> below to move all queried issues to project '" + getTargetProject() + "'";
											}
											
											@Override
											protected String getConfirmInput() {
												return "yes";
											}
											
										};
									}
				
								}.add(AttributeAppender.append("class", "no-current"));
							}
						
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(issuesTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No issues to move");
								}
							}
							
						};	
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete All Queried Issues";
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
										Collection<Issue> issues = new ArrayList<>();
										for (Iterator<Issue> it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext();) 
											issues.add(it.next());
										OneDev.getInstance(IssueManager.class).delete(issues);
										selectionColumn.getSelections().clear();
										target.add(body);
									}
									
									@Override
									protected String getConfirmMessage() {
										return "Type <code>yes</code> below to delete all queried issues";
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
								setEnabled(issuesTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No issues to delete");
								}
							}
							
						};
					}
					
				});
				
				return menuItems;
			}
			
			private Collection<Project> getTargetProjects() {
				List<Project> projects = new ArrayList<>(OneDev.getInstance(ProjectManager.class)
						.getPermittedProjects(new AccessProject()));
				
				Predicate<Project> issueManagementEnabledPredicate = item -> item.isIssueManagement();
				CollectionUtils.filter(projects, issueManagementEnabledPredicate);							
				projects.remove(getProject());
				
				Collections.sort(projects, new Comparator<Project>() {

					@Override
					public int compare(Project o1, Project o2) {
						return o1.getPath().compareTo(o2.getPath());
					}
					
				});
				return projects;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null && SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		
		dataProvider = new LoadableDetachableDataProvider<Issue, Void>() {

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				try {
					Object query = queryModel.getObject();
					if (query instanceof IssueQuery) {
						return getIssueManager().query(getProjectScope(), (IssueQuery)query, 
								true, (int)first, (int)count).iterator();
					} else if (query instanceof String) {
						return getIssueTextManager().query(getProjectScope(), (String)query, 
								true, (int)first, (int)count).iterator();
					}
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return new ArrayList<Issue>().iterator();
			}

			@Override
			public long calcSize() {
				try {
					Object query = queryModel.getObject();
					if (query instanceof IssueQuery) 
						return getIssueManager().count(getProjectScope(), ((IssueQuery)query).getCriteria());
					else if (query instanceof String) 
						return getIssueTextManager().count(getProjectScope(), (String) query);
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<Issue> model(Issue object) {
				Long issueId = object.getId();
				return new LoadableDetachableModel<Issue>() {

					@Override
					protected Issue load() {
						return getIssueManager().load(issueId);
					}
					
				};
			}
			
		};
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Issue, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId, IModel<Issue> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}
			
		});
		
		if (getProject() != null && SecurityUtils.canManageIssues(getProject())) 
			columns.add(selectionColumn = new SelectionColumn<Issue, Void>());
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId,
					IModel<Issue> rowModel) {
				Item<?> row = cellItem.findParent(Item.class);
				Cursor cursor = new Cursor(queryModel.getObject().toString(), (int)issuesTable.getItemCount(), 
						(int)issuesTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProjectScope());
				cellItem.add(newIssueDetail(componentId, rowModel.getObject().getId(), cursor));
			}
			
			@SuppressWarnings("unchecked")
			private Component newIssueDetail(String componentId, Long issueId, @Nullable Cursor cursor) {
				Fragment fragment = new Fragment(componentId, "contentFrag", IssueListPanel.this);
				fragment.setDefaultModel(new LoadableDetachableModel<Issue>() {

					@Override
					protected Issue load() {
						return getIssueManager().load(issueId);
					}
					
				});

				Issue issue = (Issue) fragment.getDefaultModelObject();
				
				fragment.add(new IssueLinkPanel("numberAndTitle") {

					@Override
					protected Issue getIssue() {
						return (Issue) fragment.getDefaultModelObject();
					}

					@Override
					protected Project getCurrentProject() {
						return getProject();
					}

					@Override
					protected Cursor getCursor() {
						return cursor;
					}
					
				});
				
				String copyContent = issue.getNumberAndTitle();
				if (!issue.getProject().equals(getProject()))
					copyContent = issue.getProject().getPath() + copyContent;
				fragment.add(new CopyToClipboardLink("copy", Model.of(copyContent)));
				
				AtomicReference<String> expandedLinkName = new AtomicReference<>(null);
				
				RepeatingView linksView = new RepeatingView("links");
				for (String linkName: getListLinks()) {
					int count = 0;
					for (IssueLink link: issue.getTargetLinks()) {
						LinkSpec spec = link.getSpec();
						if (spec.getName().equals(linkName))
							count++;
					}
					for (IssueLink link: issue.getSourceLinks()) {
						LinkSpec spec = link.getSpec();
						if (spec.getOpposite() == null && spec.getName().equals(linkName))
							count++;
					}
					if (count != 0) {
						AjaxLink<Void> link = new AjaxLink<Void>(linksView.newChildId()) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								if (linkName.equals(expandedLinkName.get()))
									expandedLinkName.set(null);
								else
									expandedLinkName.set(linkName);
								target.add(fragment);
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								if (linkName.equals(expandedLinkName.get()))
									tag.put("class", tag.getAttribute("class") + " expanded");
							}
							
						};
						link.add(new Label("label", linkName));
						linksView.add(link);
					}
				}
				fragment.add(linksView);
				
				fragment.add(new Label("votes", issue.getVoteCount()));
				fragment.add(new Label("comments", issue.getCommentCount()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String field: getListFields()) {
					if (field.equals(Issue.NAME_STATE)) {
						Fragment stateFragment = new Fragment(fieldsView.newChildId(), 
								"stateFrag", IssueListPanel.this);
						AjaxLink<Void> transitLink = new TransitionMenuLink("transit") {

							@Override
							protected Issue getIssue() {
								return (Issue) fragment.getDefaultModelObject();
							}

							@Override
							protected void onTransited(AjaxRequestTarget target) {
							}
							
						};
						
						transitLink.add(new IssueStateBadge("state", (IModel<Issue>) fragment.getDefaultModel()));
						stateFragment.add(transitLink);
						
						fieldsView.add(stateFragment.setOutputMarkupId(true));
					} else {
						fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), Mode.AVATAR_AND_NAME, true) {

							@Override
							protected void onUpdated(IPartialPageRequestHandler handler) {
							}

							@SuppressWarnings("deprecation")
							@Override
							protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
								return new AttachAjaxIndicatorListener(
										fieldsView.get(fieldsView.size()-1), AttachMode.APPEND, false);
							}

							@Override
							protected Issue getIssue() {
								return (Issue) fragment.getDefaultModelObject();
							}

							@Override
							protected Input getField() {
								Issue issue = (Issue) fragment.getDefaultModelObject();
								if (issue.isFieldVisible(field))
									return issue.getFieldInputs().get(field);
								else
									return null;
							}
							
						}.setOutputMarkupId(true));
					}
				}	
				fragment.add(fieldsView);
				
				LastUpdate lastUpdate = issue.getLastUpdate();
				if (lastUpdate.getUser() != null) 
					fragment.add(new UserIdentPanel("user", lastUpdate.getUser(), Mode.NAME));
				else 
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				fragment.add(new Label("activity", lastUpdate.getActivity()));
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate()))
					.add(new AttributeAppender("title", DateUtils.formatDateTime(lastUpdate.getDate()))));

				fragment.add(new ListView<Issue>("linkedIssues", new LoadableDetachableModel<List<Issue>>() {

					@Override
					protected List<Issue> load() {
						Issue issue = (Issue) fragment.getDefaultModelObject();
						OneDev.getInstance(IssueLinkManager.class).loadDeepLinks(issue);
						LinkSide side = new LinkSide(expandedLinkName.get());
						return issue.findLinkedIssues(side.getSpec(), side.isOpposite());
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<Issue> item) {
						Issue issue = item.getModelObject();
						if (SecurityUtils.canAccess(issue)) {
							item.add(newIssueDetail("content", issue.getId(), null));
						} else {
							Fragment fragment = new Fragment("content", "unauthorizedLinkedIssueFrag", IssueListPanel.this);
							if (getProject().equals(issue.getProject()))
								fragment.add(new Label("number", "#" + issue.getNumber()));
							else
								fragment.add(new Label("number", issue.getProject().getPath() + "#" + issue.getNumber()));
							item.add(fragment);
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(expandedLinkName.get() != null);
					}
					
				});
				
				fragment.add(new WebSocketObserver() {
					
					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler) {
						Component detail = newIssueDetail(componentId, issueId, cursor);
						fragment.replaceWith(detail);
						handler.add(detail);
					}
					
					@Override
					public Collection<String> getObservables() {
						return Sets.newHashSet(Issue.getWebSocketObservable(issueId));
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				
				return fragment;
			}
			
		});
		
		body.add(issuesTable = new DataTable<Issue, Void>("issues", columns, dataProvider, WebConstants.PAGE_SIZE) {

			@Override
			protected Item<Issue> newRowItem(String id, int index, IModel<Issue> model) {
				Item<Issue> item = super.newRowItem(id, index, model);
				Issue issue = model.getObject();
				item.add(AttributeAppender.append("class", 
						issue.isVisitedAfter(issue.getLastUpdate().getDate())?"issue":"issue new"));
				return item;
			}
			
		});
		
		if (getPagingHistorySupport() != null)
			issuesTable.setCurrentPage(getPagingHistorySupport().getCurrentPage());
		issuesTable.addBottomToolbar(new NavigationToolbar(issuesTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new OnePagingNavigator(navigatorId, table, getPagingHistorySupport());
			}
			
		});
		issuesTable.addBottomToolbar(new NoRecordsToolbar(issuesTable));
		issuesTable.add(new NoRecordsBehavior());

		setOutputMarkupId(true);
	}
	
	@Nullable
	private ProjectScope getProjectScope() {
		if (getProject() != null)
			return new ProjectScope(getProject(), true, true);
		else
			return null;
	}
	
	private List<String> getListFields() {
		Project current = getProject();
		while (current != null) {
			List<String> listFields = current.getIssueSetting().getListFields();
			if (listFields != null)
				return listFields;
			current = current.getParent();
		}
		return getGlobalIssueSetting().getListFields();
	}
	
	private List<String> getListLinks() {
		Project current = getProject();
		while (current != null) {
			List<String> listLinks = current.getIssueSetting().getListLinks();
			if (listLinks != null)
				return listLinks;
			current = current.getParent();
		}
		return getGlobalIssueSetting().getListLinks();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListCssResourceReference()));
	}
	
}

