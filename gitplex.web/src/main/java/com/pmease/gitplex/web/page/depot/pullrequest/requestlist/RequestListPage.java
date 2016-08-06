package com.pmease.gitplex.web.page.depot.pullrequest.requestlist;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.BranchLink;
import com.pmease.gitplex.web.component.pullrequest.requeststatus.RequestStatusPanel;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.pullrequest.PullRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.SearchOption.Status;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class RequestListPage extends PullRequestPage {

	private static final Map<SortOption, String> sortNames = new LinkedHashMap<>();
	
	static {
		sortNames.put(new SortOption("submitDate", false), "Newest");
		sortNames.put(new SortOption("submitDate", true), "Oldest");
		sortNames.put(new SortOption("lastEventDate", false), "Recently updated");
		sortNames.put(new SortOption("lastEventDate", true), "Least recently updated");
	}
	
	private SearchOption searchOption;
	
	private SortOption sortOption;
	
	public RequestListPage(PageParameters params) {
		super(params);
		
		searchOption = new SearchOption(params);
		sortOption = new SortOption(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new MenuLink("filters") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();

				Account currentUser = GitPlex.getInstance(AccountManager.class).getCurrent();
				if (currentUser != null) {
					final String userName = currentUser.getName();
					menuItems.add(new MenuItem() {

						@Override
						public String getIconClass() {
							return null;
						}

						@Override
						public String getLabel() {
							return "Open requests assigned to me";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									searchOption = new SearchOption();
									searchOption.setStatus(Status.OPEN);
									searchOption.setAssigneeName(userName);
									
									setResponsePage(RequestListPage.class, paramsOf(getDepot(), searchOption, sortOption));
								}
								
							};
						}
						
					});
					menuItems.add(new MenuItem() {

						@Override
						public String getIconClass() {
							return null;
						}

						@Override
						public String getLabel() {
							return "Open requests submitted by me";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									searchOption = new SearchOption();
									searchOption.setStatus(Status.OPEN);
									searchOption.setSubmitterName(userName);

									setResponsePage(RequestListPage.class, paramsOf(getDepot(), searchOption, sortOption));
								}
								
							};
						}
						
					});
				}
				menuItems.add(new MenuItem() {

					@Override
					public String getIconClass() {
						return null;
					}

					@Override
					public String getLabel() {
						return "All open requests";
					}

					@Override
					public AbstractLink newLink(String id) {
						return new Link<Void>(id) {

							@Override
							public void onClick() {
								searchOption = new SearchOption();
								searchOption.setStatus(Status.OPEN);
								
								setResponsePage(RequestListPage.class, paramsOf(getDepot(), searchOption, sortOption));
							}
							
						};
					}
					
				});
				menuItems.add(new MenuItem() {

					@Override
					public String getIconClass() {
						return null;
					}

					@Override
					public String getLabel() {
						return "All closed requests";
					}

					@Override
					public AbstractLink newLink(String id) {
						return new Link<Void>(id) {

							@Override
							public void onClick() {
								searchOption = new SearchOption();
								searchOption.setStatus(Status.CLOSED);
								
								setResponsePage(RequestListPage.class, paramsOf(getDepot(), searchOption, sortOption));
							}
							
						};
					}
					
				});
				return menuItems;
			}
			
		});
		
		add(new MenuLink("sortBy") {

			@Override
			protected List<MenuItem> getMenuItems() {
				List<MenuItem> menuItems = new ArrayList<>();
				
				for (Map.Entry<SortOption, String> entry: sortNames.entrySet()) {
					final SortOption sortOption = entry.getKey();
					final String displayName = entry.getValue();
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return displayName;
						}

						@Override
						public String getIconClass() {
							if (RequestListPage.this.sortOption.equals(sortOption))
								return "fa fa-check";
							else
								return null;
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									setResponsePage(RequestListPage.class, paramsOf(getDepot(), searchOption, sortOption));									
								}
								
							};
						}
						
					});
				}

				return menuItems;
			}
			
		});
		
		add(new Link<Void>("newRequest") {

			@Override
			public void onClick() {
				DepotPage page = (DepotPage) getPage();
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(page.getDepot()));
			}
			
		});

		Form<?> form = new Form<Void>("filterForm") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				setResponsePage(RequestListPage.class, paramsOf(getDepot(), searchOption, sortOption));
			}
			
		};
		form.add(BeanContext.editBean("editor", searchOption));
		add(form);
		
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Pull Request")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem,
					String componentId, final IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "requestFrag", RequestListPage.this);
				fragment.add(new Label("number", "#" + request.getNumber()));
				fragment.add(new BookmarkablePageLink<Void>("title", RequestOverviewPage.class, 
						RequestOverviewPage.paramsOf(request)) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getTitle());
					}
					
				});
				
				fragment.add(new RequestStatusPanel("status", rowModel, false));
				fragment.add(new AccountLink("submitter", rowModel.getObject().getSubmitter()));
				fragment.add(new BranchLink("target", request.getTarget()));
				fragment.add(new BranchLink("source", request.getSource()));
				fragment.add(new Label("date", DateUtils.formatAge(request.getSubmitDate())));
				
				WebMarkupContainer lastEventContainer = new WebMarkupContainer("lastEvent");
				if (request.getLastEvent() != null) {
					lastEventContainer.add(new AccountLink("user", request.getLastEvent().getUser())
							.setVisible(request.getLastEvent().getUser()!=null));
					lastEventContainer.add(new Label("description", request.getLastEvent().getDescription()));
					lastEventContainer.add(new Label("date", DateUtils.formatAge(request.getLastEvent().getDate())));
				} else {
					lastEventContainer.add(new AccountLink("user", (Account)null));
					lastEventContainer.add(new Label("description"));
					lastEventContainer.add(new Label("date"));
					lastEventContainer.setVisible(false);
				}
				fragment.add(lastEventContainer);
				
				cellItem.add(fragment);

				Date lastActivityDate;
				if (request.getLastEvent() != null)
					lastActivityDate = request.getLastEvent().getDate();
				else
					lastActivityDate = request.getSubmitDate();
				cellItem.add(AttributeAppender.append("class", 
						request.isVisitedAfter(lastActivityDate)?"request":"request new"));
			}
			
		});

		IDataProvider<PullRequest> dataProvider = new IDataProvider<PullRequest>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				DepotPage page = (DepotPage) getPage();
				
				EntityCriteria<PullRequest> criteria = searchOption.getCriteria(page.getDepot());
				criteria.addOrder(sortOption.getOrder());
				return GitPlex.getInstance(Dao.class).findRange(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				DepotPage page = (DepotPage) getPage();
				return GitPlex.getInstance(Dao.class).count(searchOption.getCriteria(page.getDepot()));
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
		DataTable<PullRequest, Void> dataTable = new DataTable<>("requests", columns, 
				dataProvider, Constants.DEFAULT_PAGE_SIZE);
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable) {
			
			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new BootstrapPagingNavigator(navigatorId, dataTable);
			}
			
		});
		add(dataTable);		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestListPage.class, "request-list.css")));
	}

	public static PageParameters paramsOf(Depot depot, SearchOption searchOption, SortOption sortOption) {
		PageParameters params = paramsOf(depot);
		searchOption.fillPageParams(params);
		sortOption.fillPageParams(params);
		return params;
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}
	
}
