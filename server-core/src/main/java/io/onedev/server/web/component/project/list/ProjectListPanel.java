package io.onedev.server.web.component.project.list;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.imports.ProjectImporterContribution;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.StatusCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.search.entity.project.ChildrenOfCriteria;
import io.onedev.server.search.entity.project.NameCriteria;
import io.onedev.server.search.entity.project.PathCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectBuildStats;
import io.onedev.server.util.ProjectIssueStats;
import io.onedev.server.util.ProjectPullRequestStats;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ProjectQueryBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.component.project.childrentree.ProjectChildrenTree;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.imports.ProjectImportPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {
	
	private final IModel<String> queryStringModel;
	
	private final int expectedCount;
	
	private final IModel<ProjectQuery> queryModel = new LoadableDetachableModel<ProjectQuery>() {

		@Override
		protected ProjectQuery load() {
			ProjectQuery baseQuery;
			if (getParentProject() != null)
				baseQuery = new ProjectQuery(new ChildrenOfCriteria(getParentProject().getPath()));
			else
				baseQuery = new ProjectQuery();
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
			canCreateProjects = SecurityUtils.canCreateProjects();
		
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
			
			private Collection<Project> getTargetProjects() {
				List<Project> projects = new ArrayList<>(getProjectManager().getPermittedProjects(new CreateChildren()));
				
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
					Collection<ProjectImporter<? extends Serializable, ? extends Serializable, ? extends Serializable>> importers = new ArrayList<>();
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
					for (ProjectImporter<? extends Serializable, ? extends Serializable, ? extends Serializable> importer: importers) {
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
		queryInput.add(new ProjectQueryBehavior(getParentProject() != null) {

			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				ProjectListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				List<String> hints = super.getHints(terminalExpect);
				hints.add("Free input for fuzzy query on name/path");
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
				Fragment fragment = new Fragment(componentId, "projectFrag", ProjectListPanel.this);
				Project project = rowModel.getObject();
				
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
					projectLink.add(new Label("label", project.getPath().substring(getParentProject().getPath().length()+1)));
				else
					projectLink.add(new Label("label", project.getPath()));
				fragment.add(projectLink);
				
				fragment.add(new Label("lastUpdate", "Updated " + DateUtils.formatAge(project.getUpdateDate())));
				
				int commitCount;
				if (project.isCodeManagement() && SecurityUtils.canReadCode(project)
						&& (commitCount=OneDev.getInstance(CommitInfoManager.class).getCommitCount(project)) != 0) {
					Fragment commitInfoFrag = new Fragment("codeInfo", "codeInfoFrag", ProjectListPanel.this);
					PageParameters params = ProjectCommitsPage.paramsOf(project, null);
					Link<Void> commitsLink = new BookmarkablePageLink<Void>("commits", ProjectCommitsPage.class, params);
					commitsLink.add(new Label("label", commitCount + " commits"));
					commitInfoFrag.add(commitsLink);
					
					params = ProjectBranchesPage.paramsOf(project);
					Link<Void> branchesLink = new BookmarkablePageLink<Void>("branches", ProjectBranchesPage.class, params);
					try {
						int branchCount = project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_HEADS).size();
						branchesLink.add(new Label("label", branchCount + " branches"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					commitInfoFrag.add(branchesLink);
					
					params = ProjectTagsPage.paramsOf(project);
					Link<Void> tagsLink = new BookmarkablePageLink<Void>("tags", ProjectTagsPage.class, params);
					try {
						int tagCount = project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_TAGS).size();
						tagsLink.add(new Label("label", tagCount + " tags"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					commitInfoFrag.add(tagsLink);
					
					fragment.add(commitInfoFrag);
					
					IModel<Integer> totalCountModel = new LoadableDetachableModel<Integer>() {

						@Override
						protected Integer load() {
							int totalCount = 0;
							for (ProjectPullRequestStats stats: pullRequestStatsModel.getObject()) {
								if (stats.getProjectId().equals(projectId)) 
									totalCount += stats.getStatusCount();
							}
							return totalCount;
						}
						
					};
					Fragment pullRequestInfoFrag = new Fragment("pullRequestInfo", "pullRequestInfoFrag", ProjectListPanel.this) {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(totalCountModel.getObject() != 0);
						}
						
					};
					pullRequestInfoFrag.setDefaultModel(totalCountModel);

					params = ProjectPullRequestsPage.paramsOf(project);
					Link<Void> pullRequestsLink = new BookmarkablePageLink<Void>("pullRequests", ProjectPullRequestsPage.class, params);
					pullRequestsLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return totalCountModel.getObject() + " pull requests";
						}
						
					}));
					pullRequestInfoFrag.add(pullRequestsLink);
					
					pullRequestInfoFrag.add(new ListView<ProjectPullRequestStats>("statuses", 
							new LoadableDetachableModel<List<ProjectPullRequestStats>>() {

						@Override
						protected List<ProjectPullRequestStats> load() {
							List<ProjectPullRequestStats> listOfPullRequestStats = new ArrayList<>();
							for (ProjectPullRequestStats stats: pullRequestStatsModel.getObject()) {
								if (stats.getProjectId().equals(projectId)) 
									listOfPullRequestStats.add(stats);
							}
							return listOfPullRequestStats;
						}
						
					}) {

						@Override
						protected void populateItem(ListItem<ProjectPullRequestStats> item) {
							ProjectPullRequestStats stats = item.getModelObject();
							PullRequestQuery query = new PullRequestQuery(
									new io.onedev.server.search.entity.pullrequest.StatusCriteria(stats.getPullRequestStatus()));
							PageParameters params = ProjectPullRequestsPage.paramsOf(rowModel.getObject(), query.toString(), 0);
							Link<Void> statusLink = new BookmarkablePageLink<Void>("link", ProjectPullRequestsPage.class, params);
							String statusName = stats.getPullRequestStatus().toString();
							statusLink.add(new Label("label", stats.getStatusCount() + " " + statusName));
							
							String cssClass;
							switch (stats.getPullRequestStatus()) {
							case OPEN:
								cssClass = "link-warning";
								break;
							case MERGED:
								cssClass = "link-success";
								break;
							default:
								cssClass = "link-danger";
								break;
							}
							statusLink.add(AttributeAppender.append("class", cssClass));
							item.add(statusLink);
						}
						
					});
					
					fragment.add(pullRequestInfoFrag);
				} else {
					fragment.add(new WebMarkupContainer("codeInfo").setVisible(false));
					fragment.add(new WebMarkupContainer("pullRequestInfo").setVisible(false));
				}
				
				if (project.isIssueManagement()) {
					IModel<Integer> totalCountModel = new LoadableDetachableModel<Integer>() {

						@Override
						protected Integer load() {
							int totalCount = 0;
							for (ProjectIssueStats stats: issueStatsModel.getObject()) {
								if (stats.getProjectId().equals(projectId)) 
									totalCount += stats.getStateCount();
							}
							return totalCount;
						}
						
					};
					Fragment issueInfoFrag = new Fragment("issueInfo", "issueInfoFrag", ProjectListPanel.this) {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(totalCountModel.getObject() != 0);
						}
						
					};
					issueInfoFrag.setDefaultModel(totalCountModel);

					PageParameters params = ProjectIssueListPage.paramsOf(project, null, 0);
					Link<Void> issuesLink = new BookmarkablePageLink<Void>("issues", ProjectIssueListPage.class, params);
					issuesLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return totalCountModel.getObject() + " issues";
						}
						
					}));
					issueInfoFrag.add(issuesLink);
					
					GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
					
					issueInfoFrag.add(new ListView<ProjectIssueStats>("states", new LoadableDetachableModel<List<ProjectIssueStats>>() {

						@Override
						protected List<ProjectIssueStats> load() {
							List<ProjectIssueStats> listOfIssueStats = new ArrayList<>();
							for (ProjectIssueStats stats: issueStatsModel.getObject()) {
								if (stats.getProjectId().equals(projectId) 
										&& stats.getStateOrdinal() < issueSetting.getStateSpecs().size()) {
									listOfIssueStats.add(stats);
								}
							}
							return listOfIssueStats;
						}
						
					}) {

						@Override
						protected void populateItem(ListItem<ProjectIssueStats> item) {
							Project project = rowModel.getObject();
							ProjectIssueStats stats = item.getModelObject();
							StateSpec stateSpec = issueSetting.getStateSpecs().get(stats.getStateOrdinal());
							IssueQuery query = new IssueQuery(new StateCriteria(stateSpec.getName(), IssueQueryLexer.Is));
							PageParameters params = ProjectIssueListPage.paramsOf(project, query.toString(), 0);
							Link<Void> stateLink = new BookmarkablePageLink<Void>("link", ProjectIssueListPage.class, params);
							stateLink.add(new Label("label", stats.getStateCount() + " " + stateSpec.getName()));
							stateLink.add(AttributeAppender.append("style", "color:" + stateSpec.getColor()));
							item.add(stateLink);
						}
						
					});
					
					fragment.add(issueInfoFrag);
				} else {
					fragment.add(new WebMarkupContainer("issueInfo").setVisible(false));
				}
				
				if (project.isCodeManagement()) {
					IModel<Integer> totalCountModel = new LoadableDetachableModel<Integer>() {

						@Override
						protected Integer load() {
							int totalCount = 0;
							for (ProjectBuildStats stats: buildStatsModel.getObject()) {
								if (stats.getProjectId().equals(projectId)) 
									totalCount += stats.getStatusCount();
							}
							return totalCount;
						}
						
					};
					Fragment buildInfoFrag = new Fragment("buildInfo", "buildInfoFrag", ProjectListPanel.this) {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(totalCountModel.getObject() != 0);
						}
						
					};
					buildInfoFrag.setDefaultModel(totalCountModel);

					PageParameters params = ProjectBuildsPage.paramsOf(project);
					Link<Void> buildsLink = new BookmarkablePageLink<Void>("builds", ProjectBuildsPage.class, params);
					buildsLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return totalCountModel.getObject() + " builds";
						}
						
					}));
					buildInfoFrag.add(buildsLink);
					
					buildInfoFrag.add(new ListView<ProjectBuildStats>("statuses", 
							new LoadableDetachableModel<List<ProjectBuildStats>>() {

						@Override
						protected List<ProjectBuildStats> load() {
							List<ProjectBuildStats> listOfBuildStats = new ArrayList<>();
							for (ProjectBuildStats stats: buildStatsModel.getObject()) {
								if (stats.getProjectId().equals(projectId)) 
									listOfBuildStats.add(stats);
							}
							return listOfBuildStats;
						}
						
					}) {

						@Override
						protected void populateItem(ListItem<ProjectBuildStats> item) {
							ProjectBuildStats stats = item.getModelObject();
							BuildQuery query = new BuildQuery(new StatusCriteria(stats.getBuildStatus()));
							PageParameters params = ProjectBuildsPage.paramsOf(rowModel.getObject(), query.toString(), 0);
							Link<Void> statusLink = new BookmarkablePageLink<Void>("link", ProjectBuildsPage.class, params);
							String statusName = stats.getBuildStatus().toString();
							statusLink.add(new Label("label", stats.getStatusCount() + " " + statusName));
							
							String cssClass;
							switch (stats.getBuildStatus()) {
							case SUCCESSFUL:
								cssClass = "link-success";
								break;
							case FAILED:
							case TIMED_OUT:
							case CANCELLED:
								cssClass = "link-danger";
								break;
							default:
								cssClass = "link-warning";
							}
							statusLink.add(AttributeAppender.append("class", cssClass));
							item.add(statusLink);
						}
						
					});
					
					fragment.add(buildInfoFrag);
				} else {
					fragment.add(new WebMarkupContainer("buildInfo").setVisible(false));
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
		} catch (ExplicitException e) {
			error(e.getMessage());
			return null;
		} catch (Exception e) {
			info("Performing fuzzy query");
			if (getParentProject() != null)
				return ProjectQuery.merge(baseQuery, new ProjectQuery(new NameCriteria("*" + queryString + "*")));
			else
				return ProjectQuery.merge(baseQuery, new ProjectQuery(new PathCriteria("**/*" + queryString + "*/**")));
		}
	}
	
	@Nullable
	protected Project getParentProject() {
		return null;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectCssResourceReference()));
	}
		
}
