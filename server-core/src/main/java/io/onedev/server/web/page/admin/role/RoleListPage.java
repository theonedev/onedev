package io.onedev.server.web.page.admin.role;

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
import org.apache.wicket.markup.ComponentTag;
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
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class RoleListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private DataTable<Role, Void> rolesTable;
	
	private TextField<String> searchField;
	
	private boolean typing;
	
	private String query;

	public RoleListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toString();
	}
	
	private EntityCriteria<Role> getCriteria() {
		EntityCriteria<Role> criteria = EntityCriteria.of(Role.class);
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
		target.add(rolesTable);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchField = new TextField<String>("filterRoles", new IModel<String>() {

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
				
				String url = RequestCycle.get().urlFor(RoleListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				rolesTable.setCurrentPage(0);
				target.add(rolesTable);
				
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
				setResponsePage(NewRolePage.class);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		List<IColumn<Role, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Role, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<Role>> cellItem, String componentId, IModel<Role> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameFrag", RoleListPage.this);
				
				Role role = rowModel.getObject();
				
				WebMarkupContainer link = new ActionablePageLink<Void>("link", 
						RoleDetailPage.class, RoleDetailPage.paramsOf(role)) {

					@Override
					public void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								RoleListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Role.class, redirectUrlAfterDelete);
					}
					
				};
				link.add(new Label("label", role.getName()));
				fragment.add(link);
				
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Role, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<Role>> cellItem, String componentId, IModel<Role> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", RoleListPage.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to delete role '" + rowModel.getObject().getName() + "'?";						
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						Role role = rowModel.getObject();
						OneDev.getInstance(RoleManager.class).delete(role);
						Session.get().success("Role '" + role.getName() + "' deleted");
						target.add(rolesTable);
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled()) {
							tag.put("disabled", "disabled");
							tag.put("title", "This is a built-in role and can not be deleted");
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setEnabled(!rowModel.getObject().isOwner());
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<Role, Void> dataProvider = new SortableDataProvider<Role, Void>() {

			@Override
			public Iterator<? extends Role> iterator(long first, long count) {
				EntityCriteria<Role> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(RoleManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return OneDev.getInstance(RoleManager.class).count(getCriteria());
			}

			@Override
			public IModel<Role> model(Role object) {
				Long id = object.getId();
				return new LoadableDetachableModel<Role>() {

					@Override
					protected Role load() {
						return OneDev.getInstance(RoleManager.class).load(id);
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
		
		add(rolesTable = new OneDataTable<>("roles", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Roles");
	}

}
