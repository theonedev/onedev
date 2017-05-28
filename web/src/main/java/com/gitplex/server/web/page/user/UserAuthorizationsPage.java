package com.gitplex.server.web.page.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.model.Membership;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.permission.SystemAdministration;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.datatable.DefaultDataTable;
import com.gitplex.server.web.component.datatable.SelectionColumn;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.projectchoice.AbstractProjectChoiceProvider;
import com.gitplex.server.web.component.projectchoice.ProjectChoiceResourceReference;
import com.gitplex.server.web.component.projectprivilege.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.server.web.component.projectprivilege.privilegesource.PrivilegeSourcePanel;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.component.select2.SelectToAddChoice;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class UserAuthorizationsPage extends UserPage {

	private String searchInput;
	
	private DataTable<Project, Void> authorizationsTable;
	
	private SelectionColumn<Project, Void> selectionColumn;
	
	private IModel<Map<Project, ProjectPrivilege>> projectAuthorizationsModel = 
			new LoadableDetachableModel<Map<Project, ProjectPrivilege>>() {

		@Override
		protected Map<Project, ProjectPrivilege> load() {
			Map<Project, ProjectPrivilege> projectAuthorizations = new HashMap<>();
			Collection<Project> projectsAuthorizedFromOtherSources = new HashSet<>();
			if (getUser().asSubject().isPermitted(new SystemAdministration())) {
				for (Project project: GitPlex.getInstance(ProjectManager.class).findAll()) {
					projectAuthorizations.put(project, ProjectPrivilege.ADMIN);
					projectsAuthorizedFromOtherSources.add(project);
				}
			} else {
				for (UserAuthorization authorization: getUser().getAuthorizations()) 
					projectAuthorizations.put(authorization.getProject(), authorization.getPrivilege());
				for (Membership membership: getUser().getMemberships()) {
					for (GroupAuthorization authorization: membership.getGroup().getAuthorizations()) {
						ProjectPrivilege existingPrivilege = projectAuthorizations.get(authorization.getProject());
						if (existingPrivilege == null || authorization.getPrivilege().implies(existingPrivilege))
							projectAuthorizations.put(authorization.getProject(), authorization.getPrivilege());
						projectsAuthorizedFromOtherSources.add(authorization.getProject());
					}
				}
				for (Project project: GitPlex.getInstance(ProjectManager.class).findAll()) {
					if (project.isPublicRead()) {
						if (!projectAuthorizations.containsKey(project))
							projectAuthorizations.put(project, ProjectPrivilege.READ);
						projectsAuthorizedFromOtherSources.add(project);
					}
				}
			}

			Map<Project, UserAuthorization> userAuthorizationMap = getUserAuthorizationMap();
			List<Project> projects = new ArrayList<>(projectAuthorizations.keySet());
			Collections.sort(projects, new Comparator<Project>() {

				@Override
				public int compare(Project o1, Project o2) {
					if (projectsAuthorizedFromOtherSources.contains(o1)) {
						if (projectsAuthorizedFromOtherSources.contains(o2)) {
							return o1.getName().compareTo(o2.getName());
						} else {
							return -1;
						}
					} else {
						if (projectsAuthorizedFromOtherSources.contains(o2)) {
							return 1;
						} else {
							return userAuthorizationMap.get(o2).getId().compareTo(userAuthorizationMap.get(o1).getId());
						}
					}
				}
				
			});	
			
			Map<Project, ProjectPrivilege> sortedProjectAuthorizations = new LinkedHashMap<>();
			for (Project project: projects)
				sortedProjectAuthorizations.put(project, projectAuthorizations.get(project));
			
			return sortedProjectAuthorizations;
		}
		
	};
	
	public UserAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		projectAuthorizationsModel.detach();
		super.onDetach();
	}
	
	private Map<Project, UserAuthorization> getUserAuthorizationMap() {
		Map<Project, UserAuthorization> userAuthorizationMap = new HashMap<>();
		for (UserAuthorization authorization: getUser().getAuthorizations()) {
			userAuthorizationMap.put(authorization.getProject(), authorization);
		}
		return userAuthorizationMap;
	}

	private Map<Project, ProjectPrivilege> getProjectAuthorizations() {
		return projectAuthorizationsModel.getObject();
	}
	
	private boolean isPrivilegeFromOtherSources(Project project) {
		UserAuthorization userAuthorization = getUserAuthorizationMap().get(project);
		ProjectPrivilege privilege = getProjectAuthorizations().get(project);
		return userAuthorization == null || !userAuthorization.getPrivilege().implies(privilege);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterProjects", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(authorizationsTable);
			}
			
		});
		
		add(new SelectToAddChoice<Project>("addNew", new AbstractProjectChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Project> response) {
				List<Project> notAuthorized = new ArrayList<>();
				
				for (Project project: GitPlex.getInstance(ProjectManager.class).findAll()) {
					if (project.matches(searchInput) && !getProjectAuthorizations().containsKey(project))
						notAuthorized.add(project);
				}
				Collections.sort(notAuthorized);
				Collections.reverse(notAuthorized);
				new ResponseFiller<Project>(response).fill(notAuthorized, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add project...");
				getSettings().setFormatResult("gitplex.server.projectChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.server.projectChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.server.projectChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Project selection) {
				UserAuthorization authorization = new UserAuthorization();
				authorization.setUser(getUser());
				authorization.setProject(selection);
				GitPlex.getInstance(UserAuthorizationManager.class).save(authorization);
				target.add(authorizationsTable);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new ProjectChoiceResourceReference()));
			}
			
		});			
		
		AjaxLink<Void> deleteSelected = new AjaxLink<Void>("deleteSelected") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Collection<UserAuthorization> authorizationsToDelete = new HashSet<>();
				Map<Project, UserAuthorization> userAuthorizationMap = getUserAuthorizationMap();
				for (IModel<Project> model: selectionColumn.getSelections()) {
					UserAuthorization authorization = userAuthorizationMap.get(model.getObject());
					if (authorization != null)
						authorizationsToDelete.add(authorization);
				}
				GitPlex.getInstance(UserAuthorizationManager.class).delete(authorizationsToDelete);
				getUser().getAuthorizations().removeAll(authorizationsToDelete);
				target.add(authorizationsTable);
				selectionColumn.getSelections().clear();
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasLocalPrivileges = false;
				for (IModel<Project> model: selectionColumn.getSelections()) {
					Project project = model.getObject();
					if (!isPrivilegeFromOtherSources(project)) {
						hasLocalPrivileges = true;
						break;
					}
				}
				setVisible(hasLocalPrivileges);
			}
			
		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<Project, Void>> columns = new ArrayList<>();
		
		selectionColumn = new SelectionColumn<Project, Void>() {
			
			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				target.add(deleteSelected);
			}
			
		};
		columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", UserAuthorizationsPage.this);
				fragment.add(new BookmarkablePageLink<Void>("project", ProjectBlobPage.class, 
						ProjectBlobPage.paramsOf(rowModel.getObject())) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getName());
					}

				});
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Permission")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId,
					IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "privilegeFrag", UserAuthorizationsPage.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);

						String script = String.format(""
								+ "$('#%s').closest('tr').find('.row-selector input').prop('disabled', %b);", 
								getMarkupId(), isPrivilegeFromOtherSources(rowModel.getObject()));
						
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				WebMarkupContainer dropdown = new DropdownLink("dropdown") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						ProjectPrivilege privilege = getProjectAuthorizations().get(rowModel.getObject());
						return new PrivilegeSelectionPanel(id, privilege) {
							
							@Override
							protected void onSelect(AjaxRequestTarget target, ProjectPrivilege privilege) {
								dropdown.close();
								Project project = rowModel.getObject();
								UserAuthorization userAuthorization = getUserAuthorizationMap().get(project);
								if (userAuthorization == null) {
									userAuthorization = new UserAuthorization();
									userAuthorization.setUser(getUser());
									userAuthorization.setProject(project);
									getUser().getAuthorizations().add(userAuthorization);
								}
								userAuthorization.setPrivilege(privilege);
								if (getProjectAuthorizations().get(project) != privilege) {
									Session.get().warn("Specified permission is not taking effect as a higher permission is granted from other sources");
								}
								GitPlex.getInstance(UserAuthorizationManager.class).save(userAuthorization);
								target.add(fragment);
							}
							
						};
					}
					
				};
				dropdown.add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return getProjectAuthorizations().get(rowModel.getObject()).name();
					}
					
				}));
				fragment.add(dropdown);
				
				fragment.add(new DropdownLink("otherSources") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(isPrivilegeFromOtherSources(rowModel.getObject()));
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						Project project = rowModel.getObject();
						return new PrivilegeSourcePanel(id, getUser(), project, 
								getProjectAuthorizations().get(project));
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<Project, Void> dataProvider = new SortableDataProvider<Project, Void>() {

			@Override
			public Iterator<? extends Project> iterator(long first, long count) {
				List<Project> projects = new ArrayList<>(getProjectAuthorizations().keySet());
				if (first + count <= projects.size())
					return projects.subList((int)first, (int)(first+count)).iterator();
				else
					return projects.subList((int)first, projects.size()).iterator();
			}

			@Override
			public long size() {
				return getProjectAuthorizations().size();
			}

			@Override
			public IModel<Project> model(Project object) {
				Long projectId = object.getId();
				return new LoadableDetachableModel<Project>() {

					@Override
					protected Project load() {
						return GitPlex.getInstance(ProjectManager.class).load(projectId);
					}
					
				};
			}
		};
		
		add(authorizationsTable = new DefaultDataTable<Project, Void>("authorizations", columns, 
				dataProvider, WebConstants.PAGE_SIZE));
	}
}
