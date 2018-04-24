package io.onedev.server.web.page.project.pullrequests.requestlist;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.BranchLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.requeststatus.RequestStatusPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.newrequest.NewRequestPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.RequestOverviewPage;
import io.onedev.server.web.page.project.pullrequests.requestlist.SearchOption.Status;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class RequestListPage extends ProjectPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final Map<SortOption, String> sortNames = new LinkedHashMap<>();
	
	static {
		sortNames.put(new SortOption("submitDate", false), "Newest");
		sortNames.put(new SortOption("submitDate", true), "Oldest");
		sortNames.put(new SortOption("lastEvent.date", false), "Recently updated");
		sortNames.put(new SortOption("lastEvent.date", true), "Least recently updated");
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
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Open requests";
					}

					@Override
					public AbstractLink newLink(String id) {
						return new Link<Void>(id) {

							@Override
							public void onClick() {
								searchOption = new SearchOption();
								searchOption.setStatus(Status.OPEN);
								
								setResponsePage(RequestListPage.class, paramsOf(getProject(), searchOption, sortOption));
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Closed requests";
					}

					@Override
					public AbstractLink newLink(String id) {
						return new Link<Void>(id) {

							@Override
							public void onClick() {
								searchOption = new SearchOption();
								searchOption.setStatus(Status.CLOSED);
								
								setResponsePage(RequestListPage.class, paramsOf(getProject(), searchOption, sortOption));
							}
							
						};
					}
					
				});
				
				User currentUser = OneDev.getInstance(UserManager.class).getCurrent();
				if (currentUser != null) {
					String userName = currentUser.getName();
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return "My open requests";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									searchOption = new SearchOption();
									searchOption.setStatus(Status.OPEN);
									searchOption.setSubmitterName(userName);

									setResponsePage(RequestListPage.class, paramsOf(getProject(), searchOption, sortOption));
								}
								
							};
						}
						
					});
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return "My closed requests";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									searchOption = new SearchOption();
									searchOption.setStatus(Status.CLOSED);
									searchOption.setSubmitterName(userName);

									setResponsePage(RequestListPage.class, paramsOf(getProject(), searchOption, sortOption));
								}
								
							};
						}
						
					});
				}
				return menuItems;
			}
			
		});
		
		add(new MenuLink("sortBy") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
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
									setResponsePage(RequestListPage.class, paramsOf(getProject(), searchOption, sortOption));									
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
				ProjectPage page = (ProjectPage) getPage();
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(page.getProject()));
			}
			
		});

		Form<?> form = new Form<Void>("filterForm") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				setResponsePage(RequestListPage.class, paramsOf(getProject(), searchOption, sortOption));
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
				User userForDisplay = User.getForDisplay(request.getSubmitter(), request.getSubmitterName());
				fragment.add(new AvatarLink("submitter", userForDisplay));
				fragment.add(new Label("number", "#" + request.getNumber()));
				fragment.add(new ViewStateAwarePageLink<Void>("text", RequestOverviewPage.class, 
						RequestOverviewPage.paramsOf(request)) {

					@Override
					public IModel<?> getBody() {
						return Model.of(rowModel.getObject().getTitle());
					}
					
				});
				
				fragment.add(new RequestStatusPanel("status", rowModel));
				fragment.add(new BranchLink("target", request.getTarget(), null));
				if (request.getSource() != null) { 
					fragment.add(new BranchLink("source", request.getSource(), request));
				} else {
					fragment.add(new Label("source", "(deleted)") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					});
				}
					
				WebMarkupContainer lastEventContainer = new WebMarkupContainer("lastEvent");
				if (request.getLastEvent() != null) {
					String description = request.getLastEvent().getType();
					if (description.startsWith("there are")) {
						lastEventContainer.add(new WebMarkupContainer("user").setVisible(false));
					} else {
						userForDisplay = User.getForDisplay(request.getLastEvent().getUser(), 
								request.getLastEvent().getUserName());
						lastEventContainer.add(new UserLink("user", userForDisplay));
					}
					lastEventContainer.add(new Label("description", description));
					lastEventContainer.add(new Label("date", DateUtils.formatAge(request.getLastEvent().getDate())));
				} else {
					lastEventContainer.add(new UserLink("user", User.getForDisplay(request.getSubmitter(), 
							request.getSubmitterName())));
					lastEventContainer.add(new Label("description", "submitted"));
					lastEventContainer.add(new Label("date", DateUtils.formatAge(request.getSubmitDate())));
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
				ProjectPage page = (ProjectPage) getPage();
				
				EntityCriteria<PullRequest> criteria = searchOption.getCriteria(page.getProject());
				criteria.addOrder(sortOption.getOrder());
				return OneDev.getInstance(Dao.class).findRange(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				ProjectPage page = (ProjectPage) getPage();
				return OneDev.getInstance(Dao.class).count(searchOption.getCriteria(page.getProject()));
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				final Long pullRequestId = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return OneDev.getInstance(Dao.class).load(PullRequest.class, pullRequestId);
					}
					
				};
			}
			
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject());
				searchOption.fillPageParams(params);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		DataTable<PullRequest, Void> dataTable = new DataTable<>("requests", columns, 
				dataProvider, WebConstants.PAGE_SIZE);
		dataTable.setCurrentPage(pagingHistorySupport.getCurrentPage());
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		dataTable.addBottomToolbar(new NavigationToolbar(dataTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new HistoryAwarePagingNavigator(navigatorId, table, pagingHistorySupport);
			}
			
		});
		add(dataTable);		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new RequestListResourceReference()));
	}

	public static PageParameters paramsOf(Project project, SearchOption searchOption, SortOption sortOption) {
		PageParameters params = paramsOf(project);
		searchOption.fillPageParams(params);
		sortOption.fillPageParams(params);
		return params;
	}
	
}
