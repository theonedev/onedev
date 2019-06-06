package io.onedev.server.web.page.admin.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.commons.utils.HtmlUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserListPage extends AdministrationPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private DataTable<User, Void> usersTable;
	
	private String query;
	
	public UserListPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
	}
	
	private EntityCriteria<User> getCriteria() {
		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		if (query != null) {
			criteria.add(Restrictions.or(
					Restrictions.ilike("name", query, MatchMode.ANYWHERE), 
					Restrictions.ilike("fullName", query, MatchMode.ANYWHERE)));
		}
		return criteria;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("filterUsers", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(usersTable);
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
		
		add(new FencedFeedbackPanel("feedback", this).setEscapeModelStrings(false));
		
		List<IColumn<User, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Login Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				User user = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", UserListPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", UserProfilePage.class, UserProfilePage.paramsOf(user));
				link.add(new UserAvatar("avatar", UserIdent.of(UserFacade.of(user))));
				link.add(new Label("name", user.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Full Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getFullName()));
			}
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Email")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmail()));
			}
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", UserListPage.this);
				
				fragment.add(new Link<Void>("profile") {

					@Override
					public void onClick() {
						PageParameters params = UserPage.paramsOf(rowModel.getObject());
						setResponsePage(UserProfilePage.class, params);
					}

				});
				
				User user = rowModel.getObject();
				
				fragment.add(new Link<Void>("delete") {

					@Override
					public void onClick() {
						try {
							OneDev.getInstance(UserManager.class).delete(rowModel.getObject());
							setResponsePage(UserListPage.class);
						} catch (OneException e) {
							getPage().error(HtmlUtils.formatAsHtml(e.getMessage()));
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						User user = rowModel.getObject();
						setVisible(SecurityUtils.isAdministrator() && !user.isRoot() && !user.equals(getLoginUser()));
					}

				}.add(new ConfirmOnClick("Do you really want to delete user '" + user.getDisplayName() + "'?")));
				
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
		
		add(usersTable = new HistoryAwareDataTable<User, Void>("users", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

}
