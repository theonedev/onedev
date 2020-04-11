package io.onedev.server.web.component.issue.list;

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
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.NumberCriteria;
import io.onedev.server.search.entity.issue.TitleCriteria;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.IssueStateLabel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class IssueListPanel extends Panel {

	private final String query;
	
	private IModel<IssueQuery> parsedQueryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			try {
				return IssueQuery.merge(getBaseQuery(), IssueQuery.parse(getProject(), query, true, true, false, false, false));
			} catch (OneException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, performing fuzzy query");
				try {
					EntityQuery.getProjectScopedNumber(getProject(), query);
					return IssueQuery.merge(getBaseQuery(), 
							new IssueQuery(new NumberCriteria(getProject(), query, IssueQueryLexer.Is)));
				} catch (Exception e2) {
					return IssueQuery.merge(getBaseQuery(), new IssueQuery(new TitleCriteria("*" + query + "*")));
				}
			}
		}
		
	};
	
	private DataTable<Issue, Void> issuesTable;
	
	private SelectionColumn<Issue, Void> selectionColumn;
	
	private SortableDataProvider<Issue, Void> dataProvider;	
	
	public IssueListPanel(String id, @Nullable String query) {
		super(id);
		this.query = query;
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();

	protected IssueQuery getBaseQuery() {
		return new IssueQuery();
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
	
	private GlobalIssueSetting getGlobalIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
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
		input.add(new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, true, true, false, false, false));
		
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
		add(form);
		
		add(new ModalLink("listFields") {

			private List<String> listFields;
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "listFieldsFrag", IssueListPanel.this);
				Form<?> form = new Form<Void>("form");
				listFields = getListFields();
				form.add(new StringMultiChoice("fields", new IModel<Collection<String>>() {

					@Override
					public void detach() {
					}

					@Override
					public Collection<String> getObject() {
						return listFields;
					}

					@Override
					public void setObject(Collection<String> object) {
						listFields = new ArrayList<>(object);
					}
					
				}, new LoadableDetachableModel<Map<String, String>>() {

					@Override
					protected Map<String, String> load() {
						Map<String, String> choices = new LinkedHashMap<>();
						choices.put(Issue.FIELD_STATE, Issue.FIELD_STATE);
						for (String fieldName: getGlobalIssueSetting().getFieldNames())
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
							getProject().getIssueSetting().setListFields(listFields);
							OneDev.getInstance(ProjectManager.class).save(getProject());
						} else {
							getGlobalIssueSetting().setListFields(listFields);
							OneDev.getInstance(SettingManager.class).saveIssueSetting(getGlobalIssueSetting());
						}
						target.add(body);
					}
					
				});
				
				form.add(new AjaxLink<Void>("useDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
						getProject().getIssueSetting().setListFields(null);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(body);
					}
					
				}.setVisible(getProject() != null && getProject().getIssueSetting().getListFields(false) != null));
				
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
				setVisible(SecurityUtils.isAdministrator() || getProject() != null && SecurityUtils.canManageIssues(getProject()));
			}
			
		});

		String query;
		if (parsedQueryModel.getObject() != null)
			query = parsedQueryModel.getObject().toString();
		else
			query = null;
		
		if (getProject() != null) {
			add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject(), query)));
		} else {
			add(new DropdownLink("newIssue") {

				@Override
				public IModel<?> getBody() {
					return Model.of("<i class='fa fa-plus'></i> New <i class='fa fa-caret-down'></i>");
				}
				
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new ProjectSelector(id, new LoadableDetachableModel<Collection<Project>>() {

						@Override
						protected Collection<Project> load() {
							return OneDev.getInstance(ProjectManager.class).getPermittedProjects(new AccessProject());
						}
						
					}) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							setResponsePage(NewIssuePage.class, NewIssuePage.paramsOf(project, query));
						}

					};
				}
				
			}.setEscapeModelStrings(false));
		}
		
		ModalLink batchEditSelectedLink;
		add(batchEditSelectedLink = new ModalLink("batchEditSelected") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new BatchEditPanel(id) {

					@Override
					protected Project getProject() {
						return IssueListPanel.this.getProject();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onUpdated(AjaxRequestTarget target) {
						modal.close();
						target.add(body);
					}

					@Override
					protected Iterator<Issue> getIssueIterator() {
						List<Issue> issues = new ArrayList<>();
						for (IModel<Issue> each: selectionColumn.getSelections())
							issues.add(each.getObject());
						return issues.iterator();
					}

					@Override
					protected int getIssueCount() {
						return selectionColumn.getSelections().size();
					}

					@Override
					protected IssueQuery getIssueQuery() {
						return parsedQueryModel.getObject();
					}

				};
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null 
						&& selectionColumn != null 
						&& !selectionColumn.getSelections().isEmpty()
						&& SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		batchEditSelectedLink.setOutputMarkupPlaceholderTag(true);

		add(new ModalLink("batchEditAll") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new BatchEditPanel(id) {

					@Override
					protected Project getProject() {
						return IssueListPanel.this.getProject();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onUpdated(AjaxRequestTarget target) {
						modal.close();
						target.add(body);
					}

					@Override
					protected Iterator<? extends Issue> getIssueIterator() {
						return dataProvider.iterator(0, issuesTable.getItemCount());
					}

					@Override
					protected int getIssueCount() {
						return (int) issuesTable.getItemCount();
					}

					@Override
					protected IssueQuery getIssueQuery() {
						return parsedQueryModel.getObject();
					}

				};
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null 
						&& issuesTable.getItemCount() != 0 
						&& SecurityUtils.canManageIssues(getProject()));
			}
			
		});
		
		dataProvider = new LoadableDetachableDataProvider<Issue, Void>() {

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				try {
					return getIssueManager().query(getProject(), parsedQueryModel.getObject(), 
							(int)first, (int)count).iterator();
				} catch (OneException e) {
					error(e.getMessage());
					return new ArrayList<Issue>().iterator();
				}
			}

			@Override
			public long calcSize() {
				IssueQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null) {
					try {
						return getIssueManager().count(getProject(), parsedQuery.getCriteria());
					} catch (OneException e) {
						error(e.getMessage());
					}
				}
				return 0;
			}

			@Override
			public IModel<Issue> model(Issue object) {
				Long issueId = object.getId();
				return new LoadableDetachableModel<Issue>() {

					@Override
					protected Issue load() {
						return getIssueManager().load(issueId);
					}
					
				};
			}
			
		};
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Issue, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId, IModel<Issue> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}
			
		});
		if (getProject() != null && SecurityUtils.canManageIssues(getProject())) {
			columns.add(selectionColumn = new SelectionColumn<Issue, Void>() {

				@Override
				protected void onSelectionChange(AjaxRequestTarget target) {
					target.add(batchEditSelectedLink);
				}
				
			});
		}
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId,
					IModel<Issue> rowModel) {
				Issue issue = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "contentFrag", IssueListPanel.this);
				Item<?> row = cellItem.findParent(Item.class);
				Cursor cursor;
				if (getProject() != null) {
					cursor = new Cursor(parsedQueryModel.getObject().toString(), (int)issuesTable.getItemCount(), 
						(int)issuesTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
				} else {
					cursor = null;
				}
				
				String url = RequestCycle.get().urlFor(IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(issue, cursor)).toString();

				String label = "#" + issue.getNumber();
				if (getProject() == null)
					label = issue.getProject().getName() + label;
				fragment.add(new Label("number", "<a href='" + url + "'>" + label + "</a>")
						.setEscapeModelStrings(false));
				
				String transformed = new ReferenceTransformer(issue.getProject(), url).apply(issue.getTitle());
				fragment.add(new Label("title", transformed).setEscapeModelStrings(false));
				
				fragment.add(new WebMarkupContainer("copy").add(
						new CopyClipboardBehavior(Model.of("#" + issue.getNumber() + ": " + issue.getTitle()))));
				
				fragment.add(new Label("votes", issue.getVoteCount()));
				fragment.add(new Label("comments", issue.getCommentCount()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String field: getListFields()) {
					if (field.equals(Issue.FIELD_STATE)) {
						Fragment stateFragment = new Fragment(fieldsView.newChildId(), 
								"stateFrag", IssueListPanel.this);
						stateFragment.add(new IssueStateLabel("state", rowModel));
						fieldsView.add(stateFragment);
					} else {
						fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), Mode.AVATAR_AND_NAME) {

							@Override
							protected Issue getIssue() {
								return rowModel.getObject();
							}

							@Override
							protected Input getField() {
								Issue issue = rowModel.getObject();
								if (issue.isFieldVisible(field))
									return issue.getFieldInputs().get(field);
								else
									return null;
							}
							
						});
					}
				}	
				fragment.add(fieldsView);
				
				LastUpdate lastUpdate = issue.getLastUpdate();
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
			
		});
		
		body.add(issuesTable = new DataTable<Issue, Void>("issues", columns, dataProvider, WebConstants.PAGE_SIZE) {

			@Override
			protected Item<Issue> newRowItem(String id, int index, IModel<Issue> model) {
				Item<Issue> item = super.newRowItem(id, index, model);
				Issue issue = model.getObject();
				item.add(AttributeAppender.append("class", 
						issue.isVisitedAfter(issue.getLastUpdate().getDate())?"issue":"issue new"));
				return item;
			}
			
		});
		
		if (getPagingHistorySupport() != null)
			issuesTable.setCurrentPage(getPagingHistorySupport().getCurrentPage());
		issuesTable.addBottomToolbar(new NavigationToolbar(issuesTable) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new HistoryAwarePagingNavigator(navigatorId, table, getPagingHistorySupport());
			}
			
		});
		issuesTable.addBottomToolbar(new NoRecordsToolbar(issuesTable));
		
		setOutputMarkupId(true);
	}
	
	private List<String> getListFields() {
		if (getProject() != null)
			return getProject().getIssueSetting().getListFields(true);
		else
			return getGlobalIssueSetting().getListFields();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListCssResourceReference()));
	}
	
}
