package io.onedev.server.web.page.admin.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import io.onedev.commons.utils.WordUtils;
import io.onedev.commons.utils.matchscore.MatchScoreProvider;
import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.GroupAuthorizationManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.facade.GroupAuthorizationFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.choice.AbstractProjectChoiceProvider;
import io.onedev.server.web.component.project.choice.ProjectChoiceResourceReference;
import io.onedev.server.web.component.project.privilege.selection.PrivilegeSelectionPanel;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class GroupAuthorizationsPage extends GroupPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private DataTable<GroupAuthorization, Void> authorizationsTable;
	
	private SelectionColumn<GroupAuthorization, Void> selectionColumn;
	
	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}

	private EntityCriteria<GroupAuthorization> getCriteria() {
		EntityCriteria<GroupAuthorization> criteria = EntityCriteria.of(GroupAuthorization.class);
		if (query != null)
			criteria.createCriteria("project").add(Restrictions.ilike("name", query, MatchMode.ANYWHERE)); 
		criteria.add(Restrictions.eq("group", getGroup()));
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("filterProjects", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(authorizationsTable);
			}
			
		});
		
		add(new SelectToAddChoice<ProjectFacade>("addNew", new AbstractProjectChoiceProvider() {

			@Override
			public void query(String term, int page, Response<ProjectFacade> response) {
				List<ProjectFacade> notAuthorizedProjects = new ArrayList<>();
				CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
				Collection<Long> authorizedProjectIds = new HashSet<>();
				for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
					if (authorization.getGroupId().equals(getGroup().getId()))
						authorizedProjectIds.add(authorization.getProjectId());
				}
				for (ProjectFacade project: cacheManager.getProjects().values()) {
					if (!authorizedProjectIds.contains(project.getId()))
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
				GroupAuthorization authorization = new GroupAuthorization();
				authorization.setGroup(getGroup());
				authorization.setProject(OneDev.getInstance(ProjectManager.class).load(selection.getId()));
				OneDev.getInstance(GroupAuthorizationManager.class).save(authorization);
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
				OneDev.getInstance(GroupAuthorizationManager.class).delete(authorizationsToDelete);
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
								OneDev.getInstance(GroupAuthorizationManager.class).save(authorization);
								target.add(fragment);
							}
							
						};
					}
					
				};
				dropdown.add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return WordUtils.toWords(rowModel.getObject().getPrivilege().name());
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
				return OneDev.getInstance(GroupAuthorizationManager.class).query(criteria, (int)first, 
						(int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(GroupAuthorizationManager.class).count(getCriteria());
			}

			@Override
			public IModel<GroupAuthorization> model(GroupAuthorization object) {
				Long id = object.getId();
				return new LoadableDetachableModel<GroupAuthorization>() {

					@Override
					protected GroupAuthorization load() {
						return OneDev.getInstance(GroupAuthorizationManager.class).load(id);
					}
					
				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getGroup());
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				if (query != null)
					params.add(PARAM_QUERY, query);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		add(authorizationsTable = new HistoryAwareDataTable<GroupAuthorization, Void>("authorizations", columns, 
				dataProvider, WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
}
