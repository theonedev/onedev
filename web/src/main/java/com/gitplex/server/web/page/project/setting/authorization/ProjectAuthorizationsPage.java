package com.gitplex.server.web.page.project.setting.authorization;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.model.User;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.datatable.DefaultDataTable;
import com.gitplex.server.web.component.datatable.SelectionColumn;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.UserLink;
import com.gitplex.server.web.component.projectprivilege.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.server.web.component.projectprivilege.privilegesource.PrivilegeSourcePanel;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.component.select2.SelectToAddChoice;
import com.gitplex.server.web.component.userchoice.AbstractUserChoiceProvider;
import com.gitplex.server.web.component.userchoice.UserChoiceResourceReference;
import com.gitplex.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class ProjectAuthorizationsPage extends ProjectSettingPage {

	private String searchInput;
	
	private DataTable<User, Void> authorizationsTable;
	
	private SelectionColumn<User, Void> selectionColumn;
	
	private IModel<Map<User, ProjectPrivilege>> userAuthorizationsModel = 
			new LoadableDetachableModel<Map<User, ProjectPrivilege>>() {

		@Override
		protected Map<User, ProjectPrivilege> load() {
			Map<User, ProjectPrivilege> userAuthorizations = new HashMap<>();
			Collection<User> usersAuthorizedFromOtherSources = new HashSet<>();
			UserManager userManager = GitPlex.getInstance(UserManager.class);
			userAuthorizations.put(userManager.getRoot(), ProjectPrivilege.ADMIN);
			usersAuthorizedFromOtherSources.add(userManager.getRoot());
			
			EntityCriteria<Group> adminGroupCriteria = EntityCriteria.of(Group.class);
			adminGroupCriteria.add(Restrictions.eq("administrator", true));
			for (Group group: GitPlex.getInstance(GroupManager.class).findAll(adminGroupCriteria)) {
				for (User user: group.getMembers()) {
					userAuthorizations.put(user, ProjectPrivilege.ADMIN);
					usersAuthorizedFromOtherSources.add(user);
				}
			}
			for (GroupAuthorization authorization: getProject().getGroupAuthorizations()) {
				for (User user: authorization.getGroup().getMembers()) {
					ProjectPrivilege privilege = userAuthorizations.get(user);
					if (privilege == null || authorization.getPrivilege().implies(privilege))
						userAuthorizations.put(user, authorization.getPrivilege());
					usersAuthorizedFromOtherSources.add(user);
				}
			}
			for (UserAuthorization authorization: getProject().getUserAuthorizations()) {
				ProjectPrivilege privilege = userAuthorizations.get(authorization.getUser());
				if (privilege == null || authorization.getPrivilege().implies(privilege))
					userAuthorizations.put(authorization.getUser(), authorization.getPrivilege());
			}
			if (getProject().isPublicRead()) {
				for (User user: userManager.findAll()) {
					ProjectPrivilege privilege = userAuthorizations.get(user);
					if (privilege == null || ProjectPrivilege.READ.implies(privilege))
						userAuthorizations.put(user, ProjectPrivilege.READ);
					usersAuthorizedFromOtherSources.add(user);
				}
			}
			
			Map<User, UserAuthorization> projectAuthorizationMap = getProjectAuthorizationMap();
			List<User> users = new ArrayList<>(userAuthorizations.keySet());
			Collections.sort(users, new Comparator<User>() {

				@Override
				public int compare(User o1, User o2) {
					if (usersAuthorizedFromOtherSources.contains(o1)) {
						if (usersAuthorizedFromOtherSources.contains(o2)) {
							return o1.getDisplayName().compareTo(o2.getDisplayName());
						} else {
							return -1;
						}
					} else {
						if (usersAuthorizedFromOtherSources.contains(o2)) {
							return 1;
						} else {
							return projectAuthorizationMap.get(o2).getId()
									.compareTo(projectAuthorizationMap.get(o1).getId());
						}
					}
				}
				
			});	
			
			Map<User, ProjectPrivilege> sortedUserAuthorizations = new LinkedHashMap<>();
			for (User user: users)
				sortedUserAuthorizations.put(user, userAuthorizations.get(user));
			
			return sortedUserAuthorizations;
		}
		
	};
	
	public ProjectAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onDetach() {
		userAuthorizationsModel.detach();
		super.onDetach();
	}
	
	private Map<User, UserAuthorization> getProjectAuthorizationMap() {
		Map<User, UserAuthorization> projectAuthorizationMap = new HashMap<>();
		for (UserAuthorization authorization: getProject().getUserAuthorizations()) {
			projectAuthorizationMap.put(authorization.getUser(), authorization);
		}
		return projectAuthorizationMap;
	}

	private Map<User, ProjectPrivilege> getUserAuthorizations() {
		return userAuthorizationsModel.getObject();
	}
	
	private boolean isPrivilegeFromOtherSources(User user) {
		UserAuthorization projectAuthorization = getProjectAuthorizationMap().get(user);
		ProjectPrivilege privilege = getUserAuthorizations().get(user);
		return projectAuthorization == null || !projectAuthorization.getPrivilege().implies(privilege);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterUsers", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(authorizationsTable);
			}
			
		});
		
		add(new SelectToAddChoice<User>("addNew", new AbstractUserChoiceProvider() {

			@Override
			public void query(String term, int page, Response<User> response) {
				List<User> notAuthorized = new ArrayList<>();
				
				for (User user: GitPlex.getInstance(UserManager.class).findAll()) {
					if (user.matches(searchInput) && !getUserAuthorizations().containsKey(user))
						notAuthorized.add(user);
				}
				Collections.sort(notAuthorized);
				Collections.reverse(notAuthorized);
				new ResponseFiller<User>(response).fill(notAuthorized, page, WebConstants.PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add user...");
				getSettings().setFormatResult("gitplex.server.userChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.server.userChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.server.userChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, User selection) {
				UserAuthorization authorization = new UserAuthorization();
				authorization.setProject(getProject());
				authorization.setUser(selection);
				GitPlex.getInstance(UserAuthorizationManager.class).save(authorization);
				target.add(authorizationsTable);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
			}
			
		});			
		
		AjaxLink<Void> deleteSelected = new AjaxLink<Void>("deleteSelected") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Collection<UserAuthorization> authorizationsToDelete = new HashSet<>();
				Map<User, UserAuthorization> projectAuthorizationMap = getProjectAuthorizationMap();
				for (IModel<User> model: selectionColumn.getSelections()) {
					UserAuthorization authorization = projectAuthorizationMap.get(model.getObject());
					if (authorization != null)
						authorizationsToDelete.add(authorization);
				}
				GitPlex.getInstance(UserAuthorizationManager.class).delete(authorizationsToDelete);
				getProject().getUserAuthorizations().removeAll(authorizationsToDelete);
				target.add(authorizationsTable);
				selectionColumn.getSelections().clear();
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				boolean hasLocalPrivileges = false;
				for (IModel<User> model: selectionColumn.getSelections()) {
					User user = model.getObject();
					if (!isPrivilegeFromOtherSources(user)) {
						hasLocalPrivileges = true;
						break;
					}
				}
				setVisible(hasLocalPrivileges);
			}
			
		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<User, Void>> columns = new ArrayList<>();
		
		selectionColumn = new SelectionColumn<User, Void>() {
			
			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				target.add(deleteSelected);
			}
			
		};
		columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<User, Void>(Model.of("User")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, 
					IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "userFrag", ProjectAuthorizationsPage.this);
				fragment.add(new AvatarLink("avatarLink", rowModel.getObject()));
				fragment.add(new UserLink("nameLink", rowModel.getObject()));
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Permission")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "privilegeFrag", ProjectAuthorizationsPage.this) {

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
						ProjectPrivilege privilege = getUserAuthorizations().get(rowModel.getObject());
						return new PrivilegeSelectionPanel(id, privilege) {
							
							@Override
							protected void onSelect(AjaxRequestTarget target, ProjectPrivilege privilege) {
								dropdown.close();
								User user = rowModel.getObject();
								UserAuthorization userAuthorization = getProjectAuthorizationMap().get(user);
								if (userAuthorization == null) {
									userAuthorization = new UserAuthorization();
									userAuthorization.setProject(getProject());
									userAuthorization.setUser(user);
									getProject().getUserAuthorizations().add(userAuthorization);
								}
								userAuthorization.setPrivilege(privilege);
								if (getUserAuthorizations().get(user) != privilege) {
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
						return getUserAuthorizations().get(rowModel.getObject()).name();
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
						User user = rowModel.getObject();
						return new PrivilegeSourcePanel(id, user, getProject(), getUserAuthorizations().get(user));
					}
					
				});
				
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<User, Void> dataProvider = new SortableDataProvider<User, Void>() {

			@Override
			public Iterator<? extends User> iterator(long first, long count) {
				List<User> users = new ArrayList<>(getUserAuthorizations().keySet());
				if (first + count <= users.size())
					return users.subList((int)first, (int)(first+count)).iterator();
				else
					return users.subList((int)first, users.size()).iterator();
			}

			@Override
			public long size() {
				return getUserAuthorizations().size();
			}

			@Override
			public IModel<User> model(User object) {
				Long userId = object.getId();
				return new LoadableDetachableModel<User>() {

					@Override
					protected User load() {
						return GitPlex.getInstance(UserManager.class).load(userId);
					}
					
				};
			}
		};
		
		add(authorizationsTable = new DefaultDataTable<User, Void>("authorizations", columns, 
				dataProvider, WebConstants.PAGE_SIZE));
	}
}
