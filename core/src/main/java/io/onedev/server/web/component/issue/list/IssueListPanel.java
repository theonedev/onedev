package io.onedev.server.web.component.issue.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.datatable.HistoryAwareDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.issue.IssueStateLabel;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.user.ident.UserIdentPanel.Mode;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.savedquery.SavedQueriesClosed;
import io.onedev.server.web.page.project.savedquery.SavedQueriesOpened;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.VisibleVisitor;

@SuppressWarnings("serial")
public abstract class IssueListPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(IssueListPanel.class);
	
	private final IModel<Project> projectModel;
	
	private final String query;
	
	private IModel<IssueQuery> parsedQueryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			try {
				IssueQuery additionalQuery = IssueQuery.parse(getProject(), query, true);
				if (SecurityUtils.getUser() == null && additionalQuery.needsLogin()) { 
					error("Please login to perform this query");
				} else { 
					if (SecurityUtils.getUser() == null && getBaseQuery().needsLogin())
						error("Please login to show issues");
					else
						return IssueQuery.merge(getBaseQuery(), additionalQuery);
				}
			} catch (Exception e) {
				logger.error("Error parsing issue query: " + query, e);
				error(e.getMessage());
			}
			return null;
		}
		
	};
	
	private DataTable<Issue, Void> issuesTable;
	
	private SelectionColumn<Issue, Void> selectionColumn;
	
	private ModalLink batchEditSelected;
	
	private SortableDataProvider<Issue, Void> dataProvider;	
	
	public IssueListPanel(String id, Project project, @Nullable String query) {
		super(id);
		projectModel = new EntityModel<Project>(project);
		this.query = query;
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	protected void onDetach() {
		parsedQueryModel.detach();
		projectModel.detach();
		super.onDetach();
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

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
		input.add(new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}));
		input.add(new AjaxFormComponentUpdatingBehavior("input"){
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
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
		
		others.add(new ModalLink("listFields") {

			private Set<String> fieldSet;
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "listFieldsFrag", IssueListPanel.this);
				Form<?> form = new Form<Void>("form");
				fieldSet = getProject().getIssueSetting().getListFields(true);
				form.add(new StringMultiChoice("fields", new IModel<Collection<String>>() {

					@Override
					public void detach() {
					}

					@Override
					public Collection<String> getObject() {
						return fieldSet;
					}

					@Override
					public void setObject(Collection<String> object) {
						fieldSet = new HashSet<>(object);
					}
					
				}, new LoadableDetachableModel<Map<String, String>>() {

					@Override
					protected Map<String, String> load() {
						Map<String, String> choices = new LinkedHashMap<>();
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
						getProject().getIssueSetting().setListFields(fieldSet);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						onQueryUpdated(target, query);
					}
					
				});
				
				form.add(new AjaxLink<Void>("useDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
						getProject().getIssueSetting().setListFields(null);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						onQueryUpdated(target, query);
					}
					
				}.setVisible(getProject().getIssueSetting().getListFields(false) != null));
				
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
				setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
			}
			
		});

		String query;
		if (parsedQueryModel.getObject() != null)
			query = parsedQueryModel.getObject().toString();
		else
			query = null;
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject(), query)));
		
		others.add(batchEditSelected = new ModalLink("batchEditSelected") {

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
						onQueryUpdated(target, query);
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
				setVisible(selectionColumn != null && !selectionColumn.getSelections().isEmpty());
			}
			
		});

		batchEditSelected.setOutputMarkupPlaceholderTag(true);
		
		others.add(new ModalLink("batchEditAll") {

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
						onQueryUpdated(target, query);
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
				setVisible(SecurityUtils.canWriteCode(getProject().getFacade()) && issuesTable.getItemCount() != 0);
			}
			
		});
		
		dataProvider = new LoadableDetachableDataProvider<Issue, Void>() {

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				return getIssueManager().query(getProject(), SecurityUtils.getUser(), parsedQueryModel.getObject(), (int)first, (int)count).iterator();
			}

			@Override
			public long calcSize() {
				IssueQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null)
					return getIssueManager().count(getProject(), SecurityUtils.getUser(), parsedQuery.getCriteria());
				else
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
		
		body.add(new NotificationPanel("feedback", this));
		
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
		if (SecurityUtils.canWriteCode(getProject().getFacade())) {
			columns.add(selectionColumn = new SelectionColumn<Issue, Void>() {

				@Override
				protected void onSelectionChange(AjaxRequestTarget target) {
					target.add(batchEditSelected);
				}
				
			});
		}
		
		columns.add(new AbstractColumn<Issue, Void>(Model.of("Summary")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId,
					IModel<Issue> rowModel) {
				Issue issue = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "summaryFrag", IssueListPanel.this);
				fragment.add(new Label("number", "#" + issue.getNumber()));
				OddEvenItem<?> row = cellItem.findParent(OddEvenItem.class);
				QueryPosition position = new QueryPosition(parsedQueryModel.getObject().toString(), (int)issuesTable.getItemCount(), 
						(int)issuesTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex());
				Link<Void> link = new BookmarkablePageLink<Void>("title", IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(issue, position));
				link.add(new Label("label", issue.getTitle()));
				fragment.add(link);
				fragment.add(new WebMarkupContainer("copy").add(new CopyClipboardBehavior(Model.of("#" + issue.getNumber() + ": " + issue.getTitle()))));
				
				fragment.add(new IssueStateLabel("state", rowModel));
				
				UserIdent submitterIdent = UserIdent.of(UserFacade.of(issue.getSubmitter()), issue.getSubmitterName());
				fragment.add(new UserIdentPanel("submitter", submitterIdent, Mode.NAME));
				fragment.add(new Label("submitDate", DateUtils.formatAge(issue.getSubmitDate())));
				
				fragment.add(new Label("votes", issue.getVoteCount()));
				fragment.add(new Label("comments", issue.getCommentCount()));
				
				cellItem.add(fragment);
			}
			
			@Override
			public String getCssClass() {
				return "summary";
			}
			
		});
		for (String field: getGlobalIssueSetting().sortFieldNames(getProject().getIssueSetting().getListFields(true))) {
			columns.add(new AbstractColumn<Issue, Void>(Model.of(field)) {

				@Override
				public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId,
						IModel<Issue> rowModel) {
					cellItem.add(new FieldValuesPanel(componentId) {

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
						
					}.setRenderBodyOnly(true));
				}

				@Override
				public String getCssClass() {
					return "custom-field";
				}
				
			});
		}
		
		body.add(issuesTable = new HistoryAwareDataTable<Issue, Void>("issues", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()) {

			@Override
			protected Item<Issue> newRowItem(String id, int index, IModel<Issue> model) {
				Item<Issue> item = super.newRowItem(id, index, model);
				Issue issue = model.getObject();
				item.add(AttributeAppender.append("class", 
						issue.isVisitedAfter(issue.getUpdateDate())?"issue":"issue new"));
				return item;
			}
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListCssResourceReference()));
	}
	
}
