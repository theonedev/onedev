package io.onedev.server.web.page.admin.usermanagement;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.EmailAddressVerificationStatusBadge;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.user.UserCssResourceReference;
import io.onedev.server.web.page.user.basicsetting.UserBasicSettingPage;
import io.onedev.server.web.page.user.profile.UserProfilePage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.web.util.paginghistory.ParamPagingHistorySupport;

public class UserListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";

	private static final String PARAM_INCLUDE_DISABLED = "includeDisabled";
	
	private final IModel<List<User>> usersModel = new LoadableDetachableModel<List<User>>() {

		@Override
		protected List<User> load() {
			var userCache = getUserService().cloneCache();
			var emailAddressCache = getEmailAddressService().cloneCache();
			var emailAddresses = new HashMap<Long, Collection<String>>();
			for (var emailAddress: emailAddressCache.values()) 
				emailAddresses.computeIfAbsent(emailAddress.getOwnerId(), key -> new HashSet<>()).add(emailAddress.getValue());
			List<User> users = new ArrayList<>(userCache.getUsers(state.includeDisabled));
			users.sort(userCache.comparingDisplayName(Sets.newHashSet()));
			users = new Similarities<>(users) {

				@Override
				protected double getSimilarScore(User item) {
					var nameScore =  userCache.getSimilarScore(item, state.query);
					var emailScoreOptional = emailAddresses.getOrDefault(item.getId(), new HashSet<>())
							.stream()
							.mapToDouble(it -> Similarities.getSimilarScore(it, state.query))
							.max();
					return Math.max(nameScore, emailScoreOptional.orElse(-1));
				}

			};
			return users;
		}
		
	};
	
	private TextField<String> searchField;

	private Component countLabel;

	private AjaxLink<?> includeDisabledLink;
	
	private DataTable<User, Void> usersTable;

	private SortableDataProvider<User, Void> dataProvider;
			
	private SelectionColumn<User, Void> selectionColumn;
	
	private State state = new State();
	
	private boolean typing;
	
	public UserListPage(PageParameters params) {
		super(params);
		
		state.query = params.get(PARAM_QUERY).toString();
		state.includeDisabled = params.get(PARAM_INCLUDE_DISABLED).toBoolean(false);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		
		getPageParameters().set(PARAM_QUERY, state.query);
		getPageParameters().set(PARAM_INCLUDE_DISABLED, state.includeDisabled);
		target.add(searchField);
		target.add(countLabel);
		if (WicketUtils.isSubscriptionActive())
			target.add(includeDisabledLink);
		selectionColumn.getSelections().clear();
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
		
		add(searchField = new TextField<>("filterUsers", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.query;
			}

			@Override
			public void setObject(String object) {
				state.query = object;
				var params = paramsOf(state, 0);
				var url = RequestCycle.get().urlFor(UserListPage.class, params).toString();
				var target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (typing)
					replaceState(target, url, state);
				else
					pushState(target, url, state);

				target.add(countLabel);				
				usersTable.setCurrentPage(0);
				selectionColumn.getSelections().clear();
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
			
		});

		add(new MenuLink("operations") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				if (WicketUtils.isSubscriptionActive()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Enable Selected Users");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									getTransactionService().run(() -> {
										dropdown.close();
										var users = selectionColumn.getSelections().stream().map(IModel::getObject).collect(Collectors.toSet());
										getUserService().enable(users);
										for (var user: users) {
											auditService.audit(null, "enabled account \"" + user.getName() + "\"", null, null);
										}
										target.add(countLabel);
										target.add(usersTable);
										selectionColumn.getSelections().clear();
										Session.get().success(_T("Users enabled successfully"));
									});
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select users to enable"));
									}
								}

							};
						}

					});									
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Disable Selected Users");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									
									for (var model: selectionColumn.getSelections()) {
										var user = model.getObject();
										if (user.isRoot()) {
											Session.get().error(_T("Can not disable root account"));
											return;
										} else if (user.equals(SecurityUtils.getAuthUser())) {
											Session.get().error(_T("Can not disable yourself"));
											return;
										}
									}
									
									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(() -> {
												var users = selectionColumn.getSelections().stream().map(IModel::getObject).collect(Collectors.toSet());
												getUserService().disable(users);
												for (var user: users) {
													auditService.audit(null, "disabled account \"" + user.getName() + "\"", null, null);
												}
												target.add(countLabel);
												target.add(usersTable);
												selectionColumn.getSelections().clear();
												Session.get().success(_T("Users disabled successfully"));
											});
										}

										@Override
										protected String getConfirmMessage() {
											return _T("Disabling accounts will reset password, clear access tokens, and remove all references from other " 
													+ "entities except for past activities. Type <code>yes</code> to confirm");
										}

										@Override
										protected String getConfirmInput() {
											return "yes";
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select users to disable"));
									}
								}

							};
						}

					});		
					
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Convert Selected to Service Accounts");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									
									for (var model: selectionColumn.getSelections()) {
										var user = model.getObject();
										if (user.isRoot()) {
											Session.get().error(_T("Can not convert root user to service account"));
											return;
										} else if (user.equals(SecurityUtils.getAuthUser())) {
											Session.get().error(_T("Can not convert yourself to service account"));
											return;
										}
									}
									
									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(() -> {
												var users = selectionColumn.getSelections().stream().map(IModel::getObject).collect(Collectors.toSet());
												getUserService().convertToServiceAccounts(users);
												for (var user: users) {
													auditService.audit(null, "converted \"" + user.getName() + "\" to service account", null, null);
												}
												target.add(countLabel);
												target.add(usersTable);
												selectionColumn.getSelections().clear();
												Session.get().success(_T("Users converted to service accounts successfully"));
											});
										}

										@Override
										protected String getConfirmMessage() {
											return _T("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm");
										}

										@Override
										protected String getConfirmInput() {
											return "yes";
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select users to convert to service accounts"));
									}
								}

							};
						}

					});														
				}
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Delete Selected Users");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								for (var model: selectionColumn.getSelections()) {
									var user = model.getObject();
									if (user.isRoot()) {
										Session.get().error(_T("Can not delete root account"));
										return;
									} else if (user.equals(SecurityUtils.getAuthUser())) {
										Session.get().error(_T("Can not delete yourself"));
										return;
									}
								}
								
								new ConfirmModalPanel(target) {

									@Override
									protected void onConfirm(AjaxRequestTarget target) {
										getTransactionService().run(() -> {
											var users = selectionColumn.getSelections().stream().map(IModel::getObject).collect(Collectors.toSet());
											getUserService().delete(users);
											for (var user: users) {
												var oldAuditContent = VersionedXmlDoc.fromBean(user).toXML();
												auditService.audit(null, "deleted account \"" + user.getName() + "\"", oldAuditContent, null);
											}
											target.add(countLabel);
											target.add(usersTable);
											selectionColumn.getSelections().clear();
											Session.get().success(_T("Users deleted successfully"));
										});
									}

									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to confirm deleting selected users");
									}

									@Override
									protected String getConfirmInput() {
										return "yes";
									}

								};
							}

							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(!selectionColumn.getSelections().isEmpty());
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("title", "Please select users to delete");
								}
							}

						};
					}

				});
				
				if (WicketUtils.isSubscriptionActive()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Enable All Queried Users");
						}
	
						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {
	
								@Override
								public void onClick(AjaxRequestTarget target) {
									getTransactionService().run(() -> {
										dropdown.close();
										Collection<User> users = new ArrayList<>();
										for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();)
											users.add(it.next());
										getUserService().enable(users);
										for (var user: users) {
											auditService.audit(null, "enabled account \"" + user.getName() + "\"", null, null);
										}
										target.add(usersTable);
										dataProvider.detach();
										usersModel.detach();
										selectionColumn.getSelections().clear();
	
										Session.get().success(_T("Users enabled successfully"));								
									});
								}
	
								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(usersTable.getItemCount() != 0);
								}
	
								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No users to enable"));
									}
								}
	
							};
						}
	
					});					
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Disable All Queried Users");
						}
	
						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {
	
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
	
									for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();) {
										var user = it.next();
										if (user.isRoot()) {
											Session.get().error(_T("Can not disable root account"));
											return;
										} else if (user.equals(SecurityUtils.getAuthUser())) {
											Session.get().error(_T("Can not disable yourself"));
											return;
										}
									}
									
									new ConfirmModalPanel(target) {
	
										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(() -> {
												Collection<User> users = new ArrayList<>();
												for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();)
													users.add(it.next());
												getUserService().disable(users);
												for (var user: users) {
													auditService.audit(null, "disabled account \"" + user.getName() + "\"", null, null);
												}
												target.add(usersTable);
												dataProvider.detach();
												usersModel.detach();
												selectionColumn.getSelections().clear();
		
												Session.get().success(_T("Users disabled successfully"));													
											});
										}
	
										@Override
										protected String getConfirmMessage() {
											return _T("Disabling accounts will reset password, clear access tokens, and remove all references from other " 
													+ "entities except for past activities. Type <code>yes</code> to confirm");
										}
	
										@Override
										protected String getConfirmInput() {
											return "yes";
										}
	
									};
								}
	
								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(usersTable.getItemCount() != 0);
								}
	
								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No users to disable"));
									}
								}
	
							};
						}
	
					});
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Convert All Queried to Service Accounts");
						}
	
						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {
	
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
	
									for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();) {
										var user = it.next();
										if (user.isRoot()) {
											Session.get().error(_T("Can not convert root user to service account"));
											return;
										} else if (user.equals(SecurityUtils.getAuthUser())) {
											Session.get().error(_T("Can not convert yourself to service account"));
											return;
										}
									}
									
									new ConfirmModalPanel(target) {
	
										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(() -> {
												Collection<User> users = new ArrayList<>();
												for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();)
													users.add(it.next());
												getUserService().convertToServiceAccounts(users);
												for (var user: users) {
													auditService.audit(null, "converted user \"" + user.getName() + "\" to service account", null, null);
												}
												target.add(usersTable);
												dataProvider.detach();
												usersModel.detach();
												selectionColumn.getSelections().clear();
		
												Session.get().success(_T("Users converted to service accounts successfully"));													
											});
										}
	
										@Override
										protected String getConfirmMessage() {
											return _T("Converting to service accounts will remove password, email addresses, all assignments and watches. Type <code>yes</code> to confirm");
										}
	
										@Override
										protected String getConfirmInput() {
											return "yes";
										}
	
									};
								}
	
								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(usersTable.getItemCount() != 0);
								}
	
								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No users to convert to service accounts"));
									}
								}
	
							};
						}
	
					});
				}

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Delete All Queried Users");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();

								for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();) {
									var user = it.next();
									if (user.isRoot()) {
										Session.get().error(_T("Can not delete root account"));
										return;
									} else if (user.equals(SecurityUtils.getAuthUser())) {
										Session.get().error(_T("Can not delete yourself"));
										return;
									}
								}
								
								new ConfirmModalPanel(target) {

									@Override
									protected void onConfirm(AjaxRequestTarget target) {
										getTransactionService().run(() -> {
											Collection<User> users = new ArrayList<>();
											for (@SuppressWarnings("unchecked") var it = (Iterator<User>) dataProvider.iterator(0, usersTable.getItemCount()); it.hasNext();)
												users.add(it.next());
											getUserService().delete(users);
											for (var user: users) {
												var oldAuditContent = VersionedXmlDoc.fromBean(user).toXML();
												auditService.audit(null, "deleted account \"" + user.getName() + "\"", oldAuditContent, null);
											}											
											target.add(usersTable);
											dataProvider.detach();
											usersModel.detach();
											selectionColumn.getSelections().clear();
	
											Session.get().success(_T("Users deleted successfully"));
										});
									}

									@Override
									protected String getConfirmMessage() {
										return _T("Type <code>yes</code> below to confirm deleting all queried users");
									}

									@Override
									protected String getConfirmInput() {
										return "yes";
									}

								};
							}

							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(usersTable.getItemCount() != 0);
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No users to delete"));
								}
							}

						};
					}

				});
				
				return menuItems;
			}

		});
		
		add(includeDisabledLink = new AjaxLink<Void>("includeDisabled") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(WicketUtils.isSubscriptionActive());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				state.includeDisabled = !state.includeDisabled;
				var params = paramsOf(state, 0);
				var url = RequestCycle.get().urlFor(UserListPage.class, params).toString();
				pushState(target, url, state);

				target.add(this);
				target.add(countLabel);

				usersTable.setCurrentPage(0);
				selectionColumn.getSelections().clear();
				target.add(usersTable);
			}

		});
		includeDisabledLink.add(new SpriteImage("icon", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return state.includeDisabled? "tick-box": "square";
			}
			
		}));
		
		List<IColumn<User, Void>> columns = new ArrayList<>();

		columns.add(selectionColumn = new SelectionColumn<User, Void>());
		
		columns.add(new AbstractColumn<>(Model.of(_T("Login Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
									 IModel<User> rowModel) {
				User user = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "nameFrag", UserListPage.this);
				var link = new ActionablePageLink("link", UserProfilePage.class, UserProfilePage.paramsOf(user)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								UserListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(User.class, redirectUrlAfterDelete);
					}
		
				};
				link.add(new UserAvatar("avatar", user));
				link.add(new Label("name", user.getName()));
				link.add(new WebMarkupContainer("service").setVisible(user.isServiceAccount() && WicketUtils.isSubscriptionActive()));
				link.add(new WebMarkupContainer("disabled").setVisible(user.isDisabled() && WicketUtils.isSubscriptionActive()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("Full Name"))) {

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
		
		columns.add(new AbstractColumn<>(Model.of(_T("Primary Email"))) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId,
									 IModel<User> rowModel) {
				if (rowModel.getObject().isServiceAccount()) {
					cellItem.add(new Label(componentId, "<i>N/A</i>").setEscapeModelStrings(false));
				} else {
					EmailAddress emailAddress = rowModel.getObject().getPrimaryEmailAddress();
					if (emailAddress != null) {
						Fragment fragment = new Fragment(componentId, "emailFrag", UserListPage.this);
						fragment.add(new Label("emailAddress", emailAddress.getValue()));
						fragment.add(new EmailAddressVerificationStatusBadge(
								"verificationStatus", Model.of(emailAddress)));
						cellItem.add(fragment);
					} else {
						cellItem.add(new Label(componentId, "<i>" + _T("Not specified") + "</i>").setEscapeModelStrings(false));
					}
				}
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of(_T("Auth Source"))) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				if (rowModel.getObject().isServiceAccount() || rowModel.getObject().isDisabled()) {
					cellItem.add(new Label(componentId, "<i>" + _T("N/A") + "</i>").setEscapeModelStrings(false));
				} else {
					cellItem.add(new Label(componentId, _T(rowModel.getObject().getAuthSource())));
				}
			}

		});

		columns.add(new AbstractColumn<User, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionsFrag", UserListPage.this);
				
				fragment.add(new ActionablePageLink("edit", UserBasicSettingPage.class, UserBasicSettingPage.paramsOf(rowModel.getObject())) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								UserListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(User.class, redirectUrlAfterDelete);
					}
		
				});

				fragment.add(new Link<Void>("impersonate") {

					@Override
					public void onClick() {
						SecurityUtils.getSubject().runAs(rowModel.getObject().getPrincipals());
						throw new RestartResponseException(HomePage.class);
					}
										
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});

		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return MessageFormat.format(_T("found {0} users"), dataProvider.size());
				else
					return _T("found 1 user");
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(dataProvider.size() != 0);
			}
		}.setOutputMarkupPlaceholderTag(true));
		
		dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends User> iterator(long first, long count) {
				List<User> users = usersModel.getObject();
				if (first + count > users.size())
					return users.subList((int) first, users.size()).iterator();
				else
					return users.subList((int) first, (int) (first + count)).iterator();
			}

			@Override
			public long calcSize() {
				return usersModel.getObject().size();
			}

			@Override
			public IModel<User> model(User object) {
				Long id = object.getId();
				return new LoadableDetachableModel<User>() {

					@Override
					protected User load() {
						return getUserService().load(id);
					}

				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new ParamPagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				return paramsOf(state, currentPage + 1);
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(usersTable = new DefaultDataTable<User, Void>("users", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}
	
	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
	private EmailAddressService getEmailAddressService() {
		return OneDev.getInstance(EmailAddressService.class);
	}

	private TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}

	@Override
	protected void onDetach() {
		usersModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

	public static PageParameters paramsOf(State state, int page) {
		PageParameters params = new PageParameters();
		if (state.query != null)
			params.add(PARAM_QUERY, state.query);
		if (state.includeDisabled)
			params.add(PARAM_INCLUDE_DISABLED, state.includeDisabled);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Users"));
	}

	public static class State implements Serializable {
		
		private static final long serialVersionUID = 1L;

		public String query;

		public boolean includeDisabled;
		
	}

}
