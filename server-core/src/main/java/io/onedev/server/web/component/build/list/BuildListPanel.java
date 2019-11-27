package io.onedev.server.web.component.build.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.BuildQueryBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public abstract class BuildListPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(BuildListPanel.class);
	
	private final String query;
	
	private final int expectedCount;
	
	private IModel<BuildQuery> parsedQueryModel = new LoadableDetachableModel<BuildQuery>() {

		@Override
		protected BuildQuery load() {
			try {
				BuildQuery additionalQuery = BuildQuery.parse(getProject(), query);
				if (SecurityUtils.getUser() == null && additionalQuery.needsLogin()) { 
					error("Please login to perform this query");
				} else { 
					if (SecurityUtils.getUser() == null && getBaseQuery().needsLogin())
						error("Please login to show builds");
					else
						return BuildQuery.merge(getBaseQuery(), additionalQuery);
				}
			} catch (Exception e) {
				logger.error("Error parsing build query: " + query, e);
				error(e.getMessage());
			}
			return null;
		}
		
	};
	
	private DataTable<Build, Void> buildsTable;
	
	public BuildListPanel(String id, @Nullable String query, int expectedCount) {
		super(id);
		this.query = query;
		this.expectedCount = expectedCount;
	}
	
	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();

	protected BuildQuery getBaseQuery() {
		return new BuildQuery();
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

	@Nullable
	private QueryPosition getQueryPosition(Item<ICellPopulator<Build>> cellItem) {
		if (getProject() != null) {
			OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
			return new QueryPosition(parsedQueryModel.getObject().toString(), (int)buildsTable.getItemCount(), 
					(int)buildsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
		} else {
			return null;
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer others = new WebMarkupContainer("others");
		others.setOutputMarkupId(true);
		add(others);
		
		others.add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) {
					((SavedQueriesClosed) event.getPayload()).getHandler().add(others);
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
				target.add(others);
			}
			
		});
		
		others.add(new AjaxLink<Void>("saveQuery") {

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
			
		});
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "query"));
		input.add(new BuildQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
					target.add(others);
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
		
		SortableDataProvider<Build, Void> dataProvider = new LoadableDetachableDataProvider<Build, Void>() {

			@Override
			public Iterator<? extends Build> iterator(long first, long count) {
				return getBuildManager().query(getProject(), SecurityUtils.getUser(), 
						parsedQueryModel.getObject(), (int)first, (int)count).iterator();
			}

			@Override
			public long calcSize() {
				BuildQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null)
					return getBuildManager().count(getProject(), SecurityUtils.getUser(), parsedQuery.getCriteria());
				else
					return 0;
			}

			@Override
			public IModel<Build> model(Build object) {
				Long buildId = object.getId();
				return new LoadableDetachableModel<Build>() {

					@Override
					protected Build load() {
						return getBuildManager().load(buildId);
					}
					
				};
			}
			
		};
		
		if (expectedCount != 0 && expectedCount != dataProvider.size())
			warn("Some builds might be hidden due to permission policy");
		
		body.add(new NotificationPanel("feedback", this));
		
		List<IColumn<Build, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Build")) {

			@Override
			public String getCssClass() {
				return "build";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId, IModel<Build> rowModel) {
				Fragment fragment = new Fragment(componentId, "linkFrag", BuildListPanel.this);
				Build build = rowModel.getObject();
				Long buildId = build.getId();
				
				Link<Void> link = new BookmarkablePageLink<Void>("link", BuildDashboardPage.class, 
						BuildDashboardPage.paramsOf(build, getQueryPosition(cellItem)));
				link.add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						Build build = rowModel.getObject();
						StringBuilder builder = new StringBuilder("#" + build.getNumber());
						if (build.getVersion() != null)
							builder.append(" (" + build.getVersion() + ")");
						return builder.toString();
					}
					
				}) {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(newBuildObserver(buildId));
						setOutputMarkupId(true);
					}
					
				});
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Build, Void>(Model.of(BuildConstants.FIELD_STATUS)) {

			@Override
			public String getCssClass() {
				return "status";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId, IModel<Build> rowModel) {
				Build build = rowModel.getObject();
				Long buildId = build.getId();

				Fragment fragment = new Fragment(componentId, "statusFrag", BuildListPanel.this);
				fragment.add(new BuildStatusIcon("icon", new LoadableDetachableModel<Status>() {

					@Override
					protected Status load() {
						return getBuildManager().load(buildId).getStatus();
					}
					
				}) {
					
					@Override
					protected Collection<String> getWebSocketObservables() {
						return Sets.newHashSet(Build.getWebSocketObservable(buildId));
					}
					
				});
				
				Link<Void> link = new BookmarkablePageLink<Void>("link", BuildDashboardPage.class, 
						BuildDashboardPage.paramsOf(build, getQueryPosition(cellItem)));
				
				link.add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return rowModel.getObject().getStatus().getDisplayName();
					}
					
				}) {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(newBuildObserver(buildId));
						setOutputMarkupId(true);
					}
					
				});
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		if (getProject() == null) {
			columns.add(new AbstractColumn<Build, Void>(Model.of("Project")) {

				@Override
				public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
						IModel<Build> rowModel) {
					Build build = rowModel.getObject();
					Fragment fragment = new Fragment(componentId, "linkFrag", BuildListPanel.this);
					Link<Void> link = new BookmarkablePageLink<Void>("link", ProjectDashboardPage.class, 
							ProjectDashboardPage.paramsOf(build.getProject()));
					link.add(new Label("label", build.getProject()));
					fragment.add(link);
					cellItem.add(fragment);
				}
				
				@Override
				public String getCssClass() {
					return "project";
				}
				
			});
		}
		
		columns.add(new AbstractColumn<Build, Void>(Model.of(BuildConstants.FIELD_JOB)) {

			@Override
			public String getCssClass() {
				return "job";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				Build build = rowModel.getObject();
				if (SecurityUtils.canReadCode(build.getProject())) {
					Fragment fragment = new Fragment(componentId, "linkFrag", BuildListPanel.this);
					Link<Void> link = new JobDefLink("link", build.getCommitId(), build.getJobName()) {

						@Override
						protected Project getProject() {
							return rowModel.getObject().getProject();
						}
						
					};
					link.add(new Label("label", build.getJobName()));
					fragment.add(link);
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, build.getJobName()));
				}
			}
		});
		
		columns.add(new AbstractColumn<Build, Void>(Model.of(BuildConstants.FIELD_COMMIT)) {

			@Override
			public String getCssClass() {
				return "commit expanded";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				Build build = rowModel.getObject();
				if (SecurityUtils.canReadCode(build.getProject())) {
					Fragment fragment = new Fragment(componentId, "commitFrag", BuildListPanel.this);
					CommitDetailPage.State commitState = new CommitDetailPage.State();
					commitState.revision = build.getCommitHash();
					PageParameters params = CommitDetailPage.paramsOf(build.getProject(), commitState);
					Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
					fragment.add(hashLink);
					hashLink.add(new Label("hash", GitUtils.abbreviateSHA(build.getCommitHash())));
					fragment.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(build.getCommitHash()))));
					cellItem.add(fragment);
				} else {
					cellItem.add(new Label(componentId, GitUtils.abbreviateSHA(build.getCommitHash())));
				}
			}
		});

		columns.add(new AbstractColumn<Build, Void>(Model.of(BuildConstants.FIELD_SUBMIT_DATE)) {

			@Override
			public String getCssClass() {
				return "date expanded";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				cellItem.add(new Label(componentId, DateUtils.formatAge(rowModel.getObject().getSubmitDate())));
			}
		});
		
		body.add(buildsTable = new HistoryAwareDataTable<Build, Void>("builds", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));
	}
	
	private WebSocketObserver newBuildObserver(Long buildId) {
		return new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(Build.getWebSocketObservable(buildId));
			}
			
		};
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildListResourceReference()));
	}
	
}
