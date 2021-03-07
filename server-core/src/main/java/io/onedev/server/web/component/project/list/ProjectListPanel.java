package io.onedev.server.web.component.project.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.wicket.feedback.FencedFeedbackPanel;
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.OrEntityCriteria;
import io.onedev.server.search.entity.project.NameCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ProjectQueryBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {
	
	private final IModel<String> queryStringModel;
	
	private final int expectedCount;
	
	private final IModel<ProjectQuery> queryModel = new LoadableDetachableModel<ProjectQuery>() {

		@Override
		protected ProjectQuery load() {
			String queryString = queryStringModel.getObject();
			try {
				return ProjectQuery.parse(queryString);
			} catch (ExplicitException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, performing fuzzy query");
				List<EntityCriteria<Project>> criterias = new ArrayList<>();
				criterias.add(new NameCriteria("*" + queryString + "*"));
				return new ProjectQuery(new OrEntityCriteria<Project>(criterias));
			}
		}
		
	};
	
	private DataTable<Project, Void> dataTable;	
	
	private WebMarkupContainer body;
	
	private Component saveQueryLink;	
	
	private TextField<String> queryInput;
	
	private boolean querySubmitted = true;
	
	public ProjectListPanel(String id, IModel<String> queryModel, int expectedCount) {
		super(id);
		this.queryStringModel = queryModel;
		this.expectedCount = expectedCount;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}

	private void doQuery(AjaxRequestTarget target) {
		dataTable.setCurrentPage(0);
		target.add(body);
		querySubmitted = true;
		if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
			target.add(saveQueryLink);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) 
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
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

		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(querySubmitted && queryModel.getObject() != null);
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.append("class", "disabled", " ");
				if (!querySubmitted)
					tag.put("title", "Query not submitted");
				else if (queryModel.getObject() == null)
					tag.put("title", "Can not save malformed query");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject().toString());
			}		
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				List<String> orderFields = new ArrayList<>(Project.ORDER_FIELDS.keySet());
				return new OrderEditPanel(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						ProjectQuery query = queryModel.getObject();
						ProjectListPanel.this.getFeedbackMessages().clear();
						if (query != null) 
							return query.getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						ProjectQuery query = queryModel.getObject();
						ProjectListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new ProjectQuery();
						query.getSorts().clear();
						query.getSorts().addAll(object);
						queryModel.setObject(query);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.setOutputMarkupId(true);
		queryInput.add(new ProjectQueryBehavior() {

			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				ProjectListPanel.this.getFeedbackMessages().clear();
				querySubmitted = StringUtils.trimToEmpty(queryStringModel.getObject())
						.equals(StringUtils.trimToEmpty(inputContent));
				target.add(saveQueryLink);
			}
			
		});
		
		queryInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				doQuery(target);
			}
			
		});
		
		Form<?> queryForm = new Form<Void>("query");
		queryForm.add(queryInput);
		queryForm.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				doQuery(target);
			}
			
		});
		add(queryForm);
		
		add(new BookmarkablePageLink<Void>("addProject", NewProjectPage.class).setVisible(SecurityUtils.canCreateProjects()));
		
		SortableDataProvider<Project, Void> dataProvider = new LoadableDetachableDataProvider<Project, Void>() {

			@Override
			public Iterator<? extends Project> iterator(long first, long count) {
				try {
					return getProjectManager().query(queryModel.getObject(), (int)first, (int)count).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
					return new ArrayList<Project>().iterator();
				}
			}

			@Override
			public long calcSize() {
				ProjectQuery query = queryModel.getObject();
				if (query != null) {
					try {
						return getProjectManager().count(query.getCriteria());
					} catch (ExplicitException e) {
						error(e.getMessage());
					}
				}
				return 0;
			}

			@Override
			public IModel<Project> model(Project object) {
				Long projectId = object.getId();
				return new LoadableDetachableModel<Project>() {

					@Override
					protected Project load() {
						return getProjectManager().load(projectId);
					}
					
				};
			}
			
		};
		
		if (expectedCount != 0 && expectedCount != dataProvider.size())
			warn("Some projects might be hidden due to permission policy");
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Project, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Project")) {

			@Override
			public String getCssClass() {
				return "project";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", ProjectListPanel.this);
				Project project = rowModel.getObject();
				
				ActionablePageLink<Void> link = new ActionablePageLink<Void>("link", 
						ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Project.class, redirectUrlAfterDelete);
					}
					
				};
				
				link.add(new ProjectAvatar("avatar", project));
				link.add(new Label("name", project.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Last Update")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Project project = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(project.getUpdateDate()))
					.add(new AttributeAppender("title", DateUtils.formatDateTime(project.getUpdateDate()))));
			}

			@Override
			public String getCssClass() {
				return "d-none d-sm-table-cell";
			}
			
		});
		
		body.add(dataTable = new OneDataTable<Project, Void>("projects", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectCssResourceReference()));
	}
		
}
