package io.onedev.server.web.component.build.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
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
import org.apache.wicket.feedback.FencedFeedbackPanel;
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

import com.google.common.collect.Sets;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalBuildSetting;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.OrEntityCriteria;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.BuildQueryLexer;
import io.onedev.server.search.entity.build.JobCriteria;
import io.onedev.server.search.entity.build.NumberCriteria;
import io.onedev.server.search.entity.build.VersionCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.BuildQueryBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.build.ParamValuesLabel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public abstract class BuildListPanel extends Panel {
	
	private final String query;
	
	private final int expectedCount;
	
	private IModel<BuildQuery> parsedQueryModel = new LoadableDetachableModel<BuildQuery>() {

		@Override
		protected BuildQuery load() {
			try {
				return BuildQuery.merge(getBaseQuery(), BuildQuery.parse(getProject(), query, true, true));
			} catch (OneException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, performing fuzzy query");
				try {
					EntityQuery.getProjectScopedNumber(getProject(), query);
					return BuildQuery.merge(getBaseQuery(), 
							new BuildQuery(new NumberCriteria(getProject(), query, BuildQueryLexer.Is)));
				} catch (Exception e2) {
					List<EntityCriteria<Build>> criterias = new ArrayList<>();
					criterias.add(new VersionCriteria("*" + query + "*"));
					criterias.add(new JobCriteria("*" + query + "*"));
					return BuildQuery.merge(getBaseQuery(), new BuildQuery(new OrEntityCriteria<Build>(criterias)));
				}
			}
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
	private Cursor getCursor(Item<ICellPopulator<Build>> cellItem) {
		if (getProject() != null) {
			OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
			return new Cursor(parsedQueryModel.getObject().toString(), (int)buildsTable.getItemCount(), 
					(int)buildsTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
		} else {
			return null;
		}
	}
	
	private GlobalBuildSetting getGlobalBuildSetting() {
		return OneDev.getInstance(SettingManager.class).getBuildSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
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
		
		add(new ModalLink("listParams") {

			private List<String> listParams;
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "listParamsFrag", BuildListPanel.this);
				Form<?> form = new Form<Void>("form");
				listParams = getListParams();
				form.add(new StringMultiChoice("params", new IModel<Collection<String>>() {

					@Override
					public void detach() {
					}

					@Override
					public Collection<String> getObject() {
						return listParams;
					}

					@Override
					public void setObject(Collection<String> object) {
						listParams = new ArrayList<>(object);
					}
					
				}, new LoadableDetachableModel<Map<String, String>>() {

					@Override
					protected Map<String, String> load() {
						Map<String, String> choices = new LinkedHashMap<>();
						for (String fieldName: OneDev.getInstance(BuildParamManager.class).getBuildParamNames(null))
							choices.put(fieldName, fieldName);
						return choices;
					}
					
				}));
				
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						modal.close();
						if (getProject() != null) {
							getProject().getBuildSetting().setListParams(listParams);
							OneDev.getInstance(ProjectManager.class).save(getProject());
						} else {
							getGlobalBuildSetting().setListParams(listParams);
							OneDev.getInstance(SettingManager.class).saveBuildSetting(getGlobalBuildSetting());
						}
						target.add(body);
					}
					
				});
				
				form.add(new AjaxLink<Void>("useDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
						getProject().getBuildSetting().setListParams(null);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(body);
					}
					
				}.setVisible(getProject() != null && getProject().getBuildSetting().getListParams(false) != null));
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});

				fragment.add(form);
				
				return fragment;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator() 
						|| getProject() != null && SecurityUtils.canManageBuilds(getProject()));
			}
			
		});		
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "query"));
		input.add(new BuildQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, true, true, true));
		
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
					target.add(saveQueryLink);
			}
			
		});
		
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
				try {
					return getBuildManager().query(getProject(), parsedQueryModel.getObject(), 
							(int)first, (int)count).iterator();
				} catch (OneException e) {
					error(e.getMessage());
					return new ArrayList<Build>().iterator();
				}
			}

			@Override
			public long calcSize() {
				BuildQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null) {
					try {
						return getBuildManager().count(getProject(), parsedQuery.getCriteria());
					} catch (OneException e) {
						error(e.getMessage());
					}
				} 
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
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Build, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Build")) {

			@Override
			public String getCssClass() {
				return "build";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId, IModel<Build> rowModel) {
				Fragment fragment = new Fragment(componentId, "buildFrag", BuildListPanel.this);
				Build build = rowModel.getObject();
				Long buildId = build.getId();
				
				Link<Void> link = new BookmarkablePageLink<Void>("link", BuildDashboardPage.class, 
						BuildDashboardPage.paramsOf(build, getCursor(cellItem)));
				link.add(new BuildStatusIcon("icon", new LoadableDetachableModel<Status>() {

					@Override
					protected Status load() {
						return getBuildManager().load(buildId).getStatus();
					}
					
				}));
				link.add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						Build build = rowModel.getObject();
						StringBuilder builder = new StringBuilder();
						if (getProject() == null)
							builder.append(build.getProject().getName());
						builder.append("#" + build.getNumber());
						if (build.getVersion() != null)
							builder.append(" (" + build.getVersion() + ")");
						return builder.toString();
					}
					
				}));
				fragment.add(link);
				fragment.add(newBuildObserver(buildId));
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Build, Void>(Model.of(Build.FIELD_JOB)) {

			@Override
			public String getCssClass() {
				return "job";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId,
					IModel<Build> rowModel) {
				Build build = rowModel.getObject();
				if (SecurityUtils.canReadCode(build.getProject())) {
					Fragment fragment = new Fragment(componentId, "jobFrag", BuildListPanel.this);
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
		
		columns.add(new AbstractColumn<Build, Void>(Model.of(Build.FIELD_COMMIT)) {

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

		for (String paramName: getListParams()) {
			columns.add(new AbstractColumn<Build, Void>(Model.of(paramName)) {

				@Override
				public String getCssClass() {
					return "param expanded";
				}

				@Override
				public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId, IModel<Build> rowModel) {
					Build build = rowModel.getObject();
					Input param = build.getParamInputs().get(paramName);
					if (param != null && build.isParamVisible(paramName))
						cellItem.add(new ParamValuesLabel(componentId, param));
					else
						cellItem.add(new Label(componentId, "<i>Unspecified</i>").setEscapeModelStrings(false));
				}
				
			});
		}	
		
		columns.add(new AbstractColumn<Build, Void>(Model.of("Last Update")) {

			@Override
			public String getCssClass() {
				return "date expanded";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Build>> cellItem, String componentId, IModel<Build> rowModel) {
				Build build = rowModel.getObject();
				Long buildId = build.getId();

				Fragment fragment = new Fragment(componentId, "dateFrag", BuildListPanel.this);
				fragment.add(new Label("name", new AbstractReadOnlyModel<String>() {

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
				fragment.add(new Label("date", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						return DateUtils.formatAge(rowModel.getObject().getStatusDate());
					}
					
				}));
				fragment.add(newBuildObserver(buildId));
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
			}
		});		
		
		body.add(buildsTable = new DefaultDataTable<Build, Void>("builds", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));
		
		setOutputMarkupId(true);
	}
	
	private List<String> getListParams() {
		if (getProject() != null)
			return getProject().getBuildSetting().getListParams(true);
		else
			return getGlobalBuildSetting().getListParams();
	}
	
	private WebSocketObserver newBuildObserver(Long buildId) {
		return new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
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
