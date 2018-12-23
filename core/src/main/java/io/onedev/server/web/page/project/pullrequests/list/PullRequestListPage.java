package io.onedev.server.web.page.project.pullrequests.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestQuerySettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestQuerySetting;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.PullRequestQueryBehavior;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.savedquery.NamedQueriesBean;
import io.onedev.server.web.page.project.savedquery.SaveQueryPanel;
import io.onedev.server.web.page.project.savedquery.SavedQueriesClosed;
import io.onedev.server.web.page.project.savedquery.SavedQueriesOpened;
import io.onedev.server.web.page.project.savedquery.SavedQueriesPanel;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.VisibleVisitor;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class PullRequestListPage extends ProjectPage {

	private static final Logger logger = LoggerFactory.getLogger(PullRequestListPage.class);
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private IModel<PullRequestQuery> parsedQueryModel = new LoadableDetachableModel<PullRequestQuery>() {

		@Override
		protected PullRequestQuery load() {
			try {
				PullRequestQuery parsedQuery = PullRequestQuery.parse(getProject(), query, true);
				if (SecurityUtils.getUser() == null && parsedQuery.needsLogin())  
					error("Please login to perform this query");
				else
					return parsedQuery;
			} catch (Exception e) {
				logger.error("Error parsing pull request query: " + query, e);
				error(e.getMessage());
			}
			return null;
		}
		
	};
	
	private String query;
	
	private DataTable<PullRequest, Void> requestsTable;
	
	public PullRequestListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
		if (query != null && query.length() == 0) {
			query = null;
			List<String> queries = new ArrayList<>();
			if (getProject().getPullRequestQuerySettingOfCurrentUser() != null) { 
				for (NamedPullRequestQuery namedQuery: getProject().getPullRequestQuerySettingOfCurrentUser().getUserQueries())
					queries.add(namedQuery.getQuery());
			}
			for (NamedPullRequestQuery namedQuery: getProject().getSavedPullRequestQueries())
				queries.add(namedQuery.getQuery());
			for (String each: queries) {
				try {
					if (SecurityUtils.getUser() != null || !PullRequestQuery.parse(getProject(), each, true).needsLogin()) {  
						query = each;
						break;
					}
				} catch (Exception e) {
				}
			} 
		}
	}

	private PullRequestQuerySettingManager getPullRequestQuerySettingManager() {
		return OneDev.getInstance(PullRequestQuerySettingManager.class);		
	}
	
	private PullRequestManager getPullRequestManager() {
		return OneDev.getInstance(PullRequestManager.class);		
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		SavedQueriesPanel<NamedPullRequestQuery> savedQueries;
		add(savedQueries = new SavedQueriesPanel<NamedPullRequestQuery>("side") {

			@Override
			protected NamedQueriesBean<NamedPullRequestQuery> newNamedQueriesBean() {
				return new NamedPullRequestQueriesBean();
			}

			@Override
			protected boolean needsLogin(NamedPullRequestQuery namedQuery) {
				return PullRequestQuery.parse(getProject(), namedQuery.getQuery(), true).needsLogin();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedPullRequestQuery namedQuery) {
				return new BookmarkablePageLink<Void>(componentId, PullRequestListPage.class, 
						PullRequestListPage.paramsOf(getProject(), namedQuery.getQuery(), 0));
			}

			@Override
			protected QuerySetting<NamedPullRequestQuery> getQuerySetting() {
				return getProject().getPullRequestQuerySettingOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedPullRequestQuery> getProjectQueries() {
				return getProject().getSavedPullRequestQueries();
			}

			@Override
			protected void onSaveQuerySetting(QuerySetting<NamedPullRequestQuery> querySetting) {
				getPullRequestQuerySettingManager().save((PullRequestQuerySetting) querySetting);
			}

			@Override
			protected void onSaveProjectQueries(ArrayList<NamedPullRequestQuery> projectQueries) {
				getProject().setSavedPullRequestQueries(projectQueries);
				OneDev.getInstance(ProjectManager.class).save(getProject());
			}

		});
		
		WebMarkupContainer others = new WebMarkupContainer("others") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(visitChildren(Component.class, new VisibleVisitor()) != null);
			}
			
		};
		
		add(others);
		
		others.add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) {
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				savedQueries.configure();
				setVisible(!savedQueries.isVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		Component querySave;
		others.add(querySave = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(StringUtils.isNotBlank(query));
				setVisible(SecurityUtils.getUser() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) {
					tag.put("disabled", "disabled");
					tag.put("title", "Input query to save");
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target)  {

					@Override
					protected Component newContent(String id) {
						return new SaveQueryPanel(id) {

							@Override
							protected void onSaveForMine(AjaxRequestTarget target, String name) {
								PullRequestQuerySetting setting = getProject().getPullRequestQuerySettingOfCurrentUser();
								NamedPullRequestQuery namedQuery = setting.getUserQuery(name);
								if (namedQuery == null) {
									namedQuery = new NamedPullRequestQuery(name, query);
									setting.getUserQueries().add(namedQuery);
								} else {
									namedQuery.setQuery(query);
								}
								getPullRequestQuerySettingManager().save(setting);
								target.add(savedQueries);
								close();
							}

							@Override
							protected void onSaveForAll(AjaxRequestTarget target, String name) {
								NamedPullRequestQuery namedQuery = getProject().getSavedPullRequestQuery(name);
								if (namedQuery == null) {
									namedQuery = new NamedPullRequestQuery(name, query);
									getProject().getSavedPullRequestQueries().add(namedQuery);
								} else {
									namedQuery.setQuery(query);
								}
								OneDev.getInstance(ProjectManager.class).save(getProject());
								target.add(savedQueries);
								close();
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								close();
							}
							
						};
					}
					
				};
			}		
			
		});
		
		TextField<String> input = new TextField<String>("input", Model.of(query));
		input.add(new PullRequestQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				query = input.getModelObject();
				if (SecurityUtils.getUser() != null)
					target.add(querySave);
			}
			
		});
		
		Form<?> form = new Form<Void>("query");
		form.add(input);
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				setResponsePage(PullRequestListPage.class, paramsOf(getProject(), query, 0));
			}
			
		});
		add(form);

		add(new BookmarkablePageLink<Void>("newRequest", NewPullRequestPage.class, NewPullRequestPage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canReadCode(getProject().getFacade()));
			}
			
		});		
		
		add(new NotificationPanel("feedback", this));
		
		List<IColumn<PullRequest, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}
			
		});
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Summary")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
				QueryPosition position = new QueryPosition(parsedQueryModel.getObject().toString(), (int)requestsTable.getItemCount(), 
						(int)requestsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
				
				Fragment fragment = new Fragment(componentId, "summaryFrag", PullRequestListPage.this);
				fragment.add(new Label("number", "#" + request.getNumber()));
				Link<Void> link = new BookmarkablePageLink<Void>("title", PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(rowModel.getObject(), position));
				link.add(new Label("label", request.getTitle()));
				fragment.add(link);
				fragment.add(new RequestStatusLabel("status", rowModel));
				UserIdent submitterIdent = UserIdent.of(UserFacade.of(request.getSubmitter()), request.getSubmitterName());
				fragment.add(new UserIdentPanel("submitter", submitterIdent, Mode.NAME));
				fragment.add(new Label("submitDate", DateUtils.formatAge(request.getSubmitDate())));
				fragment.add(new Label("comments", request.getCommentCount()));
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "summary";
			}
			
		});
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Source")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				if (rowModel.getObject().getSource() != null) {
					Fragment fragment = new Fragment(componentId, "sourceFrag", PullRequestListPage.this);
					fragment.add(new BranchLink("link", rowModel.getObject().getSource(), rowModel.getObject()));
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, "<i>Unknown</i>").setEscapeModelStrings(false));
				}
			}

			@Override
			public String getCssClass() {
				return "source expanded";
			}

		});
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Last Update")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(request.getUpdateDate())));
			}

			@Override
			public String getCssClass() {
				return "last-update expanded";
			}
			
		});
		
		SortableDataProvider<PullRequest, Void> dataProvider = new SortableDataProvider<PullRequest, Void>() {

			private Integer count;
			
			@Override
			public void detach() {
				count = null;
			}

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				return getPullRequestManager().query(getProject(), getLoginUser(), parsedQueryModel.getObject(), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				if (count == null) {
					PullRequestQuery parsedQuery = parsedQueryModel.getObject();
					if (parsedQuery != null)
						count = getPullRequestManager().count(getProject(), getLoginUser(), parsedQuery.getCriteria());
					else
						count = 0;
				}
				return count;
			}

			@Override
			public IModel<PullRequest> model(PullRequest object) {
				Long requestId = object.getId();
				return new LoadableDetachableModel<PullRequest>() {

					@Override
					protected PullRequest load() {
						return getPullRequestManager().load(requestId);
					}
					
				};
			}
			
		};
		
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), query, 0);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};

		add(requestsTable = new HistoryAwareDataTable<PullRequest, Void>("requests", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport) {

			@Override
			protected Item<PullRequest> newRowItem(String id, int index, IModel<PullRequest> model) {
				Item<PullRequest> item = super.newRowItem(id, index, model);
				PullRequest request = model.getObject();
				item.add(AttributeAppender.append("class", 
						request.isVisitedAfter(request.getUpdateDate())?"request":"request new"));
				return item;
			}
		});
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PullRequestListCssResourceReference()));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query, int page) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_CURRENT_PAGE, page);
		return params;
	}
	
}
