package io.onedev.server.web.page.admin.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
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
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.modal.confirm.ConfirmModal;
import io.onedev.server.web.component.user.UserDeleteLink;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private DataTable<User, Void> usersTable;
	
	private String query;
	
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
				link.add(new UserAvatar("avatar", user));
				link.add(new Label("name", user.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<User, Void>(Model.of("Full Name")) {

			@Override
			public String getCssClass() {
				return "expanded";
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
				return "expanded";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
					IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmail()));
			}
			
		});
		
		if (OneDev.getInstance(SettingManager.class).getAuthenticator() != null) {
			columns.add(new AbstractColumn<User, Void>(Model.of("Authenticator")) {

				private Component newCheckBox(String componentId, IModel<User> userModel) {
					boolean useExternal = userModel.getObject().getPassword().equals(User.EXTERNAL_MANAGED);
					CheckBox checkbox = new CheckBox(componentId, Model.of(useExternal)) {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setEnabled(!userModel.getObject().isRoot());
						}

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							if (userModel.getObject().isRoot()) {
								tag.put("disabled", "disabled");
								tag.put("title", "Administrator always authenticate via internal database");
							}
						}

					};
					
					checkbox.add(new OnChangeAjaxBehavior() {
						
						private void refresh(AjaxRequestTarget target) {
							Component newCheckbox = newCheckBox(componentId, userModel);
							checkbox.replaceWith(newCheckbox);
							target.add(newCheckbox);
						}
						
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							User user = userModel.getObject();
							if (user.getPassword().equals(User.EXTERNAL_MANAGED)) {
								PasswordBean bean = new PasswordBean();
								new BeanEditModalPanel(target, bean) {
									
									@Override
									protected void onSave(AjaxRequestTarget target, Serializable bean) {
										User user = userModel.getObject();
										user.setPassword(OneDev.getInstance(PasswordService.class).encryptPassword(((PasswordBean) bean).getPassword()));
										OneDev.getInstance(UserManager.class).save(user);
										refresh(target);
										close();
										Session.get().success("Switched to authenticate via internal database");
									}

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										super.onCancel(target);
										refresh(target);
									}
									
								};
							} else {
								new ConfirmModal(target) {
									
									@Override
									protected void onConfirm(AjaxRequestTarget target) {
										user.setPassword(User.EXTERNAL_MANAGED);
										OneDev.getInstance(UserManager.class).save(user);
										refresh(target);
										Session.get().success("Switched to use external authenticator");
									}
									
									@Override
									protected void onCancel(AjaxRequestTarget target) {
										super.onCancel(target);
										refresh(target);
									}

									@Override
									protected String getConfirmMessage() {
										return "This will clear password of user '" + userModel.getObject().getDisplayName() 
												+ "' from internal database, and use external authenticator instead. Do you really want to continue?";
									}
									
									@Override
									protected String getConfirmInput() {
										return null;
									}
									
								};
							}
						}
						
					}).setOutputMarkupId(true);		
					
					return checkbox;
				}
				
				@Override
				public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
					Fragment fragment = new Fragment(componentId, "authenticatorFrag", UserListPage.this);
					fragment.add(newCheckBox("external", rowModel));
					cellItem.add(fragment);
				}
				
			});
		}
		
		columns.add(new AbstractColumn<User, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", UserListPage.this);
				
				fragment.add(new UserDeleteLink("delete") {

					@Override
					protected User getUser() {
						return rowModel.getObject();
					}

					@Override
					protected void onDeleted(AjaxRequestTarget target) {
						setResponsePage(UserListPage.class, getPageParameters());
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
		
		add(usersTable = new DefaultDataTable<User, Void>("users", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

	@Editable(name="Authenticate via Internal Database")
	public static class PasswordBean implements Serializable {
		
		private String password;

		@Editable(order=200, name="Specify User Password", 
				description="To authenticate via internal database, you need to specify password of the user")
		@Password(confirmative=true)
		@NotEmpty
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
	}
	
}
