package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuItem;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.pullrequest.RequestLink;
import com.pmease.gitplex.web.component.user.UserSingleChoice;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RequestListPanel extends Panel {

	private DisplayOption displayOption;
	
	public RequestListPanel(String id, boolean open) {
		super(id);
		
		displayOption = new DisplayOption(open);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		MenuPanel filterMenu = new MenuPanel("filterMenu") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				menuItems.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						Fragment fragment = new Fragment(componentId, "filterOptionFrag", RequestListPanel.this);
						Link<Void> link = new Link<Void>("link") {

							@Override
							public void onClick() {
								displayOption.setSubmitterId(DisplayOption.SHOW_REQUESTS_OF_ALL_USERS);
							}
							
						};
						link.add(new Label("label", "Show requests of all users"));
						fragment.add(link);
						return fragment;
					}
					
				});
				if (User.getCurrentId() != 0L) {
					menuItems.add(new MenuItem() {

						@Override
						public Component newContent(String componentId) {
							Fragment fragment = new Fragment(componentId, "filterOptionFrag", RequestListPanel.this);
							Link<Void> link = new Link<Void>("link") {

								@Override
								public void onClick() {
									displayOption.setSubmitterId(DisplayOption.SHOW_REQUESTS_OF_CURRENT_USER);
								}
								
							};
							link.add(new Label("label", "Show my requests"));
							fragment.add(link);
							return fragment;
						}
						
					});
				}
				menuItems.add(new MenuItem() {

					@Override
					public Component newContent(String componentId) {
						Fragment fragment = new Fragment(componentId, "filterOptionFrag", RequestListPanel.this);
						Link<Void> link = new Link<Void>("link") {

							@Override
							public void onClick() {
								displayOption.setSubmitterId(DisplayOption.SHOW_REQUESTS_OF_SELECTED_USER);
							}
							
						};
						link.add(new Label("label", "Show requests of specified user"));
						fragment.add(link);
						return fragment;
					}
					
				});
				return menuItems;
			}
			
		};
		add(filterMenu);
		
		WebMarkupContainer filterMenuTrigger = new WebMarkupContainer("filterMenuTrigger");
		filterMenuTrigger.add(new Label("filterBy", new LoadableDetachableModel<String>() {

			@Override
			public String load() {
				if (displayOption.getSubmitterId() == DisplayOption.SHOW_REQUESTS_OF_ALL_USERS) {
					return "Show requests of all users";
				} else if (displayOption.getSubmitterId() == DisplayOption.SHOW_REQUESTS_OF_CURRENT_USER) {
					return "Show my requests";
				} else {
					return "Show requests of specified user";
				}
			}
			
		}));
		filterMenuTrigger.add(new MenuBehavior(filterMenu));
		add(filterMenuTrigger);
		
		add(new UserSingleChoice("userChoice", new IModel<User>() {

			@Override
			public void detach() {
			}

			@Override
			public User getObject() {
				if (displayOption.getSubmitterId() > 0L) {
					Dao dao = GitPlex.getInstance(Dao.class);
					return dao.load(User.class, displayOption.getSubmitterId());
				} else {
					return null;
				}
			}

			@Override
			public void setObject(User object) {
				if (object != null) {
					displayOption.setSubmitterId(object.getId());
				} else {
					displayOption.setSubmitterId(DisplayOption.SHOW_REQUESTS_OF_SELECTED_USER);
				}
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(displayOption.getSubmitterId() > 0L 
						|| displayOption.getSubmitterId() == DisplayOption.SHOW_REQUESTS_OF_SELECTED_USER);
			}
			
		}.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(getPage());
			}
			
		}));
		
		MenuPanel sortMenu = new MenuPanel("sortMenu") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				for (final SortOption sortOption: SortOption.values()) {
					menuItems.add(new MenuItem() {

						@Override
						public Component newContent(String componentId) {
							Fragment fragment = new Fragment(componentId, "sortOptionFrag", RequestListPanel.this);
							Link<Void> link = new Link<Void>("link") {

								@Override
								public void onClick() {
									displayOption.setSortOption(sortOption);
								}
								
							};
							link.add(new Label("label", "Sort by " + sortOption.toString()));
							fragment.add(link);
							return fragment;
						}
						
					});
				}
				return menuItems;
			}
			
		};
		add(sortMenu);
		
		WebMarkupContainer sortMenuTrigger = new WebMarkupContainer("sortMenuTrigger");
		sortMenuTrigger.add(new Label("sortBy", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Sort by " + displayOption.getSortOption();
			}
			
		}));
		sortMenuTrigger.add(new MenuBehavior(sortMenu));
		add(sortMenuTrigger);
		
		add(new Link<Void>("newPullRequest") {

			@Override
			public void onClick() {
				RepositoryPage page = (RepositoryPage) getPage();
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(page.getRepository()));
			}
			
		});
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Id")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				cellItem.add(new Label(componentId, "#" + request.getId()));
				cellItem.add(AttributeAppender.append("class", "narrow"));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Title")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new RequestLink(componentId, rowModel));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("To Branch")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new BranchLink(componentId, new EntityModel<Branch>(rowModel.getObject().getTarget())));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("From Branch")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				if (request.getSource() != null)
					cellItem.add(new BranchLink(componentId, new EntityModel<Branch>(request.getSource())));
				else
					cellItem.add(new Label(componentId, request.getSourceFQN()));
			}
			
		});
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Status")) {

			@Override
			public Component getHeader(String componentId) {
				Fragment fragment = new Fragment(componentId, "statusHeaderFrag", RequestListPanel.this);
				DropdownPanel helpPanel = new DropdownPanel("helpDropdown", false) {

					@Override
					protected Component newContent(String id) {
						return new Fragment(id, "helpFrag", RequestListPanel.this);
					}
					
				};
				fragment.add(helpPanel);
				fragment.add(new WebMarkupContainer("helpDropdownTrigger") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(displayOption.isOpen());
					}
					
				}.add(new DropdownBehavior(helpPanel)));
				return fragment;
			}

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new RequestStatusPanel(componentId, rowModel));
				cellItem.add(AttributeAppender.append("class", "narrow"));
			}
			
		});
		IDataProvider<PullRequest> dataProvider = new IDataProvider<PullRequest>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				RepositoryPage page = (RepositoryPage) getPage();
				return GitPlex.getInstance(Dao.class).query(displayOption.getCriteria(page.getRepository(), true), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				RepositoryPage page = (RepositoryPage) getPage();
				return GitPlex.getInstance(Dao.class).count(displayOption.getCriteria(page.getRepository(), false));
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				final Long pullRequestId = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return GitPlex.getInstance(Dao.class).load(PullRequest.class, pullRequestId);
					}
					
				};
			}
			
		};
		DataTable<PullRequest, Void> dataTable = new DataTable<>("pullRequests", columns, 
				dataProvider, Constants.DEFAULT_PAGE_SIZE);
		dataTable.addTopToolbar(new NavigationToolbar(dataTable));
		dataTable.addTopToolbar(new HeadersToolbar<>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable));
		add(dataTable);
	}

}