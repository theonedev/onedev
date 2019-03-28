package io.onedev.server.web.page.admin.user;

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

import io.onedev.commons.utils.WordUtils;
import io.onedev.commons.utils.matchscore.MatchScoreProvider;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.util.facade.GroupAuthorizationFacade;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserAuthorizationFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.choice.AbstractProjectChoiceProvider;
import io.onedev.server.web.component.project.choice.ProjectChoiceResourceReference;
import io.onedev.server.web.component.project.privilege.selection.PrivilegeSelectionPanel;
import io.onedev.server.web.component.project.privilege.source.PrivilegeSourcePanel;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserAuthorizationsPage extends UserPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private String searchInput;
	
	private DataTable<Long, Void> authorizationsTable;
	
	private SelectionColumn<Long, Void> selectionColumn;
	
	private IModel<Map<Long, UserAuthorizationFacade>> explicitProjectAuthorizationsModel = 
			new LoadableDetachableModel<Map<Long, UserAuthorizationFacade>>() {

		@Override
		protected Map<Long, UserAuthorizationFacade> load() {
			Map<Long, UserAuthorizationFacade> authorizations = new HashMap<>();
			for (UserAuthorizationFacade authorization: 
					OneDev.getInstance(CacheManager.class).getUserAuthorizations().values()) {
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
			CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
			
			if (getUser().asSubject().isPermitted(new SystemAdministration())) {
				for (ProjectFacade project: cacheManager.getProjects().values()) {
					projectAuthorizations.put(project.getId(), ProjectPrivilege.ADMINISTRATION);
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
					ProjectPrivilege privilege = projectAuthorizations.get(project.getId());
					if (project.getDefaultPrivilege() != null) {
						if (privilege == null || project.getDefaultPrivilege().getProjectPrivilege().implies(privilege))
							projectAuthorizations.put(project.getId(), project.getDefaultPrivilege().getProjectPrivilege());
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
				
				for (ProjectFacade project: OneDev.getInstance(CacheManager.class).getProjects().values()) {
					if (!getProjectAuthorizations().containsKey(project.getId()))
						notAuthorizedProjects.add(project);
				}
				Collections.sort(notAuthorizedProjects);
				Collections.reverse(notAuthorizedProjects);
				
				MatchScoreProvider<ProjectFacade> matchScoreProvider = new MatchScoreProvider<ProjectFacade>() {

					@Override
					public double getMatchScore(ProjectFacade object) {
						return MatchScoreUtils.getMatchScore(object.getName(), term);
					}
					
				};

				notAuthorizedProjects = MatchScoreUtils.filterAndSort(notAuthorizedProjects, matchScoreProvider);
				
				new ResponseFiller<ProjectFacade>(response).fill(notAuthorizedProjects, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Authorize project...");
				getSettings().setFormatResult("onedev.server.projectChoiceFormatter.formatResult");
				getSettings().setFormatSelection("onedev.server.projectChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("onedev.server.projectChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, ProjectFacade selection) {
				UserAuthorization authorization = new UserAuthorization();
				authorization.setUser(getUser());
				authorization.setProject(OneDev.getInstance(ProjectManager.class).load(selection.getId()));
				OneDev.getInstance(UserAuthorizationManager.class).save(authorization);
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
				UserAuthorizationManager userAuthorizationManager = OneDev.getInstance(UserAuthorizationManager.class);
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
				Project project = OneDev.getInstance(ProjectManager.class).load(rowModel.getObject());
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
										OneDev.getInstance(UserAuthorizationManager.class);
								UserAuthorization userAuthorization;
								if (userAuthorizationFacade == null) {
									userAuthorization = new UserAuthorization();
									userAuthorization.setUser(getUser());
									userAuthorization.setProject(
											OneDev.getInstance(ProjectManager.class).load(projectId));
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
						return WordUtils.toWords(getProjectAuthorizations().get(rowModel.getObject()).name());
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
						Project project = OneDev.getInstance(ProjectManager.class).load(rowModel.getObject());
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
				List<Long> projectIds = new ArrayList<>(getProjectAuthorizations().keySet());
				CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
				
				return MatchScoreUtils.filterAndSort(projectIds, new MatchScoreProvider<Long>() {

					@Override
					public double getMatchScore(Long object) {
						return MatchScoreUtils.getMatchScore(cacheManager.getProject(object).getName(), searchInput);
					}
					
				});
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

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getUser());
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(authorizationsTable = new HistoryAwareDataTable<Long, Void>("authorizations", columns, 
				dataProvider, WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
}
