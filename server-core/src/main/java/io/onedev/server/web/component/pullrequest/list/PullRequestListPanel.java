package io.onedev.server.web.component.pullrequest.list;

import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.pullrequest.NumberCriteria;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer;
import io.onedev.server.search.entity.pullrequest.TitleCriteria;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.PullRequestQueryBehavior;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.pullrequest.RequestStatusLabel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class PullRequestListPanel extends Panel {

	private final String query;
	
	private IModel<PullRequestQuery> parsedQueryModel = new LoadableDetachableModel<PullRequestQuery>() {

		@Override
		protected PullRequestQuery load() {
			try {
				return PullRequestQuery.parse(getProject(), query);
			} catch (OneException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, performing fuzzy query");
				try {
					EntityQuery.getProjectScopedNumber(getProject(), query);
					return new PullRequestQuery(new NumberCriteria(getProject(), query, PullRequestQueryLexer.Is));
				} catch (Exception e2) {
					return new PullRequestQuery(new TitleCriteria("*" + query + "*"));
				}
			}
		}
		
	};
	
	private DataTable<PullRequest, Void> requestsTable;
	
	public PullRequestListPanel(String id, @Nullable String query) {
		super(id);
		this.query = query;
	}

	private PullRequestManager getPullRequestManager() {
		return OneDev.getInstance(PullRequestManager.class);		
	}
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	protected void onQueryUpdated(AjaxRequestTarget target, @Nullable String query) {
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("showSavedQueries") {

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
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));

		Component saveQueryLink;
		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(StringUtils.isNotBlank(query));
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
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
				getQuerySaveSupport().onSaveQuery(target, query);
			}		
			
		}.setOutputMarkupId(true));
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "query"));
		input.add(new PullRequestQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
					target.add(saveQueryLink);
			}
			
		});
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));

		Form<?> form = new Form<Void>("query");
		form.add(input);
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(body);
				onQueryUpdated(target, query);
			}
			
		});
		if (getProject() == null || SecurityUtils.canReadCode(getProject()))
			form.add(AttributeAppender.append("class", "can-create-pull-requests"));
		add(form);

		if (getProject() != null) {
			add(new BookmarkablePageLink<Void>("newRequest", NewPullRequestPage.class, 
					NewPullRequestPage.paramsOf(getProject())));		
		} else {
			add(new DropdownLink("newRequest") {

				@Override
				public IModel<?> getBody() {
					return Model.of("<i class='fa fa-plus'></i> New <i class='fa fa-caret-down'></i>");
				}
				
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new ProjectSelector(id, new LoadableDetachableModel<Collection<Project>>() {

						@Override
						protected Collection<Project> load() {
							return OneDev.getInstance(ProjectManager.class).getPermittedProjects(new ReadCode());
						}
						
					}) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							setResponsePage(NewPullRequestPage.class, NewPullRequestPage.paramsOf(project));
						}

					};
				}
				
			}.setEscapeModelStrings(false));
		}
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
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
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				PullRequest request = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "contentFrag", PullRequestListPanel.this);
				
				Cursor cursor;
				if (getProject() != null) {
					Item<?> row = cellItem.findParent(Item.class);
					cursor = new Cursor(parsedQueryModel.getObject().toString(), (int)requestsTable.getItemCount(), 
							(int)requestsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
				} else {
					cursor = null;
				}
				
				String url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, 
						PullRequestActivitiesPage.paramsOf(request, cursor)).toString();
				
				String label = "#" + request.getNumber();
				if (getProject() == null)
					label = request.getTargetProject().getName() + label;
				fragment.add(new Label("number", "<a href='" + url + "'>" + label + "</a>").setEscapeModelStrings(false));
				ReferenceTransformer transformer = new ReferenceTransformer(request.getTargetProject(), url);
				fragment.add(new Label("title", transformer.apply(request.getTitle())).setEscapeModelStrings(false));

				fragment.add(new Label("comments", request.getCommentCount()));
				
				fragment.add(new RequestStatusLabel("status", rowModel));
				
				fragment.add(new BranchLink("target", request.getTarget(), request));

				if (request.getSource() != null) { 
					fragment.add(new BranchLink("source", request.getSource(), request));
				} else { 
					fragment.add(new Label("source", "<i>unknown</i>") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("span");
						}
						
					}.setEscapeModelStrings(false));
				}
				
				LastUpdate lastUpdate = request.getLastUpdate();
				if (lastUpdate.getUser() != null || lastUpdate.getUserName() != null) {
					User user = User.from(lastUpdate.getUser(), lastUpdate.getUserName());
					fragment.add(new UserIdentPanel("user", user, Mode.NAME));
				} else {
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				}
				fragment.add(new Label("activity", lastUpdate.getActivity()));
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate())));
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "summary";
			}
			
		});
		
		SortableDataProvider<PullRequest, Void> dataProvider = new LoadableDetachableDataProvider<PullRequest, Void>() {

			@Override
			public Iterator<? extends PullRequest> iterator(long first, long count) {
				try {
					return getPullRequestManager().query(getProject(), parsedQueryModel.getObject(), 
							(int)first, (int)count).iterator();
				} catch (OneException e) {
					error(e.getMessage());
					return new ArrayList<PullRequest>().iterator();
				}
			}

			@Override
			public long calcSize() {
				PullRequestQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null) {
					try {
						return getPullRequestManager().count(getProject(), parsedQuery.getCriteria());
					} catch (OneException e) {
						error(e.getMessage());
					}
				}
				return 0;
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
		
		body.add(requestsTable = new DataTable<PullRequest, Void>("requests", columns, dataProvider, WebConstants.PAGE_SIZE) {

			@Override
			protected Item<PullRequest> newRowItem(String id, int index, IModel<PullRequest> model) {
				Item<PullRequest> item = super.newRowItem(id, index, model);
				PullRequest request = model.getObject();
				item.add(AttributeAppender.append("class", 
						request.isVisitedAfter(request.getLastUpdate().getDate())?"request":"request new"));
				return item;
			}
			
		});
		
		if (getPagingHistorySupport() != null)
			requestsTable.setCurrentPage(getPagingHistorySupport().getCurrentPage());
		requestsTable.addBottomToolbar(new NavigationToolbar(requestsTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new HistoryAwarePagingNavigator(navigatorId, table, getPagingHistorySupport());
			}
			
		});
		requestsTable.addBottomToolbar(new NoRecordsToolbar(requestsTable));
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PullRequestListCssResourceReference()));
	}
	
}
