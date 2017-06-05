package com.gitplex.server.web.page.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.GroupAuthorizationManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.datatable.DefaultDataTable;
import com.gitplex.server.web.component.datatable.SelectionColumn;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.projectchoice.AbstractProjectChoiceProvider;
import com.gitplex.server.web.component.projectchoice.ProjectChoiceResourceReference;
import com.gitplex.server.web.component.projectprivilege.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.component.select2.SelectToAddChoice;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class GroupAuthorizationsPage extends GroupPage {

	private String searchInput;
	
	private DataTable<GroupAuthorization, Void> authorizationsTable;
	
	private SelectionColumn<GroupAuthorization, Void> selectionColumn;
	
	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
	}

	private EntityCriteria<GroupAuthorization> getCriteria() {
		EntityCriteria<GroupAuthorization> criteria = EntityCriteria.of(GroupAuthorization.class);
		if (searchInput != null)
			criteria.createCriteria("project").add(Restrictions.ilike("name", searchInput, MatchMode.ANYWHERE)); 
		criteria.add(Restrictions.eq("group", getGroup()));
		return criteria;
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
				CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
				Collection<Long> authorizedProjectIds = new HashSet<>();
				for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
					if (authorization.getGroupId().equals(getGroup().getId()))
						authorizedProjectIds.add(authorization.getProjectId());
				}
				for (ProjectFacade project: cacheManager.getProjects().values()) {
					if (project.matchesQuery(term) && !authorizedProjectIds.contains(project.getId()))
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
				GroupAuthorization authorization = new GroupAuthorization();
				authorization.setGroup(getGroup());
				authorization.setProject(GitPlex.getInstance(ProjectManager.class).load(selection.getId()));
				GitPlex.getInstance(GroupAuthorizationManager.class).save(authorization);
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
				Collection<GroupAuthorization> authorizationsToDelete = new HashSet<>();
				for (IModel<GroupAuthorization> model: selectionColumn.getSelections()) {
					authorizationsToDelete.add(model.getObject());
				}
				GitPlex.getInstance(GroupAuthorizationManager.class).delete(authorizationsToDelete);
				target.add(authorizationsTable);
				selectionColumn.getSelections().clear();
				target.add(this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!selectionColumn.getSelections().isEmpty());
			}
			
		};
		deleteSelected.setOutputMarkupPlaceholderTag(true);
		add(deleteSelected);

		List<IColumn<GroupAuthorization, Void>> columns = new ArrayList<>();
		
		selectionColumn = new SelectionColumn<GroupAuthorization, Void>() {
			
			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				target.add(deleteSelected);
			}
			
		};
		columns.add(selectionColumn);
		
		columns.add(new AbstractColumn<GroupAuthorization, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroupAuthorization>> cellItem, String componentId,
					IModel<GroupAuthorization> rowModel) {
				GroupAuthorization authorization = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "projectFrag", GroupAuthorizationsPage.this);
				fragment.add(new BookmarkablePageLink<Void>("project", ProjectBlobPage.class, 
						ProjectBlobPage.paramsOf(authorization.getProject())) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getProject().getName());
					}

				});
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<GroupAuthorization, Void>(Model.of("Permission")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroupAuthorization>> cellItem, String componentId,
					IModel<GroupAuthorization> rowModel) {
				Fragment fragment = new Fragment(componentId, "privilegeFrag", GroupAuthorizationsPage.this);
				WebMarkupContainer dropdown = new DropdownLink("dropdown") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new PrivilegeSelectionPanel(id, rowModel.getObject().getPrivilege()) {
							
							@Override
							protected void onSelect(AjaxRequestTarget target, ProjectPrivilege privilege) {
								dropdown.close();
								GroupAuthorization authorization = rowModel.getObject();
								authorization.setPrivilege(privilege);
								GitPlex.getInstance(GroupAuthorizationManager.class).save(authorization);
								target.add(fragment);
							}
							
						};
					}
					
				};
				dropdown.add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return rowModel.getObject().getPrivilege().name();
					}
					
				}));
				fragment.add(dropdown);
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});
		
		SortableDataProvider<GroupAuthorization, Void> dataProvider = new SortableDataProvider<GroupAuthorization, Void>() {

			@Override
			public Iterator<? extends GroupAuthorization> iterator(long first, long count) {
				EntityCriteria<GroupAuthorization> criteria = getCriteria();
				criteria.addOrder(Order.desc("id"));
				return GitPlex.getInstance(GroupAuthorizationManager.class).findRange(criteria, (int)first, 
						(int)count).iterator();
			}

			@Override
			public long size() {
				return GitPlex.getInstance(GroupAuthorizationManager.class).count(getCriteria());
			}

			@Override
			public IModel<GroupAuthorization> model(GroupAuthorization object) {
				Long id = object.getId();
				return new LoadableDetachableModel<GroupAuthorization>() {

					@Override
					protected GroupAuthorization load() {
						return GitPlex.getInstance(GroupAuthorizationManager.class).load(id);
					}
					
				};
			}
		};
		
		add(authorizationsTable = new DefaultDataTable<GroupAuthorization, Void>("authorizations", columns, 
				dataProvider, WebConstants.PAGE_SIZE));
	}
	
}
