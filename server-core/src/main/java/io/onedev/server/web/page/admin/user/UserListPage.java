package io.onedev.server.web.page.admin.user;

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
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private TextField<String> searchField;
	
	private DataTable<User, Void> usersTable;
	
	private String query;
	
	private boolean typing;
	
	public UserListPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
	}
	
	private EntityCriteria<User> getCriteria() {
		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		criteria.add(Restrictions.not(Restrictions.eq("id", User.SYSTEM_ID)));
		if (query != null) {
			criteria.add(Restrictions.or(
					Restrictions.ilike("name", query, MatchMode.ANYWHERE), 
					Restrictions.ilike("fullName", query, MatchMode.ANYWHERE)));
		} else {
			criteria.setCacheable(true);
		}
		return criteria;
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(searchField);
		target.add(usersTable);
	}

	@Override
	protected void onBeforeRender() {
		typing = false;
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchField = new TextField<String>("filterUsers", new IModel<String>() {

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
				
				String url = RequestCycle.get().urlFor(UserListPage.class, params).toString();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				usersTable.setCurrentPage(0);
				target.add(usersTable);
				
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
				setResponsePage(NewUserPage.class);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		List<IColumn<User, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Login Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				User user = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", UserListPage.this);
				WebMarkupContainer link = new ActionablePageLink<Void>("link", UserProfilePage.class, UserProfilePage.paramsOf(user)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								UserListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(User.class, redirectUrlAfterDelete);
					}
					
				};
				link.add(new UserAvatar("avatar", user));
				link.add(new Label("name", user.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Full Name")) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getFullName()));
			}
			
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Email")) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmail()));
			}
			
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Auth Source")) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getAuthSource()));
			}
			
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionsFrag", UserListPage.this);
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						User user = rowModel.getObject();
						OneDev.getInstance(UserManager.class).delete(user);
						Session.get().success("User '" + user.getDisplayName() + "' deleted");
						
						target.add(usersTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						
						User user = rowModel.getObject();
						String message = "Do you really want to delete user '" + user.getDisplayName() + "'?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled())
							tag.put("disabled", "disabled");
						User user = rowModel.getObject();
						if (user.isRoot())
							tag.put("title", "Root user can not be deleted");
						else if (user.equals(SecurityUtils.getUser()))
							tag.put("title", "You can not delete yourself");
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						User user = rowModel.getObject();
						setEnabled(!user.isRoot() && !user.equals(SecurityUtils.getUser()));
					}

				});
				
				fragment.add(new Link<Void>("impersonate") {

					@Override
					public void onClick() {
						SecurityUtils.getSubject().runAs(rowModel.getObject().getPrincipals());
						setResponsePage(ProjectListPage.class);
					}
										
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<User, Void> dataProvider = new LoadableDetachableDataProvider<User, Void>() {

			@Override
			public Iterator<? extends User> iterator(long first, long count) {
				EntityCriteria<User> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(UserManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long calcSize() {
				return OneDev.getInstance(UserManager.class).count(getCriteria());
			}

			@Override
			public IModel<User> model(User object) {
				Long id = object.getId();
				return new LoadableDetachableModel<User>() {

					@Override
					protected User load() {
						return OneDev.getInstance(UserManager.class).load(id);
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
		
		add(usersTable = new OneDataTable<User, Void>("users", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Users");
	}

}
