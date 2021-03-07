package io.onedev.server.web.component.issue.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
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
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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
import org.apache.wicket.request.cycle.RequestCycle;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.fieldspec.ChoiceField;
import io.onedev.server.model.support.issue.fieldspec.DateField;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.issue.fieldspec.NumberField;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.NumberCriteria;
import io.onedev.server.search.entity.issue.TitleCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class IssueListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			return parse(queryStringModel.getObject(), getBaseQuery());
		}
		
	};
	
	private DataTable<Issue, Void> issuesTable;
	
	private SelectionColumn<Issue, Void> selectionColumn;
	
	private SortableDataProvider<Issue, Void> dataProvider;	
	
	private WebMarkupContainer body;	
	
	private Component saveQueryLink;
	
	private TextField<String> queryInput;
	
	private boolean querySubmitted = true;
	
	public IssueListPanel(String id, IModel<String> queryModel) {
		super(id);
		this.queryStringModel = queryModel;
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	protected void onDetach() {
		queryStringModel.detach();
		queryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected abstract Project getProject();

	protected IssueQuery getBaseQuery() {
		return new IssueQuery();
	}
	
	@Nullable
	private IssueQuery parse(@Nullable String queryString, IssueQuery baseQuery) {
		try {
			return IssueQuery.merge(baseQuery, IssueQuery.parse(getProject(), queryString, true, true, false, false, false));
		} catch (ExplicitException e) {
			error(e.getMessage());
			return null;
		} catch (Exception e) {
			warn("Not a valid formal query, performing fuzzy query");
			try {
				EntityQuery.getProjectScopedNumber(getProject(), queryString);
				return IssueQuery.merge(baseQuery, 
						new IssueQuery(new NumberCriteria(getProject(), queryString, IssueQueryLexer.Is)));
			} catch (Exception e2) {
				return IssueQuery.merge(baseQuery, new IssueQuery(new TitleCriteria(queryString)));
			}
		}
	}

	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}
	
	private GlobalIssueSetting getGlobalIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private void doQuery(AjaxRequestTarget target) {
		issuesTable.setCurrentPage(0);
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
				List<String> orderFields = new ArrayList<>(Issue.ORDER_FIELDS.keySet());
				if (getProject() != null)
					orderFields.remove(Issue.NAME_PROJECT);
				for (FieldSpec field: getGlobalIssueSetting().getFieldSpecs()) {
					if (field instanceof NumberField || field instanceof ChoiceField || field instanceof DateField) 
						orderFields.add(field.getName());
				}
				
				return new OrderEditPanel(id, orderFields, new IModel<List<EntitySort>> () {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						IssueQuery query = parse(queryStringModel.getObject(), new IssueQuery());
						IssueListPanel.this.getFeedbackMessages().clear();
						if (query != null) 
							return query.getSorts();
						else
							return new ArrayList<>();
					}

					@Override
					public void setObject(List<EntitySort> object) {
						IssueQuery query = parse(queryStringModel.getObject(), new IssueQuery());
						IssueListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new IssueQuery();
						query.getSorts().clear();
						query.getSorts().addAll(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
						target.add(queryInput);
						doQuery(target);
					}
					
				});
			}
			
		});	
		
		queryInput = new TextField<String>("input", queryStringModel);
		queryInput.add(new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, true, true, false, false, false) {
			
			@Override
			protected void onInput(AjaxRequestTarget target, String inputContent) {
				IssueListPanel.this.getFeedbackMessages().clear();
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
				IssueListPanel.this.getFeedbackMessages().clear();
				doQuery(target);
			}
			
		});
		add(queryForm);
		
		if (getProject() == null) {
			add(new DropdownLink("newIssue") {

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new ProjectSelector(id, new LoadableDetachableModel<Collection<Project>>() {
	
						@Override
						protected Collection<Project> load() {
							List<Project> projects = new ArrayList<>(OneDev.getInstance(ProjectManager.class)
									.getPermittedProjects(new AccessProject()));
							
							Predicate<Project> issueManagementEnabledPredicate = item -> item.isIssueManagementEnabled();
							CollectionUtils.filter(projects, issueManagementEnabledPredicate);							
							
							Collections.sort(projects, new Comparator<Project>() {
	
								@Override
								public int compare(Project o1, Project o2) {
									return o1.getName().compareTo(o2.getName());
								}
								
							});
							return projects;
						}
						
					}) {
	
						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							setResponsePage(NewIssuePage.class, NewIssuePage.paramsOf(project));
						}
	
					}.add(AttributeAppender.append("class", "no-current"));
				}
			
			});	
		} else {
			add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())));
		}
		
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
						choices.put(Issue.NAME_STATE, Issue.NAME_STATE);
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
		
		ModalLink batchEditSelectedLink;
		add(batchEditSelectedLink = new ModalLink("batchEditSelected") {

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}

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
						return queryModel.getObject();
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
			protected String getModalCssClass() {
				return "modal-lg";
			}
			
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
						return queryModel.getObject();
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
					return getIssueManager().query(getProject(), queryModel.getObject(), 
							(int)first, (int)count, true).iterator();
				} catch (ExplicitException e) {
					error(e.getMessage());
					return new ArrayList<Issue>().iterator();
				}
			}

			@Override
			public long calcSize() {
				IssueQuery query = queryModel.getObject();
				if (query != null) {
					try {
						return getIssueManager().count(getProject(), query.getCriteria());
					} catch (ExplicitException e) {
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
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
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
				
				Cursor cursor = new Cursor(queryModel.getObject().toString(), (int)issuesTable.getItemCount(), 
						(int)issuesTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProject() != null);
				
				String label;
				if (getProject() == null)
					label = issue.getProject().getName() + "#" + issue.getNumber();
				else
					label = "#" + issue.getNumber();
				
				ActionablePageLink<Void> numberLink;
				fragment.add(numberLink = new ActionablePageLink<Void>("number", 
						IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue)) {

					@Override
					public IModel<?> getBody() {
						return Model.of(label);
					}

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						WebSession.get().setIssueCursor(cursor);
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Issue.class, redirectUrlAfterDelete);
					}
					
				});
				
				String url = RequestCycle.get().urlFor(IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(issue)).toString();

				String transformed = new ReferenceTransformer(issue.getProject(), url).apply(issue.getTitle());
				fragment.add(new Label("title", transformed) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format(""
								+ "$('#%s a:not(.embedded-reference)').click(function() {"
								+ "  $('#%s').click();"
								+ "  return false;"
								+ "});", 
								getMarkupId(), numberLink.getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				}.setEscapeModelStrings(false).setOutputMarkupId(true));
				
				fragment.add(new CopyToClipboardLink("copy", Model.of(issue.getNumberAndTitle())));
				
				fragment.add(new Label("votes", issue.getVoteCount()));
				fragment.add(new Label("comments", issue.getCommentCount()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String field: getListFields()) {
					if (field.equals(Issue.NAME_STATE)) {
						Fragment stateFragment = new Fragment(fieldsView.newChildId(), 
								"stateFrag", IssueListPanel.this);
						stateFragment.add(new IssueStateBadge("state", rowModel));
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
				fragment.add(new Label("date", DateUtils.formatAge(lastUpdate.getDate()))
					.add(new AttributeAppender("title", DateUtils.formatDateTime(lastUpdate.getDate()))));

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
				return new OnePagingNavigator(navigatorId, table, getPagingHistorySupport());
			}
			
		});
		issuesTable.addBottomToolbar(new NoRecordsToolbar(issuesTable));
		issuesTable.add(new NoRecordsBehavior());
		
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

