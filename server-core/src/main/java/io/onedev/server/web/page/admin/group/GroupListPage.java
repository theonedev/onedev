package io.onedev.server.web.page.admin.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class GroupListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private TextField<String> searchField;
	
	private DataTable<Group, Void> groupsTable;
	
	private String query;
	
	private boolean typing;

	public GroupListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}
	
	private EntityCriteria<Group> getCriteria() {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (query != null) 
			criteria.add(Restrictions.ilike("name", query, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}
	
	@Override
	protected void onBeforeRender() {
		typing = false;
		super.onBeforeRender();
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(searchField);
		target.add(groupsTable);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchField = new TextField<String>("filterGroups", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				
				String url = RequestCycle.get().urlFor(GroupListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				groupsTable.setCurrentPage(0);
				target.add(groupsTable);
				
				typing = true;
			}
			
		}));
		
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
			}

		});
		
		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewGroupPage.class);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		List<IColumn<Group, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", GroupListPage.this);
				Group group = rowModel.getObject();
				WebMarkupContainer link = new ActionablePageLink<Void>("link", 
						GroupProfilePage.class, GroupProfilePage.paramsOf(group)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								GroupListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Group.class, redirectUrlAfterDelete);
					}
					
				};
				link.add(new Label("label", group.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Is Admin")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
					IModel<Group> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().isAdministrator()));
			}
			
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Can Create Projects")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
					IModel<Group> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().isCreateProjects()));
			}

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
		});
		
		columns.add(new AbstractColumn<Group, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId, IModel<Group> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", GroupListPage.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to delete group '" + rowModel.getObject().getName() + "'?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						Group group = rowModel.getObject();
						OneDev.getInstance(GroupManager.class).delete(group);
						Session.get().success("Group '" + group.getName() + "' deleted");
						target.add(groupsTable);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<Group, Void> dataProvider = new SortableDataProvider<Group, Void>() {

			@Override
			public Iterator<? extends Group> iterator(long first, long count) {
				EntityCriteria<Group> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(GroupManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(GroupManager.class).count(getCriteria());
			}

			@Override
			public IModel<Group> model(Group object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Group>() {

					@Override
					protected Group load() {
						return OneDev.getInstance(GroupManager.class).load(id);
					}
					
				};
			}
		};

		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
				params.add(PARAM_PAGE, currentPage+1);
				if (query != null)
					params.add(PARAM_QUERY, query);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(groupsTable = new OneDataTable<>("groups", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Groups");
	}

}
