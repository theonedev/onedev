package io.onedev.server.web.component.project.list;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.*;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.imports.ProjectImporterContribution;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.project.ChildrenOfCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.ProjectBuildStats;
import io.onedev.server.util.ProjectIssueStats;
import io.onedev.server.util.ProjectPullRequestStats;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ProjectQueryBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.project.ProjectAvatar;
import io.onedev.server.web.component.project.childrentree.ProjectChildrenTree;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.project.stats.build.BuildStatsPanel;
import io.onedev.server.web.component.project.stats.code.CodeStatsPanel;
import io.onedev.server.web.component.project.stats.issue.IssueStatsPanel;
import io.onedev.server.web.component.project.stats.pullrequest.PullRequestStatsPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.imports.ProjectImportPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;
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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {
	
	private final IModel<String> queryStringModel;
	
	private final int expectedCount;
	
	private final IModel<ProjectQuery> queryModel = new LoadableDetachableModel<ProjectQuery>() {

		@Override
		protected ProjectQuery load() {
			ProjectQuery baseQuery = getBaseQuery();
			if (getParentProject() != null)
				baseQuery = ProjectQuery.merge(baseQuery, new ProjectQuery(new ChildrenOfCriteria(getParentProject().getPath())));
			return parse(queryStringModel.getObject(), baseQuery);
		}
		
	};
	
	private final IModel<List<ProjectIssueStats>> issueStatsModel = 
			new LoadableDetachableModel<List<ProjectIssueStats>>() {

		@Override
		protected List<ProjectIssueStats> load() {
			List<Project> projects = new ArrayList<>();
			for (Component row: (WebMarkupContainer)projectsTable.get("body").get("rows")) 
				projects.add((Project) row.getDefaultModelObject());
			return OneDev.getInstance(IssueManager.class).queryStats(projects);
		}
		
	}; 
	
	private final IModel<List<ProjectBuildStats>> buildStatsModel = 
			new LoadableDetachableModel<List<ProjectBuildStats>>() {

		@Override
		protected List<ProjectBuildStats> load() {
			List<Project> projects = new ArrayList<>();
			for (Component row: (WebMarkupContainer)projectsTable.get("body").get("rows")) 
				projects.add((Project) row.getDefaultModelObject());
			return OneDev.getInstance(BuildManager.class).queryStats(projects);
		}
		
	}; 
	
	private final IModel<List<ProjectPullRequestStats>> pullRequestStatsModel = 
			new LoadableDetachableModel<List<ProjectPullRequestStats>>() {

		@Override
		protected List<ProjectPullRequestStats> load() {
			List<Project> projects = new ArrayList<>();
			for (Component row: (WebMarkupContainer)projectsTable.get("body").get("rows")) 
				projects.add((Project) row.getDefaultModelObject());
			return OneDev.getInstance(PullRequestManager.class).queryStats(projects);
		}
		
	}; 
	
	private DataTable<Project, Void> projectsTable;	
	
	private SelectionColumn<Project, Void> selectionColumn;
	
	private SortableDataProvider<Project, Void> dataProvider;	
	
	private WebMarkupContainer body;
	
	private Component saveQueryLink;	
	
	private TextField<String> queryInput;
	
	private boolean querySubmitted = true;
	
	public ProjectListPanel(String id, IModel<String> queryModel, int expectedCount) {
		super(id);
		this.queryStringModel = queryModel;
		this.expectedCount = expectedCount;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	protected void onDetach() {
		pullRequestStatsModel.detach();
		buildStatsModel.detach();
		issueStatsModel.detach();
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
		projectsTable.setCurrentPage(0);
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
				List<String> orderFields = new ArrayList<>(Project.ORDER_FIELDS.keySet());
				return new OrderEditPanel<Project>(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						ProjectQuery query = parse(queryStringModel.getObject(), new ProjectQuery());
						ProjectListPanel.this.getFeedbackMessages().clear();
						if (query != null) 
							return query.getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						ProjectQuery query = parse(queryStringModel.getObject(), new ProjectQuery());
						ProjectListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new ProjectQuery();
						query.getSorts().clear();
						query.getSorts().addAll(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});
		
		boolean canCreateProjects;
		if (getParentProject() != null)
			canCreateProjects = SecurityUtils.canCreateChildren(getParentProject());
		else
			canCreateProjects = true;
		
		add(new MenuLink("operations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Move Selected Projects To...";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new DropdownLink(id) {

							@Override
							protected Component newContent(String id, FloatingPanel dropdown2) {
								return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {
				
									@Override
									protected List<Project> load() {
										return getTargetProjects();
									}
									
								}) {
				
									@Override
									protected void onSelect(AjaxRequestTarget target, Project project) {
										dropdown.close();
										dropdown2.close();
										
										Long projectId = project.getId();
										String errorMessage = null;
										for (IModel<Project> each: selectionColumn.getSelections()) {
											Project eachProject = each.getObject();
											if (!SecurityUtils.canManage(eachProject)) {
												errorMessage = "Project manage privilege required to move '" + eachProject + "'";
												break;
											} else if (eachProject.isSelfOrAncestorOf(project)) {
												errorMessage = "Can not move project '" + eachProject + "' to be under itself or its descendants";
												break;
											} else {
												Project projectWithSameName = getProjectManager().find(project, eachProject.getName());
												if (projectWithSameName != null && !projectWithSameName.equals(eachProject)) {
													errorMessage = "A child project with name '" + eachProject.getName() + "' already exists under '" + project.getPath() + "'";
													break;
												}
											}
										}
										
										if (errorMessage != null) {
											getSession().error(errorMessage);
										} else {
											new ConfirmModalPanel(target) {
												
												private Project getTargetProject() {
													return getProjectManager().load(projectId);
												}
												
												@Override
												protected void onConfirm(AjaxRequestTarget target) {
													Collection<Project> projects = new ArrayList<>();
													for (IModel<Project> each: selectionColumn.getSelections()) 
														projects.add(each.getObject());
													getProjectManager().move(projects, getTargetProject());
													target.add(body);
													selectionColumn.getSelections().clear();
													Session.get().success("Projects moved");
												}
												
												@Override
												protected String getConfirmMessage() {
													return "Type <code>yes</code> below to move selected projects to be under '" + getTargetProject() + "'";
												}
												
												@Override
												protected String getConfirmInput() {
													return "yes";
												}
												
											};
										}
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
									tag.put("title", "Please select projects to move");
								}
							}
							
						};	
					}
					
				});
				
				if (SecurityUtils.canCreateRootProjects()) {
					menuItems.add(new MenuItem() {
	
						@Override
						public String getLabel() {
							return "Set Selected As Root Projects";
						}
	
						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {
	
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									
									String errorMessage = null;
									for (IModel<Project> each: selectionColumn.getSelections()) {
										Project eachProject = each.getObject();
										if (!SecurityUtils.canManage(eachProject)) {
											errorMessage = "Project manage privilege required to modify '" + eachProject + "'";
											break;
										} else {
											Project projectWithSameName = getProjectManager().findByPath(eachProject.getName());
											if (projectWithSameName != null && !projectWithSameName.equals(eachProject)) {
												errorMessage = "A root project with name '" + eachProject.getName() + "' already exists";
												break;
											}
										}
									}
									
									if (errorMessage != null) {
										getSession().error(errorMessage);
									} else {
										new ConfirmModalPanel(target) {
											
											@Override
											protected void onConfirm(AjaxRequestTarget target) {
												Collection<Project> projects = new ArrayList<>();
												for (IModel<Project> each: selectionColumn.getSelections()) 
													projects.add(each.getObject());
												getProjectManager().move(projects, null);
												target.add(body);
												selectionColumn.getSelections().clear();
												Session.get().success("Projects modified");
											}
											
											@Override
											protected String getConfirmMessage() {
												return "Type <code>yes</code> below to set selected as root projects";
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
										tag.put("title", "Please select projects to modify");
									}
								}
								
							};
						}
					});
				}
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete Selected Projects";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								String errorMessage = null;
								for (IModel<Project> each: selectionColumn.getSelections()) { 
									Project eachProject = each.getObject();
									if (!SecurityUtils.canManage(eachProject)) {
										errorMessage = "Project manage privilege required to delete '" + eachProject + "'";
										break;
									}
								}
								if (errorMessage != null) {
									getSession().error(errorMessage);
								} else {
									new ConfirmModalPanel(target) {
										
										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											Collection<Project> projects = new ArrayList<>();
											for (IModel<Project> each: selectionColumn.getSelections())  
												projects.add(each.getObject());
											getProjectManager().delete(projects);
											selectionColumn.getSelections().clear();
											target.add(body);
										}
										
										@Override
										protected String getConfirmMessage() {
											return "Type <code>yes</code> below to delete selected projects";
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
									tag.put("title", "Please select projects to delete");
								}
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Move All Queried Projects To...";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new DropdownLink(id) {

							@Override
							protected Component newContent(String id, FloatingPanel dropdown2) {
								return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {
				
									@Override
									protected List<Project> load() {
										return getTargetProjects();
									}
									
								}) {
				
									@SuppressWarnings("unchecked")
									@Override
									protected void onSelect(AjaxRequestTarget target, Project project) {
										dropdown.close();
										dropdown2.close();
										
										Long projectId = project.getId();
										String errorMessage = null;
										for (Iterator<Project> it = (Iterator<Project>) dataProvider.iterator(0, projectsTable.getItemCount()); it.hasNext();) {
											Project eachProject = it.next();
											if (!SecurityUtils.canManage(eachProject)) {
												errorMessage = "Project manage privilege required to move '" + eachProject + "'";
												break;
											} else if (eachProject.isSelfOrAncestorOf(project)) {
												errorMessage = "Can not move project '" + eachProject + "' to be under itself or its descendants";
												break;
											} else {
												Project projectWithSameName = getProjectManager().find(project, eachProject.getName());
												if (projectWithSameName != null && !projectWithSameName.equals(eachProject)) {
													errorMessage = "A child project with name '" + eachProject.getName() + "' already exists under '" + project.getPath() + "'";
													break;
												}
											}
										}
										
										if (errorMessage != null) {
											getSession().error(errorMessage);
										} else {
											new ConfirmModalPanel(target) {
												
												private Project getTargetProject() {
													return getProjectManager().load(projectId);
												}
												
												@Override
												protected void onConfirm(AjaxRequestTarget target) {
													Collection<Project> projects = new ArrayList<>();
													for (Iterator<Project> it = (Iterator<Project>) dataProvider.iterator(0, projectsTable.getItemCount()); it.hasNext();) 
														projects.add(it.next());
													getProjectManager().move(projects, getTargetProject());
													dataProvider.detach();
													target.add(body);
													selectionColumn.getSelections().clear();
													Session.get().success("Projects moved");
												}
												
												@Override
												protected String getConfirmMessage() {
													return "Type <code>yes</code> below to move all queried projects to be under '" + getTargetProject() + "'";
												}
												
												@Override
												protected String getConfirmInput() {
													return "yes";
												}
												
											};
										}
									}
				
								}.add(AttributeAppender.append("class", "no-current"));
							}
						
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(projectsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No projects to move");
								}
							}
							
						};	
					}
					
				});
				
				if (SecurityUtils.canCreateRootProjects()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return "Set All Queried As Root Projects";
						}
						
						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@SuppressWarnings("unchecked")
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									String errorMessage = null;
									for (Iterator<Project> it = (Iterator<Project>) dataProvider.iterator(0, projectsTable.getItemCount()); it.hasNext();) {
										Project eachProject = it.next();
										if (!SecurityUtils.canManage(eachProject)) {
											errorMessage = "Project manage privilege required to modify '" + eachProject + "'";
											break;
										} else {
											Project projectWithSameName = getProjectManager().findByPath(eachProject.getName());
											if (projectWithSameName != null && !projectWithSameName.equals(eachProject)) {
												errorMessage = "A root project with name '" + eachProject.getName() + "' already exists";
												break;
											}
										}
									}
									
									if (errorMessage != null) {
										getSession().error(errorMessage);
									} else {
										new ConfirmModalPanel(target) {
											
											@Override
											protected void onConfirm(AjaxRequestTarget target) {
												Collection<Project> projects = new ArrayList<>();
												for (Iterator<Project> it = (Iterator<Project>) dataProvider.iterator(0, projectsTable.getItemCount()); it.hasNext();) 
													projects.add(it.next());
												getProjectManager().move(projects, null);
												dataProvider.detach();
												target.add(body);
												selectionColumn.getSelections().clear();
												Session.get().success("Projects modified");
											}
											
											@Override
											protected String getConfirmMessage() {
												return "Type <code>yes</code> below to set all queried as root projects";
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
									setEnabled(projectsTable.getItemCount() != 0);
								}
								
								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("title", "No projects to modify");
									}
								}
								
							};
						}
						
					});
				}
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete All Queried Projects";
					}
					
					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@SuppressWarnings("unchecked")
							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								String errorMessage = null;
								for (Iterator<Project> it = (Iterator<Project>) dataProvider.iterator(0, projectsTable.getItemCount()); it.hasNext();) {
									Project eachProject = it.next();
									if (!SecurityUtils.canManage(eachProject)) {
										errorMessage = "Project manage privilege required to delete '" + eachProject + "'";
										break;
									}
								}
								
								if (errorMessage != null) {
									getSession().error(errorMessage);
								} else {
									new ConfirmModalPanel(target) {
										
										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											Collection<Project> projects = new ArrayList<>();
											for (Iterator<Project> it = (Iterator<Project>) dataProvider.iterator(0, projectsTable.getItemCount()); it.hasNext();) 
												projects.add(it.next());
											getProjectManager().delete(projects);
											dataProvider.detach();
											selectionColumn.getSelections().clear();
											target.add(body);
										}
										
										@Override
										protected String getConfirmMessage() {
											return "Type <code>yes</code> below to delete all queried projects";
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
								setEnabled(projectsTable.getItemCount() != 0);
							}
							
							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "No projects to delete");
								}
							}
							
						};
					}
					
				});
				
				return menuItems;
			}
			
			private List<Project> getTargetProjects() {
				ProjectCache cache = getProjectManager().cloneCache();
				List<Project> projects = new ArrayList<>(getProjectManager().getPermittedProjects(new CreateChildren()));
				projects.sort(cache.comparingPath());
				return projects;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getUser() != null);
			}

			@Override
			protected String getHelp() {
				if (!SecurityUtils.isAdministrator())
					return "Permission will be checked upon actual operation";
				else
					return null;
			}
			
		});
		
		if (getParentProject() == null && canCreateProjects) {
			add(new MenuLink("importProjects") {
	
				@Override
				protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
					Collection<ProjectImporter> importers = new ArrayList<>();
					List<ProjectImporterContribution> contributions = 
							new ArrayList<>(OneDev.getExtensions(ProjectImporterContribution.class));
					Collections.sort(contributions, new Comparator<ProjectImporterContribution>() {
	
						@Override
						public int compare(ProjectImporterContribution o1, ProjectImporterContribution o2) {
							return o1.getOrder() - o2.getOrder();
						}
						
					});
					
					for (ProjectImporterContribution contribution: contributions)
						importers.addAll(contribution.getImporters());
					
					List<MenuItem> menuItems = new ArrayList<>();
					for (ProjectImporter importer: importers) {
						menuItems.add(new MenuItem() {
	
							@Override
							public String getLabel() {
								return "From " + importer.getName();
							}
	
							@Override
							public WebMarkupContainer newLink(String id) {
								return new BookmarkablePageLink<Void>(id, ProjectImportPage.class, 
										ProjectImportPage.paramsOf(importer.getName()));
							}
							
						});
					}
					return menuItems;
				}
				
			});
		} else {
			add(new WebMarkupContainer("importProjects").setVisible(false));
		}
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.setOutputMarkupId(true);
		queryInput.add(new ProjectQueryBehavior(getParentProject() != null, true) {

			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				ProjectListPanel.this.getFeedbackMessages().clear();
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
		
		if (getParentProject() != null) {
			PageParameters params = NewProjectPage.paramsOf(getParentProject());
			add(new BookmarkablePageLink<Void>("addProject", NewProjectPage.class, params)
					.add(AttributeAppender.replace("title", "Add child project"))
					.setVisible(canCreateProjects));
		} else {
			add(new BookmarkablePageLink<Void>("addProject", NewProjectPage.class)
					.setVisible(canCreateProjects));
		}
		
		dataProvider = new LoadableDetachableDataProvider<Project, Void>() {

			@Override
			public Iterator<? extends Project> iterator(long first, long count) {
				try {
					ProjectQuery query = queryModel.getObject();
					if (query != null) 
						return getProjectManager().query(query, (int)first, (int)count).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return new ArrayList<Project>().iterator();
			}

			@Override
			public long calcSize() {
				try {
					ProjectQuery query = queryModel.getObject();
					if (query != null) 
						return getProjectManager().count(query.getCriteria());
				} catch (ExplicitException e) {
					error(e.getMessage());
				}
				return 0;
			}

			@Override
			public IModel<Project> model(Project object) {
				Long projectId = object.getId();
				return new LoadableDetachableModel<Project>() {

					@Override
					protected Project load() {
						return getProjectManager().load(projectId);
					}
					
				};
			}
			
		};
		
		if (expectedCount != 0 && expectedCount != dataProvider.size())
			warn("Some projects might be hidden due to permission policy");
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Project, Void>> columns = new ArrayList<>();
		
		if (SecurityUtils.getUser() != null)
			columns.add(selectionColumn = new SelectionColumn<Project, Void>());
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, IModel<Project> rowModel) {
				Project project = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "projectFrag", ProjectListPanel.this);
				
				Long projectId = project.getId();
				
				ActionablePageLink projectLink = new ActionablePageLink("path", 
						ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Project.class, redirectUrlAfterDelete);
					}
					
				};
				
				projectLink.add(new ProjectAvatar("avatar", projectId));
				if (getParentProject() != null)
					projectLink.add(new Label("text", project.getPath().substring(getParentProject().getPath().length()+1)));
				else
					projectLink.add(new Label("text", project.getPath()));
				fragment.add(projectLink);

				fragment.add(new EntityLabelsPanel<ProjectLabel>("labels", rowModel));
				
				if (project.getActiveServer(false) != null) {
					if (project.isCodeManagement() && SecurityUtils.canReadCode(project)) {
						fragment.add(new CodeStatsPanel("codeStats", rowModel));
						fragment.add(new PullRequestStatsPanel("pullRequestStats", rowModel, 
								new LoadableDetachableModel<Map<PullRequest.Status, Long>>() {
	
							@Override
							protected Map<PullRequest.Status, Long> load() {
								Map<PullRequest.Status, Long> statusCounts = new LinkedHashMap<>();
								for (ProjectPullRequestStats stats: pullRequestStatsModel.getObject()) {
									if (stats.getProjectId().equals(projectId)) 
										statusCounts.put(stats.getPullRequestStatus(), stats.getStatusCount());
								}
								return statusCounts;
							}
							
						}));
					} else {
						fragment.add(new WebMarkupContainer("codeStats").setVisible(false));
						fragment.add(new WebMarkupContainer("pullRequestStats").setVisible(false));
					}
					
					if (project.isIssueManagement()) {
						fragment.add(new IssueStatsPanel("issueStats", rowModel, new LoadableDetachableModel<>() {

							@Override
							protected Map<Integer, Long> load() {
								Map<Integer, Long> stateCounts = new LinkedHashMap<>();
								GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
								for (ProjectIssueStats stats : issueStatsModel.getObject()) {
									if (stats.getProjectId().equals(projectId)
											&& stats.getStateOrdinal() >= 0
											&& stats.getStateOrdinal() < issueSetting.getStateSpecs().size()) {
										stateCounts.put(stats.getStateOrdinal(), stats.getStateCount());
									}
								}
								return stateCounts;
							}

						}));
					} else {
						fragment.add(new WebMarkupContainer("issueStats").setVisible(false));
					}
					
					if (project.isCodeManagement()) {
						fragment.add(new BuildStatsPanel("buildStats", rowModel, new LoadableDetachableModel<Map<Build.Status, Long>>() {
	
							@Override
							protected Map<Build.Status, Long> load() {
								Map<Build.Status, Long> statusCounts = new LinkedHashMap<>();
								for (ProjectBuildStats stats: buildStatsModel.getObject()) {
									if (stats.getProjectId().equals(projectId)) 
										statusCounts.put(stats.getBuildStatus(), stats.getStatusCount());
								}
								return statusCounts;
							}
							
						}));
					} else {
						fragment.add(new WebMarkupContainer("buildStats").setVisible(false));
					}
					fragment.add(new WebMarkupContainer("noStorage").setVisible(false));
				} else {
					fragment.add(new WebMarkupContainer("codeStats").setVisible(false));
					fragment.add(new WebMarkupContainer("pullRequestStats").setVisible(false));
					fragment.add(new WebMarkupContainer("issueStats").setVisible(false));
					fragment.add(new WebMarkupContainer("buildStats").setVisible(false));
					fragment.add(new WebMarkupContainer("noStorage"));
				}
				
				List<ProjectFacade> children = getProjectManager().getChildren(projectId);
				if (!children.isEmpty()) {
					Fragment childrenFrag = new Fragment("children", "childrenFrag", ProjectListPanel.this);
					childrenFrag.add(new AjaxLink<Void>("toggle") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (WebSession.get().getExpandedProjectIds().contains(projectId))
								WebSession.get().getExpandedProjectIds().remove(projectId);
							else
								WebSession.get().getExpandedProjectIds().add(projectId);
							target.add(childrenFrag);
						}
						
					}.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

						@Override
						protected String load() {
							return WebSession.get().getExpandedProjectIds().contains(projectId)? "expanded": "collapsed";
						}
						
					})));
					
 					childrenFrag.add(new BookmarkablePageLink<Void>("link", ProjectChildrenPage.class, 
 							ProjectChildrenPage.paramsOf(projectId)) {

						@Override
						protected void onInitialize() {
							super.onInitialize();
		 					add(new Label("label", children.size() + " child projects"));
						}
 						
 					});
					
					childrenFrag.add(new ProjectChildrenTree("tree", projectId) {
						
						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(WebSession.get().getExpandedProjectIds().contains(projectId));
						}

						@Override
						protected Set<Long> getExpandedProjectIds() {
							return WebSession.get().getExpandedProjectIds();
						}
						
					});
		
					childrenFrag.setOutputMarkupId(true);
					fragment.add(childrenFrag);
				} else {
					fragment.add(new WebMarkupContainer("children").setVisible(false));
				}
				
				cellItem.add(fragment);
			}
			
		});
		
		body.add(projectsTable = new DefaultDataTable<Project, Void>("projects", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	private ProjectQuery parse(@Nullable String queryString, ProjectQuery baseQuery) {
		try {
			return ProjectQuery.merge(baseQuery, ProjectQuery.parse(queryString));
		} catch (Exception e) {
			getFeedbackMessages().clear();
			if (e instanceof ExplicitException)
				error(e.getMessage());
			else 
				error("Malformed query");
			return null;
		}
	}
	
	@Nullable
	protected Project getParentProject() {
		return null;
	}
	
	protected ProjectQuery getBaseQuery() {
		return new ProjectQuery();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectListCssResourceReference()));
	}
		
}
