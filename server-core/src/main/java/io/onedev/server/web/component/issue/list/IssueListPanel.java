package io.onedev.server.web.component.issue.list;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.search.entity.issue.IssueQuery.merge;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
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
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.dhatim.fastexcel.Workbook;

import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.imports.IssueImporterContribution;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.FuzzyCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.timetracking.TimeTrackingService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.LinkDescriptor;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.watch.WatchStatus;
import io.onedev.server.util.xstream.ObjectMap;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener.AttachMode;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.datatable.selectioncolumn.SelectionColumn;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.iteration.IterationCrumbPanel;
import io.onedev.server.web.component.issue.link.IssueLinksPanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.issue.progress.IssueProgressPanel;
import io.onedev.server.web.component.issue.progress.QueriedIssuesProgressPanel;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.component.project.selector.ProjectSelector;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.component.sortedit.SortEditPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.watchstatus.WatchStatusPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.imports.IssueImportPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.QuerySaveSupport;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;
import io.onedev.server.xodus.VisitInfoService;

public abstract class IssueListPanel extends Panel {

	private final IModel<String> queryStringModel;
	
	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected IssueQuery load() {
			return parse(queryStringModel.getObject(), getBaseQuery());
		}
		
	};
	
	private Component countLabel;
	
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
	
	private IssueService getIssueService() {
		return OneDev.getInstance(IssueService.class);
	}
	
	private IssueLinkService getIssueLinkService() {
		return OneDev.getInstance(IssueLinkService.class);
	}

	private TransactionService getTransactionService() {
		return OneDev.getInstance(TransactionService.class);
	}

	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
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
		IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
		if (getProject() != null)
			option.withCurrentProjectCriteria(true);
		IssueQuery parsedQuery;
		try {
			parsedQuery = IssueQuery.parse(getProject(), queryString, option, true);
		} catch (Exception e) {
			getFeedbackMessages().clear();
			if (e instanceof ExplicitException) {
				error(e.getMessage());
				return null;
			} else {
				info(_T("Performing fuzzy query. Enclosing search text with '~' to add more conditions, for instance: ~text to search~ and \"State\" is \"Open\""));
				parsedQuery = new IssueQuery(new FuzzyCriteria(queryString));
			}
		}
		return merge(baseQuery, parsedQuery);
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
		return OneDev.getInstance(SettingService.class).getIssueSetting();
	}
	
	private void doQuery(AjaxRequestTarget target) {
		issuesTable.setCurrentPage(0);
		target.add(countLabel);
		target.add(body);
		if (selectionColumn != null)
			selectionColumn.getSelections().clear();
		querySubmitted = true;
		if (SecurityUtils.getAuthUser() != null && getQuerySaveSupport() != null)
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
				setVisible(SecurityUtils.getAuthUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.append("class", "disabled", " ");
				if (!querySubmitted)
					tag.put("data-tippy-content", _T("Query not submitted"));
				else if (queryModel.getObject() == null)
					tag.put("data-tippy-content", _T("Can not save malformed query"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject().toString());
			}		
			
		}.setOutputMarkupPlaceholderTag(true));
		
		add(new DropdownLink("filter") {
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new IssueFilterPanel(id, new IModel<EntityQuery<Issue>>() {
					@Override
					public void detach() {
					}
					@Override
					public EntityQuery<Issue> getObject() {
						var query = parse(queryStringModel.getObject(), new IssueQuery());
						return query!=null? query : new IssueQuery();
					}
					@Override
					public void setObject(EntityQuery<Issue> object) {
						IssueListPanel.this.getFeedbackMessages().clear();
						queryStringModel.setObject(object.toString());
						var target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}
					
				}) {
					@Override
					protected Project getProject() {
						return IssueListPanel.this.getProject();
					}
				};
			}
		});
		add(new DropdownLink("orderBy") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Map<String, Direction> sortFields = new LinkedHashMap<>();
				for (var entry: Issue.SORT_FIELDS.entrySet())
					sortFields.put(entry.getKey(), entry.getValue().getDefaultDirection());
				if (getProject() != null)
					sortFields.remove(Issue.NAME_PROJECT);
				for (FieldSpec field: getGlobalIssueSetting().getFieldSpecs()) {
					if (field instanceof IntegerField || field instanceof ChoiceField || field instanceof DateField) 
						sortFields.put(field.getName(), ASCENDING);
				}
				
				return new SortEditPanel<Issue>(id, sortFields, new IModel<>() {

					@Override
					public void detach() {
					}

					@Override
					public List<EntitySort> getObject() {
						var query = parse(queryStringModel.getObject(), new IssueQuery());
						return query!=null? query.getSorts() : new ArrayList<>();
					}
 
					@Override
					public void setObject(List<EntitySort> object) {
						var query = parse(queryStringModel.getObject(), new IssueQuery());
						IssueListPanel.this.getFeedbackMessages().clear();
						if (query == null)
							query = new IssueQuery();
						query.setSorts(object);
						queryStringModel.setObject(query.toString());
						AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
						target.add(queryInput);
						doQuery(target);
					}

				});
			}
			
		});	
		
		add(new MenuLink("import") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				Collection<IssueImporter> importers = new ArrayList<>();
				
				List<IssueImporterContribution> contributions = 
						new ArrayList<>(OneDev.getExtensions(IssueImporterContribution.class));
				Collections.sort(contributions, comparingInt(IssueImporterContribution::getOrder));
				
				for (IssueImporterContribution contribution: contributions)
					importers.addAll(contribution.getImporters());
				
				List<MenuItem> menuItems = new ArrayList<>();
				for (IssueImporter importer: importers) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return MessageFormat.format(_T("From {0}"), importer.getName());
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new BookmarkablePageLink<Void>(id, IssueImportPage.class, 
									IssueImportPage.paramsOf(getProject(), importer.getName()));
						}
						
					});
				}
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (getPage() instanceof ProjectIssueListPage) {
					ProjectIssueListPage page = (ProjectIssueListPage) getPage();
					setVisible(SecurityUtils.canManageIssues(page.getProject()));
				} else {
					setVisible(false);
				}
			}
			
		});

		var extraActionsView = new RepeatingView("extraActions");
		add(extraActionsView);
		for (var renderer: OneDev.getExtensions(IssueListActionRenderer.class))
			extraActionsView.add(renderer.render(extraActionsView.newChildId()));
		
		queryInput = new TextField<String>("input", queryStringModel);
		
		IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
		if (getProject() != null)
			option.withCurrentProjectCriteria(true);
		
		queryInput.add(new IssueQueryBehavior(new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}, option) {
			
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
					return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {
	
						@Override
						protected List<Project> load() {
							List<Project> projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(new AccessProject()));
							
							ProjectCache cache = getProjectService().cloneCache();
							CollectionUtils.filter(projects, new Predicate<Project>() {

								@Override
								public boolean evaluate(Project object) {
									return cache.get(object.getId()).isIssueManagement();
								}
								
							});							
							projects.sort(cache.comparingPath());
							return projects;
						}
						
					}) {
						
						@Override
						protected String getTitle() {
							return _T("Select Project");
						}

						@Override
						protected void onSelect(AjaxRequestTarget target, Project project) {
							PageParameters params = NewIssuePage.paramsOf(project, getBaseQuery().toString());
							setResponsePage(NewIssuePage.class, params);
						}
	
					}.add(AttributeAppender.append("class", "no-current"));
				}
			
			});	
		} else {
			PageParameters params = NewIssuePage.paramsOf(getProject(), getBaseQuery().toString());
			add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, params));
		}
		
		add(new ModalLink("fieldsAndLinks") {

			private String getAuditContent(Project project) {
				var listFields = project.getIssueSetting().getListFields();
				if (listFields == null)
					listFields = getGlobalIssueSetting().getListFields();
				var listLinks = project.getIssueSetting().getListLinks();
				if (listLinks == null)
					listLinks = getGlobalIssueSetting().getListLinks();
				var auditData = new ObjectMap();
				auditData.put("listFields", listFields);
				auditData.put("listLinks", listLinks);
				return VersionedXmlDoc.fromBean(auditData).toXML();
			}

			private String getAuditContent() {
				var auditData = new ObjectMap();
				auditData.put("listFields", getGlobalIssueSetting().getListFields());
				auditData.put("listLinks", getGlobalIssueSetting().getListLinks());
				return VersionedXmlDoc.fromBean(auditData).toXML();
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldsAndLinksFrag", IssueListPanel.this);
				Form<?> form = new Form<Void>("form");
				
				FieldsAndLinksBean bean = new FieldsAndLinksBean();
				
				bean.setFields(getListFields());
				bean.setLinks(getListLinks());
				
				form.add(BeanContext.edit("editor", bean));
				
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
							var oldAuditContent = getAuditContent(getProject());
							getProject().getIssueSetting().setListFields(bean.getFields());
							getProject().getIssueSetting().setListLinks(bean.getLinks());
							var newAuditContent = getAuditContent(getProject());
							getProjectService().update(getProject());
							getAuditService().audit(getProject(), "changed display fields/links of issue list", oldAuditContent, newAuditContent);
						} else {		
							var oldAuditContent = getAuditContent();					
							getGlobalIssueSetting().setListFields(bean.getFields());
							getGlobalIssueSetting().setListLinks(bean.getLinks());
							var newAuditContent = getAuditContent();
							OneDev.getInstance(SettingService.class).saveIssueSetting(getGlobalIssueSetting());
							getAuditService().audit(null, "changed display fields/links of issue list", oldAuditContent, newAuditContent);
						}
						target.add(body);
						onDisplayFieldsAndLinksUpdated(target);
					}
					
				});
				
				form.add(new AjaxLink<Void>("useDefault") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
						var oldAuditContent = getAuditContent();
						getProject().getIssueSetting().setListFields(null);
						getProject().getIssueSetting().setListLinks(null);
						var newAuditContent = getAuditContent();
						getProjectService().update(getProject());
						getAuditService().audit(getProject(), "changed display fields/links of issue list", oldAuditContent, newAuditContent);
						target.add(body);
						onDisplayFieldsAndLinksUpdated(target);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getProject() != null 
								&& getProject().getIssueSetting().getListFields() != null
								&& getProject().getIssueSetting().getListLinks() != null);
					}
					
				});
				
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
		
		add(new ResourceLink<Void>("exportXlsx", new AbstractResource() {

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse response = new ResourceResponse();
				response.setContentType(MimeTypes.OCTET_STREAM);
				response.disableCaching();
				response.setFileName("issues.xlsx");
				response.setWriteCallback(new WriteCallback() {
		
					@Override
					public void writeData(Attributes attributes) {
						var os = attributes.getResponse().getOutputStream();
						var version = StringUtils.substringBeforeLast(OneDev.getInstance().getVersion(), ".");
						if (version.startsWith("v"))
							version = version.substring(1);
							
						try (var workBook = new Workbook(os, "OneDev", version)) {
							var worksheet = workBook.newWorksheet(_T("Issues"));
							
							var colIndex = 0;
							worksheet.value(0, colIndex++, _T("issue:Number"));
							worksheet.value(0, colIndex++, _T("Title"));
							
							for (String field: getListFields()) {
								if (field.equals(Issue.NAME_STATE)) {
									worksheet.value(0, colIndex++, _T("State"));
								} else if (field.equals(IssueSchedule.NAME_ITERATION)) {
									worksheet.value(0, colIndex++, _T("Iteration"));
								} else {
									worksheet.value(0, colIndex++, field);
								}
							}
							
							var withTimeTracking = false;
							var issues = new ArrayList<Issue>();
							for (@SuppressWarnings("unchecked") var it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); ) {
								var issue = it.next();
								if (issue.getProject().isTimeTracking() && issue.getTotalEstimatedTime() != 0 && WicketUtils.isSubscriptionActive()) {
									withTimeTracking = true;
								}
								issues.add(issue);
							}
							if (withTimeTracking) {
								worksheet.value(0, colIndex++, _T("Estimated Time"));
								worksheet.value(0, colIndex++, _T("Spent Time"));
							}
				
							var rowIndex = 1;
							var timeTrackingSetting = getGlobalIssueSetting().getTimeTrackingSetting();
							for (var issue: issues) {
								colIndex = 0;
								
								worksheet.value(rowIndex, colIndex++, issue.getReference().toString(getProject()));
								worksheet.value(rowIndex, colIndex++, issue.getTitle());
								
								for (String field: getListFields()) {
									if (field.equals(Issue.NAME_STATE)) {
										worksheet.value(rowIndex, colIndex, issue.getState());
									} else if (field.equals(IssueSchedule.NAME_ITERATION)) {
										var iterations = issue.getIterations().stream().map(Iteration::getName).collect(joining(", "));
										worksheet.value(rowIndex, colIndex, iterations);
									} else {
										var input = issue.getFieldInputs().get(field);
										if (input != null) 
											worksheet.value(rowIndex, colIndex, input.getValues().stream().collect(joining(", ")));
									}
									colIndex++;
								}
								if (withTimeTracking) {
									if (issue.getProject().isTimeTracking() && issue.getTotalEstimatedTime() != 0) {
										worksheet.value(rowIndex, colIndex++, timeTrackingSetting.formatWorkingPeriod(issue.getTotalEstimatedTime(), true));
										worksheet.value(rowIndex, colIndex++, timeTrackingSetting.formatWorkingPeriod(issue.getTotalSpentTime(), true));
									} else {
										colIndex++;
										colIndex++;
									}
								} 
								rowIndex++;
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}						
					}
				});
		
				return response;
			}

		}));
		add(new ResourceLink<Void>("exportCsv", new AbstractResource() {

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse response = new ResourceResponse();
				response.setContentType(MimeTypes.OCTET_STREAM);
				response.disableCaching();
				response.setFileName("issues.csv");
				response.setWriteCallback(new WriteCallback() {
		
					@Override
					public void writeData(Attributes attributes) {
						var os = attributes.getResponse().getOutputStream();
						try (var printer = new CSVPrinter(new OutputStreamWriter(os), CSVFormat.DEFAULT)) {
							var headers = new ArrayList<String>();
							headers.add(_T("issue:Number"));
							headers.add(_T("Title"));
							for (String field: getListFields()) {
								if (field.equals(Issue.NAME_STATE)) {
									headers.add(_T("State"));
								} else if (field.equals(IssueSchedule.NAME_ITERATION)) {
									headers.add(_T("Iteration"));
								} else {
									headers.add(field);
								}
							}
							var withTimeTracking = false;
							var issues = new ArrayList<Issue>();
							for (@SuppressWarnings("unchecked") var it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); ) {
								var issue = it.next();
								if (issue.getProject().isTimeTracking() && issue.getTotalEstimatedTime() != 0 && WicketUtils.isSubscriptionActive()) {
									withTimeTracking = true;
								}
								issues.add(issue);
							}
							if (withTimeTracking) {
								headers.add(_T("Estimated Time"));
								headers.add(_T("Spent Time"));
							}
							
							printer.printRecord(headers);
							
							var timeTrackingSetting = getGlobalIssueSetting().getTimeTrackingSetting();
							for (var issue: issues) {
								var row = new ArrayList<String>();
								
								row.add(issue.getReference().toString(getProject()));
								row.add(issue.getTitle());
								
								for (String field: getListFields()) {
									if (field.equals(Issue.NAME_STATE)) {
										row.add(issue.getState());
									} else if (field.equals(IssueSchedule.NAME_ITERATION)) {
										row.add(issue.getIterations().stream().map(Iteration::getName).collect(joining(", ")));
									} else {
										var input = issue.getFieldInputs().get(field);
										if (input != null) 
											row.add(input.getValues().stream().collect(joining(", ")));
										else
											row.add("");
									}
								}
								
								if (withTimeTracking) {
									if (issue.getProject().isTimeTracking() && issue.getTotalEstimatedTime() != 0) {
										row.add(timeTrackingSetting.formatWorkingPeriod(issue.getTotalEstimatedTime(), true));
										row.add(timeTrackingSetting.formatWorkingPeriod(issue.getTotalSpentTime(), true));
									} else {
										row.add("");
										row.add("");
									}
								} 
								
								printer.printRecord(row);
							}					
							printer.flush();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}						
					}

				});
				return response;
			}

		}));
		add(new MenuLink("operations") {

			private String getQueryAfterCopyOrMove() {
				EntitySort sort = new EntitySort();
				sort.setField(Issue.NAME_NUMBER);
				sort.setDirection(Direction.DESCENDING);
				IssueQuery query = new IssueQuery(null, newArrayList(sort));
				return query.toString();
			}
			
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();

				if (getProject() != null && getProject().isTimeTracking() 
						&& SecurityUtils.canManageIssues(getProject()) 
						&& WicketUtils.isSubscriptionActive()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Sync Timing of Selected Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select issues to sync estimated/spent time"));
									}
								}

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									var issueIds = selectionColumn.getSelections().stream()
											.map(it->it.getObject()).map(Issue::getId).collect(toList());
									getTimeTrackingService().requestToSyncTimes(issueIds);
									selectionColumn.getSelections().clear();
									Session.get().success(_T("Requested to sync estimated/spent time"));
								}

							};
						}

					});
				}
				
				if (getProject() != null && SecurityUtils.canManageIssues(getProject())) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Batch Edit Selected Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new ModalLink(id) {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									dropdown.close();

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
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
											onBatchUpdated(target);
										}

										@Override
										protected Iterator<Issue> getIssueIterator() {
											List<Issue> issues = new ArrayList<>();
											for (IModel<Issue> each : selectionColumn.getSelections())
												issues.add(each.getObject());
											return issues.iterator();
										}

										@Override
										protected int getIssueCount() {
											return selectionColumn.getSelections().size();
										}

										@Override
										protected IssueQuery getIssueQuery() {
											if (queryModel.getObject() != null)
												return (IssueQuery) queryModel.getObject();
											else
												return null;
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select issues to edit"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Move Selected Issues To...");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {

										@Override
										protected List<Project> load() {
											return getTargetProjects(true);
										}

									}) {

										@Override
										protected void onSelect(AjaxRequestTarget target, Project project) {
											dropdown.close();
											dropdown2.close();

											Long projectId = project.getId();
											new ConfirmModalPanel(target) {

												private Project getTargetProject() {
													return OneDev.getInstance(ProjectService.class).load(projectId);
												}

												@Override
												protected void onConfirm(AjaxRequestTarget target) {
													Collection<Issue> issues = new ArrayList<>();
													for (IModel<Issue> each : selectionColumn.getSelections())
														issues.add(each.getObject());
													var user = SecurityUtils.getUser();
													getIssueService().move(user, issues, getProject(), getTargetProject());
													setResponsePage(ProjectIssueListPage.class,
															ProjectIssueListPage.paramsOf(getTargetProject(), getQueryAfterCopyOrMove(), 0));
													Session.get().success(_T("Issues moved"));
												}

												@Override
												protected String getConfirmMessage() {
													return MessageFormat.format(_T("Type <code>yes</code> below to move selected issues to project \"{0}\""), getTargetProject());
												}

												@Override
												protected String getConfirmInput() {
													return "yes";
												}

											};
										}

									}.add(AttributeAppender.append("class", "no-current"));
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select issues to move"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Copy Selected Issues To...");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {

										@Override
										protected List<Project> load() {
											return getTargetProjects(false);
										}

									}) {

										@Override
										protected void onSelect(AjaxRequestTarget target, Project project) {
											dropdown.close();
											dropdown2.close();

											Long projectId = project.getId();
											new ConfirmModalPanel(target) {

												private Project getTargetProject() {
													return OneDev.getInstance(ProjectService.class).load(projectId);
												}

												@Override
												protected void onConfirm(AjaxRequestTarget target) {
													Collection<Issue> issues = new ArrayList<>();
													for (IModel<Issue> each : selectionColumn.getSelections())
														issues.add(each.getObject());
													getIssueService().copy(issues, getProject(), getTargetProject());
													setResponsePage(ProjectIssueListPage.class,
															ProjectIssueListPage.paramsOf(getTargetProject(), getQueryAfterCopyOrMove(), 0));
													Session.get().success(_T("Issues copied"));
												}

												@Override
												protected String getConfirmMessage() {
													return MessageFormat.format(_T("Type <code>yes</code> below to copy selected issues to project \"{0}\""), getTargetProject());
												}

												@Override
												protected String getConfirmInput() {
													return "yes";
												}

											};
										}

									}.add(AttributeAppender.append("class", "no-current"));
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select issues to copy"));
									}
								}

							};
						}

					});
					
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Delete Selected Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(()-> {
												Collection<Issue> issues = new ArrayList<>();
												for (IModel<Issue> each : selectionColumn.getSelections())
													issues.add(each.getObject());
												getIssueService().delete(issues, getProject());
												for (var issue: issues) {
													var oldAuditContent = VersionedXmlDoc.fromBean(issue).toXML();
													getAuditService().audit(issue.getProject(), "deleted issue \"" + issue.getReference().toString(issue.getProject()) + "\"", oldAuditContent, null);
												}													
											});
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
											onBatchDeleted(target);
										}

										@Override
										protected String getConfirmMessage() {
											return _T("Type <code>yes</code> below to delete selected issues");
										}

										@Override
										protected String getConfirmInput() {
											return "yes";
										}

									};

								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(!selectionColumn.getSelections().isEmpty());
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("Please select issues to delete"));
									}
								}

							};
						}

					});
				}

				if (getProject() != null && getProject().isTimeTracking() 
						&& SecurityUtils.canManageIssues(getProject())
						&& WicketUtils.isSubscriptionActive()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Sync Timing of All Queried Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									Collection<Long> issueIds = new ArrayList<>();
									for (@SuppressWarnings("unchecked")
									Iterator<Issue> it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); )
										issueIds.add(it.next().getId());
									getTimeTrackingService().requestToSyncTimes(issueIds);
									Session.get().success(_T("Requested to sync estimated/spent time"));
								}
								
								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(issuesTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No issues to sync estimated/spent time"));
									}
								}

							};
						}

					});
				}
				
				if (getProject() != null && SecurityUtils.canManageIssues(getProject())) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Batch Edit All Queried Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new ModalLink(id) {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									dropdown.close();

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
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
											onBatchUpdated(target);
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
											if (queryModel.getObject() != null)
												return queryModel.getObject();
											else
												return null;
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(issuesTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No issues to edit"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Move All Queried Issues To...");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {

										@Override
										protected List<Project> load() {
											return getTargetProjects(true);
										}

									}) {

										@SuppressWarnings("unchecked")
										@Override
										protected void onSelect(AjaxRequestTarget target, Project project) {
											dropdown.close();
											dropdown2.close();

											Long projectId = project.getId();
											new ConfirmModalPanel(target) {

												private Project getTargetProject() {
													return OneDev.getInstance(ProjectService.class).load(projectId);
												}

												@Override
												protected void onConfirm(AjaxRequestTarget target) {
													Collection<Issue> issues = new ArrayList<>();
													for (Iterator<Issue> it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); ) {
														issues.add(it.next());
													}
													var user = SecurityUtils.getUser();
													OneDev.getInstance(IssueService.class).move(user, issues, getProject(), getTargetProject());
													setResponsePage(ProjectIssueListPage.class,
															ProjectIssueListPage.paramsOf(getTargetProject(), getQueryAfterCopyOrMove(), 0));
													Session.get().success(_T("Issues moved"));
												}

												@Override
												protected String getConfirmMessage() {
													return MessageFormat.format(_T("Type <code>yes</code> below to move all queried issues to project \"{0}\""), getTargetProject());
												}

												@Override
												protected String getConfirmInput() {
													return "yes";
												}

											};
										}

									}.add(AttributeAppender.append("class", "no-current"));
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(issuesTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No issues to move"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Copy All Queried Issues To...");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new ProjectSelector(id, new LoadableDetachableModel<List<Project>>() {

										@Override
										protected List<Project> load() {
											return getTargetProjects(false);
										}

									}) {

										@SuppressWarnings("unchecked")
										@Override
										protected void onSelect(AjaxRequestTarget target, Project project) {
											dropdown.close();
											dropdown2.close();

											Long projectId = project.getId();
											new ConfirmModalPanel(target) {

												private Project getTargetProject() {
													return OneDev.getInstance(ProjectService.class).load(projectId);
												}

												@Override
												protected void onConfirm(AjaxRequestTarget target) {
													Collection<Issue> issues = new ArrayList<>();
													for (Iterator<Issue> it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); ) {
														issues.add(it.next());
													}
													OneDev.getInstance(IssueService.class).copy(issues, getProject(), getTargetProject());
													setResponsePage(ProjectIssueListPage.class,
															ProjectIssueListPage.paramsOf(getTargetProject(), getQueryAfterCopyOrMove(), 0));
													Session.get().success(_T("Issues copied"));
												}

												@Override
												protected String getConfirmMessage() {
													return MessageFormat.format(_T("Type <code>yes</code> below to copy all queried issues to project \"{0}\""), getTargetProject());
												}

												@Override
												protected String getConfirmInput() {
													return "yes";
												}

											};
										}

									}.add(AttributeAppender.append("class", "no-current"));
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(issuesTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No issues to copy"));
									}
								}

							};
						}

					});

					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Delete All Queried Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@SuppressWarnings("unchecked")
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();

									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											getTransactionService().run(()-> {
												Collection<Issue> issues = new ArrayList<>();
												for (Iterator<Issue> it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); )
													issues.add(it.next());
												getIssueService().delete(issues, getProject());
												for (var issue: issues) {
													var oldAuditContent = VersionedXmlDoc.fromBean(issue).toXML();
													getAuditService().audit(issue.getProject(), "deleted issue \"" + issue.getReference().toString(issue.getProject()) + "\"", oldAuditContent, null);
												}													
											});
											dataProvider.detach();
											selectionColumn.getSelections().clear();
											target.add(countLabel);
											target.add(body);
											onBatchDeleted(target);
										}

										@Override
										protected String getConfirmMessage() {
											return _T("Type <code>yes</code> below to delete all queried issues");
										}

										@Override
										protected String getConfirmInput() {
											return "yes";
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(issuesTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No issues to delete"));
									}
								}

							};
						}

					});
				}

				if (!SecurityUtils.getAuthUser().isServiceAccount()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return _T("Watch/Unwatch All Queried Issues");
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new DropdownLink(id) {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown2) {
									return new WatchStatusPanel(id) {

										@Override
										protected WatchStatus getWatchStatus() {
											return null;
										}

										@Override
										protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
											dropdown.close();
											dropdown2.close();

											Collection<Issue> issues = new ArrayList<>();
											for (@SuppressWarnings("unchecked") var it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); )
												issues.add(it.next());
											getWatchService().setWatchStatus(SecurityUtils.getAuthUser(), issues, watchStatus);
											Session.get().success(_T("Watch status changed"));
										}

									};
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setEnabled(issuesTable.getItemCount() != 0);
								}

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									configure();
									if (!isEnabled()) {
										tag.put("disabled", "disabled");
										tag.put("data-tippy-content", _T("No issues to watch/unwatch"));
									}
								}
							};
						}

					});
				}

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Set All Queried Issues as Read");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(issuesTable.getItemCount() != 0);
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No issues to set as read"));
								}
							}

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								var visitInfoService = OneDev.getInstance(VisitInfoService.class);
								for (@SuppressWarnings("unchecked") var it = (Iterator<Issue>) dataProvider.iterator(0, issuesTable.getItemCount()); it.hasNext(); )
									visitInfoService.visitIssue(SecurityUtils.getAuthUser(), it.next());
								target.add(body);
							}

						};
					}

				});

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return _T("Export All Queried Issues To...");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new MenuLink(id) {

							@Override
							protected void onConfigure() {
								super.onConfigure();
								setEnabled(issuesTable.getItemCount() != 0);
							}

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								configure();
								if (!isEnabled()) {
									tag.put("disabled", "disabled");
									tag.put("data-tippy-content", _T("No issues to export"));
								}
							}

							@Override
							protected List<MenuItem> getMenuItems(FloatingPanel dropdown2) {
								var menuItems = new ArrayList<MenuItem>();
								menuItems.add(new MenuItem() {
				
									@Override
									public String getLabel() {
										return "XLSX";
									}
				
									@Override
									public WebMarkupContainer newLink(String id) {
										return new AjaxLink<Void>(id) {
											
											@Override
											public void onClick(AjaxRequestTarget target) {
												dropdown.close();
												dropdown2.close();
												target.appendJavaScript("window.location.href = $('#" + IssueListPanel.this.getMarkupId() + " .export-xlsx').attr('href');");
											}
				
										};
									}
				
								});
								menuItems.add(new MenuItem() {
				
									@Override
									public String getLabel() {
										return "CSV";
									}
				
									@Override
									public WebMarkupContainer newLink(String id) {
										return new AjaxLink<Void>(id) {
				
											@Override
											public void onClick(AjaxRequestTarget target) {
												dropdown.close();
												dropdown2.close();
												target.appendJavaScript("window.location.href = $('#" + IssueListPanel.this.getMarkupId() + " .export-csv').attr('href');");
											}
				
										};
									}
				
								});											
								return menuItems;
							}

						};
					}
				});	
				
				return menuItems;
			}
			
			private List<Project> getTargetProjects(boolean excludeCurrent) {
				Collection<Project> collection = SecurityUtils.getAuthorizedProjects(new AccessProject());
				ProjectCache cache = getProjectService().cloneCache();
				
				CollectionUtils.filter(collection, new Predicate<Project>() {

					@Override
					public boolean evaluate(Project object) {
						return cache.get(object.getId()).isIssueManagement();
					}
					
				});
				
				if (excludeCurrent)
					collection.remove(getProject());
				
				List<Project> list = new ArrayList<>(collection);
				list.sort(cache.comparingPath());
				
				return list;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getAuthUser() != null);
			}
			
		});
		
		if (getProject() != null && getProject().isTimeTracking() && WicketUtils.isSubscriptionActive()) {
			add(new DropdownLink("showProgress") {
				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					return new QueriedIssuesProgressPanel(id) {
						@Override
						protected ProjectScope getProjectScope() {
							return IssueListPanel.this.getProjectScope();
						}

						@Override
						protected IssueQuery getQuery() {
							return queryModel.getObject();
						}
					};
				}
			});
		} else {
			add(new WebMarkupContainer("showProgress").setVisible(false));			
		}
		
		add(countLabel = new Label("count", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (dataProvider.size() > 1)
					return MessageFormat.format(_T("found {0} issues"), dataProvider.size());
				else
					return _T("found 1 issue");
			} 

		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(dataProvider.size() != 0);
			}
		}.setOutputMarkupPlaceholderTag(true));
		
		dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				Project.push(getProject());
				try {
					var query = queryModel.getObject();
					if (query != null) {
						return getIssueService().query(SecurityUtils.getSubject(), getProjectScope(), query,
								true, (int) first, (int) count).iterator();
					}
				} catch (ExplicitException e) {
					error(e.getMessage());
				} finally {
					Project.pop();
				}
				return new ArrayList<Issue>().iterator();
			}

			@Override
			public long calcSize() {
				Project.push(getProject());
				try {
					var query = queryModel.getObject();
					if (query != null)
						return getIssueService().count(SecurityUtils.getSubject(), getProjectScope(), query.getCriteria());
				} catch (ExplicitException e) {
					error(e.getMessage());
				} finally {
					Project.pop();
				}
				return 0;
			}

			@Override
			public IModel<Issue> model(Issue object) {
				Long issueId = object.getId();
				return new LoadableDetachableModel<>() {

					@Override
					protected Issue load() {
						return getIssueService().load(issueId);
					}

				};
			}

		};
		
		body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Issue, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId, IModel<Issue> rowModel) {
				cellItem.add(new Label(componentId, ""));
			}

			@Override
			public String getCssClass() {
				return "new-indicator";
			}

		});
		
		if (SecurityUtils.getAuthUser() != null)
			columns.add(selectionColumn = new SelectionColumn<Issue, Void>());
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<Issue>> cellItem, String componentId,
									 IModel<Issue> rowModel) {
				Item<?> row = cellItem.findParent(Item.class);
				Cursor cursor = new Cursor(queryModel.getObject().toString(), (int) issuesTable.getItemCount(),
						(int) issuesTable.getCurrentPage() * WebConstants.PAGE_SIZE + row.getIndex(), getProjectScope());
				cellItem.add(newIssueDetail(componentId, rowModel.getObject().getId(), cursor, new HashSet<>()));
			}

			@SuppressWarnings("unchecked")
			private Component newIssueDetail(String componentId, Long issueId, @Nullable Cursor cursor, Set<Long> displayedIssueIds) {
				Fragment fragment = new Fragment(componentId, "contentFrag", IssueListPanel.this);
				fragment.setDefaultModel(new LoadableDetachableModel<Issue>() {

					@Override
					protected Issue load() {
						return getIssueService().load(issueId);
					}

				});

				Issue issue = (Issue) fragment.getDefaultModelObject();

				fragment.add(new IssueTitlePanel("numberAndTitle") {

					@Override
					protected Issue getIssue() {
						return (Issue) fragment.getDefaultModelObject();
					}

					@Override
					protected Project getCurrentProject() {
						return getProject();
					}

					@Override
					protected Cursor getCursor() {
						return cursor;
					}

				});

				fragment.add(new IssueProgressPanel("progress") {
					
					@Override
					protected Issue getIssue() {
						return (Issue) fragment.getDefaultModelObject();
					}
					
				});
				
				fragment.add(new CopyToClipboardLink("copy",
						Model.of(issue.getTitle() + " (" + issue.getReference().toString(getProject()) + ")")));

				fragment.add(new AjaxLink<Void>("pin") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						var issue = (Issue) fragment.getDefaultModelObject();
						getIssueService().togglePin(issue);
						send(getPage(), Broadcast.BREADTH, new IssuePinStatusChanged(target, issueId));
					}

					@Override
					public void onEvent(IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof IssuePinStatusChanged) {
							var issuePinStatusChanged = (IssuePinStatusChanged) event.getPayload();
							if (issuePinStatusChanged.getIssueId().equals(issueId))
								issuePinStatusChanged.getHandler().add(this);
						}
						event.dontBroadcastDeeper();
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						var issue = (Issue) fragment.getDefaultModelObject();
						setVisible(issue.getPinDate() == null
								&& getPage() instanceof ProjectIssueListPage
								&& SecurityUtils.canManageIssues(getProject())
								&& issue.getProject().equals(getProject()));
					}
				}.setOutputMarkupPlaceholderTag(true));

				var linksPanel = new IssueLinksPanel("links") {

					@Override
					protected Issue getIssue() {
						return (Issue) fragment.getDefaultModelObject();
					}

					@Override
					protected List<String> getDisplayLinks() {
						if (displayedIssueIds.contains(issueId))
							return Collections.emptyList();
						else
							return getListLinks();
					}

					@Override
					protected void onToggleExpand(AjaxRequestTarget target) {
						target.add(fragment);
					}

				};
				fragment.add(linksPanel);

				fragment.add(new Label("votes", issue.getVoteCount()));
				fragment.add(new Label("comments", issue.getCommentCount()));

				RepeatingView fieldsView = new RepeatingView("fields");
				for (String field : getListFields()) {
					if (field.equals(Issue.NAME_STATE)) {
						Fragment stateFragment = new Fragment(fieldsView.newChildId(),
								"stateFrag", IssueListPanel.this);
						AjaxLink<Void> transitLink = new TransitionMenuLink("transit") {

							@Override
							protected Issue getIssue() {
								return (Issue) fragment.getDefaultModelObject();
							}

						};

						transitLink.add(new IssueStateBadge("state", (IModel<Issue>) fragment.getDefaultModel(), false));
						stateFragment.add(transitLink);

						fieldsView.add(stateFragment.setOutputMarkupId(true));
					} else if (field.equals(IssueSchedule.NAME_ITERATION)) {
						fieldsView.add(new IterationCrumbPanel(fieldsView.newChildId()) {
							@Override
							protected Issue getIssue() {
								return (Issue) fragment.getDefaultModelObject();
							}
						});
					} else {
						fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), Mode.AVATAR_AND_NAME, true) {

							@SuppressWarnings("deprecation")
							@Override
							protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
								return new AttachAjaxIndicatorListener(
										fieldsView.get(fieldsView.size() - 1), AttachMode.APPEND, false);
							}

							@Override
							protected Issue getIssue() {
								return (Issue) fragment.getDefaultModelObject();
							}

							@Override
							protected Input getField() {
								if (getIssue().isFieldVisible(field))
									return getIssue().getFieldInputs().get(field);
								else
									return null;
							}

						}.setOutputMarkupId(true));
					}
				}
				fragment.add(fieldsView);
				
				LastActivity lastActivity = issue.getLastActivity();
				if (lastActivity.getUser() != null)
					fragment.add(new UserIdentPanel("user", lastActivity.getUser(), Mode.NAME));
				else
					fragment.add(new WebMarkupContainer("user").setVisible(false));
				fragment.add(new Label("activity", lastActivity.getDescription()));
				fragment.add(new Label("date", DateUtils.formatAge(lastActivity.getDate()))
						.add(new AttributeAppender("title", DateUtils.formatDateTime(lastActivity.getDate()))));

				fragment.add(new ListView<Issue>("linkedIssues", new LoadableDetachableModel<>() {

					@Override
					protected List<Issue> load() {
						Issue issue = (Issue) fragment.getDefaultModelObject();
						getIssueLinkService().loadDeepLinks(issue);
						LinkDescriptor descriptor = new LinkDescriptor(linksPanel.getExpandedLink());
						return issue.findLinkedIssues(descriptor.getSpec(), descriptor.isOpposite()).stream().filter(SecurityUtils::canAccessIssue).collect(toList());
					}

				}) {

					@Override
					protected void populateItem(ListItem<Issue> item) {
						Issue issue = item.getModelObject();
						var copyOfDisplayedIssueIds = new HashSet<>(displayedIssueIds);
						copyOfDisplayedIssueIds.add(issueId);
						item.add(newIssueDetail("content", issue.getId(), null, copyOfDisplayedIssueIds));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(linksPanel.getExpandedLink() != null);
					}

				});

				fragment.add(new ChangeObserver() {

					@Override
					public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
						Component detail = newIssueDetail(componentId, issueId, cursor, new HashSet<>());
						fragment.replaceWith(detail);
						handler.add(detail);
					}

					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(Issue.getDetailChangeObservable(issueId));
					}

				});

				fragment.setOutputMarkupId(true);

				return fragment;
			}

		});
		
		body.add(issuesTable = new DataTable<>("issues", columns, dataProvider, WebConstants.PAGE_SIZE) {

			@Override
			protected Item<Issue> newRowItem(String id, int index, IModel<Issue> model) {
				Item<Issue> item = super.newRowItem(id, index, model);
				item.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
					@Override
					protected String load() {
						Issue issue = model.getObject();
						return issue.isVisitedAfter(issue.getLastActivity().getDate()) ? "issue" : "issue new";
					}
				}));
				var issueId = model.getObject().getId();
				item.add(new ChangeObserver() {
					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(Issue.getDetailChangeObservable(issueId));
					}

				});
				item.setOutputMarkupId(true);
				return item;
			}

		});
		body.add(new WebMarkupContainer("tips").setVisible(getPage() instanceof ProjectIssueListPage));
		
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
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
	private IssueWatchService getWatchService() {
		return OneDev.getInstance(IssueWatchService.class);
	}
	
	private TimeTrackingService getTimeTrackingService() {
		return OneDev.getInstance(TimeTrackingService.class);
	}
	
	@Nullable
	private ProjectScope getProjectScope() {
		if (getProject() != null)
			return new ProjectScope(getProject(), true, true);
		else
			return null;
	}
	
	public List<String> getListFields() {
		Project current = getProject();
		while (current != null) {
			List<String> listFields = current.getIssueSetting().getListFields();
			if (listFields != null)
				return listFields;
			current = current.getParent();
		}
		return getGlobalIssueSetting().getListFields();
	}
	
	public List<String> getListLinks() {
		Project current = getProject();
		while (current != null) {
			List<String> listLinks = current.getIssueSetting().getListLinks();
			if (listLinks != null)
				return listLinks;
			current = current.getParent();
		}
		return getGlobalIssueSetting().getListLinks();
	}
	
	protected void onDisplayFieldsAndLinksUpdated(AjaxRequestTarget target) {
	}
	
	protected void onBatchUpdated(AjaxRequestTarget target) {
	}
	
	protected void onBatchDeleted(AjaxRequestTarget target) {
	}
		
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListCssResourceReference()));
	}
	
}

