package io.onedev.server.web.component.pullrequest.list;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.PullRequestQueryBehavior;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.pullrequest.summary.PullRequestSummaryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.VisibleVisitor;

@SuppressWarnings("serial")
public class PullRequestListPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(PullRequestListPanel.class);
	
	private final IModel<Project> projectModel;
	
	private final String query;
	
	private IModel<PullRequestQuery> parsedQueryModel = new LoadableDetachableModel<PullRequestQuery>() {

		@Override
		protected PullRequestQuery load() {
			try {
				PullRequestQuery parsedQuery = PullRequestQuery.parse(getProject(), query);
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
	
	private DataTable<PullRequest, Void> requestsTable;
	
	public PullRequestListPanel(String id, @Nullable Project project, @Nullable String query) {
		super(id);

		projectModel = project!=null?new EntityModel<Project>(project):Model.of((Project)null);
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
		projectModel.detach();
		super.onDetach();
	}
	
	@Nullable
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

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
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
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
				getQuerySaveSupport().onSaveQuery(target, query);
			}		
			
		});
		
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
				if (SecurityUtils.getUser() != null)
					target.add(querySave);
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
		add(form);

		if (getProject() != null) {
			add(new BookmarkablePageLink<Void>("newRequest", NewPullRequestPage.class, 
					NewPullRequestPage.paramsOf(getProject())) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canReadCode(getProject()));
				}
				
			});		
		} else {
			add(new WebMarkupContainer("newRequest").setVisible(false));
		}
		
		body.add(new NotificationPanel("feedback", this));
		
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
				cellItem.add(new PullRequestSummaryPanel(componentId, rowModel) {

					@Override
					protected QueryPosition getQueryPosition() {
						OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
						return new QueryPosition(parsedQueryModel.getObject().toString(), (int)requestsTable.getItemCount(), 
								(int)requestsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
					}
					
				});
			}

			@Override
			public String getCssClass() {
				return "summary";
			}
			
		});
		
		if (getProject() == null) {
			columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Target")) {
	
				@Override
				public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
					Fragment fragment = new Fragment(componentId, "branchFrag", PullRequestListPanel.this);
					fragment.add(new BranchLink("link", rowModel.getObject().getTarget(), rowModel.getObject()));
					cellItem.add(fragment);
				}
	
				@Override
				public String getCssClass() {
					return "target expanded";
				}
	
			});
		}
		
		columns.add(new AbstractColumn<PullRequest, Void>(Model.of("Source")) {

			@Override
			public void populateItem(Item<ICellPopulator<PullRequest>> cellItem, String componentId, IModel<PullRequest> rowModel) {
				if (rowModel.getObject().getSource() != null) {
					Fragment fragment = new Fragment(componentId, "branchFrag", PullRequestListPanel.this);
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
				return getPullRequestManager().query(getProject(), SecurityUtils.getUser(), 
						parsedQueryModel.getObject(), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				if (count == null) {
					PullRequestQuery parsedQuery = parsedQueryModel.getObject();
					if (parsedQuery != null)
						count = getPullRequestManager().count(getProject(), SecurityUtils.getUser(), parsedQuery.getCriteria());
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
		
		body.add(requestsTable = new HistoryAwareDataTable<PullRequest, Void>("requests", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()) {

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
	
}
