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
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.permission.SystemAdministration;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
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
	
	private DataTable<Long, Void> authorizationsTable;
	
	private SelectionColumn<Long, Void> selectionColumn;
	
	private IModel<Map<Long, UserAuthorizationFacade>> explicitProjectAuthorizationsModel = 
			new LoadableDetachableModel<Map<Long, UserAuthorizationFacade>>() {

		@Override
		protected Map<Long, UserAuthorizationFacade> load() {
			Map<Long, UserAuthorizationFacade> authorizations = new HashMap<>();
			for (UserAuthorizationFacade authorization: 
					GitPlex.getInstance(CacheManager.class).getUserAuthorizations().values()) {
				if (authorization.getUserId().equals(getUser().getId()))
					authorizations.put(authorization.getProjectId(), authorization);
			}
			return authorizations;
		}
		
	};
	private IModel<Map<Long, ProjectPrivilege>> projectAuthorizationsModel = 
			new LoadableDetachableModel<Map<Long, ProjectPrivilege>>() {

		@Override
		protected Map<Long, ProjectPrivilege> load() {
			Map<Long, ProjectPrivilege> projectAuthorizations = new HashMap<>();
			Collection<Long> projectsAuthorizedFromOtherSources = new HashSet<>();
			CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
			
			if (getUser().asSubject().isPermitted(new SystemAdministration())) {
				for (ProjectFacade project: cacheManager.getProjects().values()) {
					projectAuthorizations.put(project.getId(), ProjectPrivilege.ADMIN);
					projectsAuthorizedFromOtherSources.add(project.getId());
				}
			} else {
				for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) { 
					if (authorization.getUserId().equals(getUser().getId()))
						projectAuthorizations.put(authorization.getProjectId(), authorization.getPrivilege());
				}
				Collection<Long> groupIds = new HashSet<>();
				for (MembershipFacade membership: cacheManager.getMemberships().values()) {
					if (membership.getUserId().equals(getUser().getId()))
						groupIds.add(membership.getGroupId());
				}
				for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
					if (groupIds.contains(authorization.getGroupId())) {
						ProjectPrivilege privilege = projectAuthorizations.get(authorization.getProjectId());
						if (privilege == null || authorization.getPrivilege().implies(privilege))
							projectAuthorizations.put(authorization.getProjectId(), authorization.getPrivilege());
						projectsAuthorizedFromOtherSources.add(authorization.getProjectId());
					}
				}
				for (ProjectFacade project: cacheManager.getProjects().values()) {
					if (project.isPublicRead()) {
						if (!projectAuthorizations.containsKey(project.getId()))
							projectAuthorizations.put(project.getId(), ProjectPrivilege.READ);
						projectsAuthorizedFromOtherSources.add(project.getId());
					}
				}
			}

			Map<Long, UserAuthorizationFacade> explicitProjectAuthorizations = getExplicitProjectAuthorizations();
			List<Long> projectIds = new ArrayList<>(projectAuthorizations.keySet());
			Collections.sort(projectIds, new Comparator<Long>() {

				@Override
				public int compare(Long o1, Long o2) {
					if (projectsAuthorizedFromOtherSources.contains(o1)) {
						if (projectsAuthorizedFromOtherSources.contains(o2)) {
							return o1.compareTo(o2);
						} else {
							return 1;
						}
					} else {
						if (projectsAuthorizedFromOtherSources.contains(o2)) {
							return -1;
						} else {
							return explicitProjectAuthorizations.get(o2).getId()
									.compareTo(explicitProjectAuthorizations.get(o1).getId());
						}
					}
				}
				
			});	
			
			Map<Long, ProjectPrivilege> sortedProjectAuthorizations = new LinkedHashMap<>();
			for (Long projectId: projectIds)
				sortedProjectAuthorizations.put(projectId, projectAuthorizations.get(projectId));
			
			return sortedProjectAuthorizations;
		}
		
	};
	
	public UserAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		explicitProjectAuthorizationsModel.detach();
		projectAuthorizationsModel.detach();
		super.onDetach();
	}
	
	private Map<Long, UserAuthorizationFacade> getExplicitProjectAuthorizations() {
		return explicitProjectAuthorizationsModel.getObject();
	}

	private Map<Long, ProjectPrivilege> getProjectAuthorizations() {
		return projectAuthorizationsModel.getObject();
	}
	
	private boolean isPrivilegeFromOtherSources(Long projectId) {
		UserAuthorizationFacade authorization = getExplicitProjectAuthorizations().get(projectId);
		ProjectPrivilege privilege = getProjectAuthorizations().get(projectId);
		return authorization == null || !authorization.getPrivilege().implies(privilege);
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
		
		add(new SelectToAddChoice<ProjectFacade>("addNew", new AbstractProjectChoiceProvider() {

			@Override
			public void query(String term, int page, Response<ProjectFacade> response) {
				List<ProjectFacade> notAuthorizedProjects = new ArrayList<>();
				
				for (ProjectFacade project: GitPlex.getInstance(CacheManager.class).getProjects().values()) {
					if (project.matchesQuery(term) && !getProjectAuthorizations().containsKey(project.getId()))
						notAuthorizedProjects.add(project);
				}
				Collections.sort(notAuthorizedProjects);
				Collections.reverse(notAuthorizedProjects);
				new ResponseFiller<ProjectFacade>(response).fill(notAuthorizedProjects, page, WebConstants.PAGE_SIZE);
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
			protected void onSelect(AjaxRequestTarget target, ProjectFacade selection) {
				UserAuthorization authorization = new UserAuthorization();
				authorization.setUser(getUser());
				authorization.setProject(GitPlex.getInstance(ProjectManager.class).load(selection.getId()));
				GitPlex.getInstance(UserAuthorizationManager.class).save(authorization);
				target.add(authorizationsTable);
				Session.get().success("Project added");
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
				Map<Long, UserAuthorizationFacade> explicitProjectAuthorizations = getExplicitProjectAuthorizations();
				UserAuthorizationManager userAuthorizationManager = GitPlex.getInstance(UserAuthorizationManager.class);
				for (IModel<Long> model: selectionColumn.getSelections()) {
					UserAuthorizationFacade authorization = explicitProjectAuthorizations.get(model.getObject());
					if (authorization != null) {
						authorizationsToDelete.add(userAuthorizationManager.load(authorization.getId()));
						explicitProjectAuthorizations.remove(model.getObject());
					}
				}
				userAuthorizationManager.delete(authorizationsToDelete);
				target.add(authorizationsTable);
				selectionColumn.getSelections().clear();
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasLocalPrivileges = false;
				for (IModel<Long> model: selectionColumn.getSelections()) {
					if (!isPrivilegeFromOtherSources(model.getObject())) {
						hasLocalPrivileges = true;
						break;
					}
				}
				setVisible(hasLocalPrivileges);
			}
			
		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<Long, Void>> columns = new ArrayList<>();
		
		selectionColumn = new SelectionColumn<Long, Void>() {
			
			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				target.add(deleteSelected);
			}
			
		};
		columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<Long, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<Long>> cellItem, String componentId, 
					IModel<Long> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", UserAuthorizationsPage.this);
				Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject());
				String projectName = project.getName();
				fragment.add(new BookmarkablePageLink<Void>("project", ProjectBlobPage.class, 
						ProjectBlobPage.paramsOf(project)) {

					@Override
					public IModel<?> getBody() {
						return Model.of(projectName);
					}

				});
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Long, Void>(Model.of("Permission")) {

			@Override
			public void populateItem(Item<ICellPopulator<Long>> cellItem, String componentId,
					IModel<Long> rowModel) {
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
								Long projectId = rowModel.getObject();
								
								Map<Long, UserAuthorizationFacade> explicitProjectAuthorizations = 
										getExplicitProjectAuthorizations();
								UserAuthorizationFacade userAuthorizationFacade = 
										explicitProjectAuthorizations.get(projectId);
								UserAuthorizationManager userAuthorizationManager = 
										GitPlex.getInstance(UserAuthorizationManager.class);
								UserAuthorization userAuthorization;
								if (userAuthorizationFacade == null) {
									userAuthorization = new UserAuthorization();
									userAuthorization.setUser(getUser());
									userAuthorization.setProject(
											GitPlex.getInstance(ProjectManager.class).load(projectId));
								} else {
									userAuthorization = userAuthorizationManager.load(userAuthorizationFacade.getId()); 
								}
								userAuthorization.setPrivilege(privilege);
								userAuthorizationManager.save(userAuthorization);
								explicitProjectAuthorizations.put(projectId, userAuthorization.getFacade());
								
								target.add(fragment);
								
								if (getProjectAuthorizations().get(projectId) != privilege) {
									Session.get().warn("Specified permission is not taking effect as a higher "
											+ "permission is granted from other sources");
								}
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
						Project project = GitPlex.getInstance(ProjectManager.class).load(rowModel.getObject());
						return new PrivilegeSourcePanel(id, getUser(), project, 
								getProjectAuthorizations().get(project.getId()));
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<Long, Void> dataProvider = new SortableDataProvider<Long, Void>() {

			private List<Long> getProjectIds() {
				List<Long> projectIds = new ArrayList<>();
				CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
				for (Long projectId: getProjectAuthorizations().keySet()) {
					if (cacheManager.getProject(projectId).matchesQuery(searchInput))
						projectIds.add(projectId);
				}
				return projectIds;
			}
			
			@Override
			public Iterator<? extends Long> iterator(long first, long count) {
				List<Long> projectIds = getProjectIds();
				if (first + count <= projectIds.size())
					return projectIds.subList((int)first, (int)(first+count)).iterator();
				else
					return projectIds.subList((int)first, projectIds.size()).iterator();
			}

			@Override
			public long size() {
				return getProjectIds().size();
			}

			@Override
			public IModel<Long> model(Long object) {
				return Model.of(object);
			}
		};
		
		add(authorizationsTable = new DefaultDataTable<Long, Void>("authorizations", columns, 
				dataProvider, WebConstants.PAGE_SIZE));
	}
}
