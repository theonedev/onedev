package io.onedev.server.web.page.project.issues.issuelist;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.IssueQuerySettingManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.FieldsEditBean;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.WatchStatus;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.watchstatus.WatchStatusLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.WorkflowReconcilePanel;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class IssueListPage extends IssuesPage {

	private static final Logger logger = LoggerFactory.getLogger(IssueListPage.class);
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private Component querySave;
	
	public IssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private Map<String, String> getUserQueries() {
		IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
		if (setting != null) 
			return setting.getUserQueries();
		else 
			return new HashMap<>();
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueQuerySettingManager getIssueQuerySettingManager() {
		return OneDev.getInstance(IssueQuerySettingManager.class);		
	}
	
	private void setWatchStatus(Map<String, Boolean> watches, String name, WatchStatus watchStatus) {
		if (watchStatus != WatchStatus.DEFAULT) 
			watches.put(name, watchStatus == WatchStatus.WATCH);
		else
			watches.remove(name);
	}
	
	private WatchStatus getWatchStatus(Map<String, Boolean> watches, String name) {
		Boolean watching = watches.get(name);
		if (Boolean.TRUE.equals(watching))
			return WatchStatus.WATCH;
		else if (Boolean.FALSE.equals(watching))
			return WatchStatus.DO_NOT_WATCH;
		else
			return WatchStatus.DEFAULT;
	}
	
	private WebMarkupContainer newSideContainer() {
		WebMarkupContainer side = new WebMarkupContainer("side");
		side.setOutputMarkupId(true);
		add(side);
		
		side.add(querySave = new ModalLink("save") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(StringUtils.isNotBlank(query));
				setVisible(getLoginUser() != null);
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
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "saveQueryFrag", IssueListPage.this);
				Form<?> form = new Form<Void>("form") {

					@Override
					protected void onError() {
						super.onError();
						RequestCycle.get().find(AjaxRequestTarget.class).add(this);
					}
					
				};
				SaveQueryBean bean = new SaveQueryBean();
				BeanEditor editor = BeanContext.editBean("editor", bean); 
				form.add(editor);
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
						if (setting == null) {
							setting = new IssueQuerySetting();
							setting.setProject(getProject());
							setting.setUser(getLoginUser());
						}
						setting.getUserQueries().put(bean.getName(), query);
						getIssueQuerySettingManager().save(setting);
						target.add(side);
						modal.close();
					}
					
				});
				form.add(new AjaxButton("saveForAll") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						getProject().getIssueWorkflow().getSavedQueries().put(bean.getName(), query);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(side);
						modal.close();
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(getProject()));
					}
					
				});
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.setOutputMarkupId(true);
				fragment.add(form);
				return fragment;
			}
			
		}.setOutputMarkupId(true));
		
		side.add(new ModalLink("edit") {

			private static final String TAB_PANEL_ID = "tabPanel";
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (SecurityUtils.canManage(getProject())) {
					setVisible(!getUserQueries().isEmpty() || !getProject().getIssueWorkflow().getSavedQueries().isEmpty());
				} else {
					setVisible(getLoginUser() != null && !getUserQueries().isEmpty());
				}
			}

			private Component newUserQueriesEditor(String componentId, ModalPanel modal, Map<String, String> userQueries) {
				return new NamedQueriesEditor(componentId, userQueries) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, LinkedHashMap<String, String> queries) {
						IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
						if (setting == null) {
							setting = new IssueQuerySetting();
							setting.setProject(getProject());
							setting.setUser(getLoginUser());
						}
						setting.setUserQueries(queries);
						getIssueQuerySettingManager().save(setting);
						target.add(side);
						modal.close();
					}
					
					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}
				};
			}
			
			private Component newProjectQueriesEditor(String componentId, ModalPanel modal, Map<String, String> projectQueries) {
				return new NamedQueriesEditor(componentId, projectQueries) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, LinkedHashMap<String, String> queries) {
						getProject().getIssueWorkflow().setSavedQueries(queries);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(side);
						modal.close();
					}
					
					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}
					
				};
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "editSavedQueriesFrag", IssueListPage.this);
				List<Tab> tabs = new ArrayList<>();

				Map<String, String> userQueries = getUserQueries();
				if (!userQueries.isEmpty()) {
					tabs.add(new AjaxActionTab(Model.of("For Mine")) {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
						}

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component editor = newUserQueriesEditor(TAB_PANEL_ID, modal, userQueries);
							fragment.replace(editor);
							target.add(editor);
						}
						
					});
					fragment.add(newUserQueriesEditor(TAB_PANEL_ID, modal, userQueries));
				}
				
				Map<String, String> projectQueries = getProject().getIssueWorkflow().getSavedQueries();
				if (SecurityUtils.canManage(getProject()) && !projectQueries.isEmpty()) {
					tabs.add(new AjaxActionTab(Model.of("For All Users")) {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
						}

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component editor = newProjectQueriesEditor(TAB_PANEL_ID, modal, projectQueries);
							fragment.replace(editor);
							target.add(editor);
						}
						
					});
					if (userQueries.isEmpty())
						fragment.add(newProjectQueriesEditor(TAB_PANEL_ID, modal, projectQueries));
				}
				
				fragment.add(new Tabbable("tab", tabs));
				
				fragment.add(new AjaxLink<Void>("close") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				return fragment;
			}
			
		});
		
		side.add(new ListView<NamedQuery>("userQueries", new LoadableDetachableModel<List<NamedQuery>>() {

			@Override
			protected List<NamedQuery> load() {
				List<NamedQuery> namedQueries = new ArrayList<>();
				if (getLoginUser() != null) {
					for (Map.Entry<String, String> entry: getUserQueries().entrySet())
						namedQueries.add(new NamedQuery(entry.getKey(), entry.getValue()));
				}
				return namedQueries;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<NamedQuery> item) {
				NamedQuery namedQuery = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueListPage.class, IssueListPage.paramsOf(getProject(), namedQuery.getQuery()));
				link.add(new Label("label", namedQuery.getName()));
				item.add(link);
				
				item.add(new WatchStatusLink("watchStatus") {
					
					@Override
					protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
						IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
						if (setting == null) {
							setting = new IssueQuerySetting();
							setting.setProject(getProject());
							setting.setUser(getLoginUser());
						}
						setWatchStatus(setting.getUserQueryWatches(), namedQuery.getName(), watchStatus);
						getIssueQuerySettingManager().save(setting);
						target.add(this);
					}
					
					@Override
					protected WatchStatus getWatchStatus() {
						IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
						if (setting != null)
							return IssueListPage.this.getWatchStatus(setting.getUserQueryWatches(), namedQuery.getName());
						else
							return WatchStatus.DEFAULT;
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}

		});
		
		side.add(new ListView<NamedQuery>("projectQueries", new LoadableDetachableModel<List<NamedQuery>>() {

			@Override
			protected List<NamedQuery> load() {
				List<NamedQuery> namedQueries = new ArrayList<>();
				for (Map.Entry<String, String> entry: getProject().getIssueWorkflow().getSavedQueries().entrySet()) {
					try {
						if (getLoginUser() != null || !IssueQuery.parse(getProject(), entry.getValue()).needsLogin())
							namedQueries.add(new NamedQuery(entry.getKey(), entry.getValue()));
					} catch (Exception e) {
						namedQueries.add(new NamedQuery(entry.getKey(), entry.getValue()));
					}
				}
				return namedQueries;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<NamedQuery> item) {
				NamedQuery namedQuery = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueListPage.class, IssueListPage.paramsOf(getProject(), namedQuery.getQuery()));
				link.add(new Label("label", namedQuery.getName()));
				item.add(link);
				
				item.add(new WatchStatusLink("watchStatus") {
					
					@Override
					protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
						IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
						if (setting == null) {
							setting = new IssueQuerySetting();
							setting.setProject(getProject());
							setting.setUser(getLoginUser());
						}
						setWatchStatus(setting.getProjectQueryWatches(), namedQuery.getName(), watchStatus);
						getIssueQuerySettingManager().save(setting);
						target.add(this);
					}
					
					@Override
					protected WatchStatus getWatchStatus() {
						IssueQuerySetting setting = getIssueQuerySettingManager().find(getProject(), getLoginUser());
						if (setting != null)
							return IssueListPage.this.getWatchStatus(setting.getProjectQueryWatches(), namedQuery.getName());
						else
							return WatchStatus.DEFAULT;
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getLoginUser() != null);
					}
					
				});
				
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
				
		return side;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newSideContainer());
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "query"));
		input.add(new IssueQueryBehavior(projectModel));
		
		if (getLoginUser() != null) {
			input.add(new AjaxFormComponentUpdatingBehavior("input"){
	
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(querySave);
				}
				
			});
		}
		Form<?> form = new Form<Void>("query") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject(), query));
			}

		};
		form.add(input);
		add(form);
		
		add(new ModalLink("displayFields") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldsFrag", IssueListPage.this);

				FieldsEditBean bean = new FieldsEditBean();
				bean.setFields(getProject().getIssueWorkflow().getListFields());
				Form<?> form = new Form<Void>("form") {

					@Override
					protected void onError() {
						super.onError();
						RequestCycle.get().find(AjaxRequestTarget.class).add(this);
					}

				};
				form.add(BeanContext.editBean("editor", bean));

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
						getProject().getIssueWorkflow().setListFields(bean.getFields());
						OneDev.getInstance(ProjectManager.class).save(getProject());
						setResponsePage(IssueListPage.this);
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.setOutputMarkupId(true);
				fragment.add(form);
				return fragment;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getProject()));
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canRead(getProject()));
			}
			
		});

		AtomicReference<IssueQuery> parsedQuery = new AtomicReference<>(null);
		try {
			parsedQuery.set(IssueQuery.parse(getProject(), query));
			if (getLoginUser() == null && parsedQuery.get().needsLogin()) {
				form.error("Please login to perform this query");
				parsedQuery.set(null);
			}
		} catch (Exception e) {
			logger.error("Error parsing issue query: " + query, e);
			if (StringUtils.isNotBlank(e.getMessage()))
				form.error(e.getMessage());
			else
				form.error("Malformed issue query");
		}
		
		int count;
		if (parsedQuery.get() != null)
			count = getIssueManager().count(parsedQuery.get().getCriteria());
		else
			count = 0;
		
		IDataProvider<Issue> dataProvider = new IDataProvider<Issue>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				return getIssueManager().query(parsedQuery.get(), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return count;
			}

			@Override
			public IModel<Issue> model(Issue object) {
				Long issueId = object.getId();
				return new LoadableDetachableModel<Issue>() {

					@Override
					protected Issue load() {
						return OneDev.getInstance(IssueManager.class).load(issueId);
					}
					
				};
			}
			
		};
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		
		body.add(new NotificationPanel("feedback", form));
		
		if (SecurityUtils.canManage(getProject())) {
			body.add(new ModalLink("reconcile") {

				@Override
				protected Component newContent(String id, ModalPanel modal) {
					return new WorkflowReconcilePanel(id) {
						
						@Override
						protected Project getProject() {
							return IssueListPage.this.getProject();
						}

						@Override
						protected void onCancel(AjaxRequestTarget target) {
							modal.close();
						}

						@Override
						protected void onCompleted(AjaxRequestTarget target) {
							setResponsePage(IssueListPage.this);
						}
						
					};
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getProject().getIssueWorkflow().isReconciled());
				}

				@Override
				public IModel<?> getBody() {
					return Model.of("reconcile");
				}
				
			});
		} else {
			body.add(new Label("reconcile", "contact project administrator to reconcile") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getProject().getIssueWorkflow().isReconciled());
				}
				
			});
		}
		
		DataView<Issue> issuesView = new DataView<Issue>("issues", dataProvider) {

			@Override
			protected void populateItem(Item<Issue> item) {
				Issue issue = item.getModelObject();
				item.add(new Label("number", "#" + issue.getNumber()));
				Fragment titleFrag = new Fragment("title", "titleFrag", IssueListPage.this);
				QueryPosition position = new QueryPosition(query, count, (int)getCurrentPage() * WebConstants.PAGE_SIZE + item.getIndex());
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueActivitiesPage.class, 
						IssueActivitiesPage.paramsOf(issue, position));
				link.add(new Label("label", issue.getTitle()));
				titleFrag.add(link);
				item.add(titleFrag);

				item.add(new UserLink("user", 
						User.getForDisplay(issue.getLastActivity().getUser(), issue.getLastActivity().getUserName())));
				item.add(new Label("action", issue.getLastActivity().getAction()));
				item.add(new Label("date", DateUtils.formatAge(issue.getLastActivity().getDate())));
				
				item.add(new IssueStateLabel("state", item.getModel()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String fieldName: getProject().getIssueWorkflow().getListFields()) {
					fieldsView.add(new FieldValuesPanel(fieldsView.newChildId()) {

						@Override
						protected Issue getIssue() {
							return item.getModelObject();
						}

						@Override
						protected IssueField getField() {
							return item.getModelObject().getEffectiveFields().get(fieldName);
						}
						
					}.add(AttributeAppender.append("title", fieldName)));
				}
				
				item.add(fieldsView);
				item.add(new Label("votes", issue.getNumOfVotes()));
				item.add(new Label("comments", issue.getNumOfComments()));
				
				Date lastActivityDate;
				if (issue.getLastActivity() != null)
					lastActivityDate = issue.getLastActivity().getDate();
				else
					lastActivityDate = issue.getSubmitDate();
				item.add(AttributeAppender.append("class", 
						issue.isVisitedAfter(lastActivityDate)?"issue":"issue new"));
			}
			
		};
		issuesView.setItemsPerPage(WebConstants.PAGE_SIZE);
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {

			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject(), query);
				params.add(PARAM_CURRENT_PAGE, currentPage+1);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_CURRENT_PAGE).toInt(1)-1;
			}
			
		};
		
		issuesView.setCurrentPage(pagingHistorySupport.getCurrentPage());
		
		body.add(issuesView);
		
		body.add(new HistoryAwarePagingNavigator("issuesPageNav", issuesView, pagingHistorySupport) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(issuesView.getPageCount() > 1);
			}
			
		});
		body.add(new WebMarkupContainer("noIssues") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(parsedQuery.get() != null && dataProvider.size() == 0);
			}
			
		});
		add(body);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueListResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueList.onDomReady();"));
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
	private abstract class NamedQueriesEditor extends Fragment {

		private final NamedQueriesBean bean;
		
		public NamedQueriesEditor(String id, Map<String, String> queries) {
			super(id, "editSavedQueriesContentFrag", IssueListPage.this);
			bean = new NamedQueriesBean();
			for (Map.Entry<String, String> entry: queries.entrySet())
				bean.getQueries().add(new NamedQuery(entry.getKey(), entry.getValue()));
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			Form<?> form = new Form<Void>("form");
			form.setOutputMarkupId(true);
			
			form.add(new NotificationPanel("feedback", form));
			form.add(BeanContext.editBean("editor", bean));
			form.add(new AjaxButton("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					LinkedHashMap<String, String> namedQueries = new LinkedHashMap<>();
					for (NamedQuery namedQuery: bean.getQueries()) {
						if (namedQueries.put(namedQuery.getName(), namedQuery.getQuery()) != null) {
							form.error("Duplicate name found: " + namedQuery.getName());
							return;
						}
					}
					onSave(target, namedQueries);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(form);
				}
				
			});
			form.add(new AjaxLink<Void>("cancel") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
				
			});
			add(form);
			setOutputMarkupId(true);
		}
		
		protected abstract void onSave(AjaxRequestTarget target, LinkedHashMap<String, String> queries);
		
		protected abstract void onCancel(AjaxRequestTarget target);
	}
}
