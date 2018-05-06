package io.onedev.server.web.page.project.issues.issuelist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import org.apache.wicket.model.AbstractReadOnlyModel;
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
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueListCustomization;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.choice.MultiChoiceEditor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.WorkflowReconcilePanel;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class IssueListPage extends ProjectPage {

	private static final Logger logger = LoggerFactory.getLogger(IssueListPage.class);
	
	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private static final MetaDataKey<IssueListCustomization> CUSTOMIZATION_KEY = 
			new MetaDataKey<IssueListCustomization>() {};
			
	private String query;
	
	private Component querySave;
	
	public IssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private IssueListCustomization getCustomization() {
		IssueListCustomization customization = WebSession.get().getMetaData(CUSTOMIZATION_KEY);
		if (customization == null)
			customization = getProject().getIssueListCustomization();
		return customization;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer head = new WebMarkupContainer("issueListHead");
		head.setOutputMarkupId(true);
		add(head);
		
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
		head.add(form);
		
		form.add(new NotificationPanel("feedback", form));
		
		head.add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())));
		
		head.add(querySave = new ModalLink("save") {

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
				if (!isEnabled())
					tag.put("disabled", "disabled");
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
						getLoginUser().getIssueQueries().put(bean.getName(), query);
						OneDev.getInstance(UserManager.class).save(getLoginUser());
						target.add(head);
						modal.close();
					}
					
				});
				form.add(new AjaxButton("saveForAll") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						getProject().getIssueListCustomization().getSavedQueries().put(bean.getName(), query);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(head);
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
		
		head.add(new ModalLink("edit") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (SecurityUtils.canManage(getProject())) {
					setVisible(!getLoginUser().getIssueQueries().isEmpty() 
							|| !getProject().getIssueListCustomization().getSavedQueries().isEmpty());
				} else {
					setVisible(getLoginUser() != null && !getLoginUser().getIssueQueries().isEmpty());
				}
			}
			
			private WebMarkupContainer newQueryList(String componentId, List<String> queries) {
				Fragment fragment = new Fragment(componentId, "queryListFrag", IssueListPage.this);
				WebMarkupContainer table = new WebMarkupContainer("queries");
				fragment.add(table);
				table.add(new ListView<String>("queries", queries) {

					@Override
					protected void populateItem(ListItem<String> item) {
						item.add(new Label("name", item.getModelObject()));
						item.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								queries.remove(item.getModelObject());
								target.add(fragment);
							}
							
						});
					}
					
				});
				table.add(new SortBehavior() {

					@Override
					protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
						int fromIndex = from.getItemIndex();
						int toIndex = to.getItemIndex();
						if (fromIndex < toIndex) {
							for (int i=0; i<toIndex-fromIndex; i++) 
								Collections.swap(queries, fromIndex+i, fromIndex+i+1);
						} else {
							for (int i=0; i<fromIndex-toIndex; i++) 
								Collections.swap(queries, fromIndex-i, fromIndex-i-1);
						}
						target.add(fragment);
					}
					
				}.sortable("tbody").handle(".drag-handle").helperClass("sort-helper"));
				
				fragment.add(new WebMarkupContainer("noRecords") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(queries.isEmpty());
					}
					
				});
				fragment.setOutputMarkupId(true);
				return fragment;
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "editSavedFrag", IssueListPage.this);
				List<String> userQueries = new ArrayList<>(getLoginUser().getIssueQueries().keySet());
				List<String> projectQueries = new ArrayList<>();
				if (SecurityUtils.canManage(getProject())) 
					projectQueries.addAll(getProject().getIssueListCustomization().getSavedQueries().keySet());
				
				if (!userQueries.isEmpty() && !projectQueries.isEmpty()) {
					fragment.add(new Label("title", "Edit Saved Queries"));
					Fragment tabFragment = new Fragment("content", "tabFrag", fragment);
					List<Tab> tabs = new ArrayList<>();
					tabs.add(new AjaxActionTab(Model.of("Mine")) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							WebMarkupContainer queryList = newQueryList("tabPanel", userQueries);
							tabFragment.replace(queryList);
							target.add(queryList);
						}
						
					});
					tabs.add(new AjaxActionTab(Model.of("All Users")) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							WebMarkupContainer queryList = newQueryList("tabPanel", projectQueries);
							tabFragment.replace(queryList);
							target.add(queryList);
						}
						
					});
					tabFragment.add(new Tabbable("tab", tabs));
					tabFragment.add(newQueryList("tabPanel", userQueries));
					fragment.add(tabFragment);
				} else if (!userQueries.isEmpty()) {
					fragment.add(new Label("title", "Edit Saved Queries (Mine)"));
					fragment.add(newQueryList("content", userQueries));
				} else {
					fragment.add(new Label("title", "Edit Saved Queries (All Users)"));
					fragment.add(newQueryList("content", projectQueries));
				}
				
				fragment.add(new AjaxLink<Void>("save") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						LinkedHashMap<String, String> queryMap = new LinkedHashMap<>();
						for (String query: userQueries)
							queryMap.put(query, getLoginUser().getIssueQueries().get(query));
						getLoginUser().setIssueQueries(queryMap);
						OneDev.getInstance(UserManager.class).save(getLoginUser());
						
						if (SecurityUtils.canManage(getProject())) {
							queryMap = new LinkedHashMap<>();
							for (String query: projectQueries)
								queryMap.put(query, getProject().getIssueListCustomization().getSavedQueries().get(query));
							getProject().getIssueListCustomization().setSavedQueries(queryMap);
							OneDev.getInstance(ProjectManager.class).save(getProject());
						}
						modal.close();
						target.add(head);
					}
					
				});
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				fragment.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				fragment.setOutputMarkupId(true);
				return fragment;
			}
			
		});
		
		head.add(new ModalLink("fields") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldsFrag", IssueListPage.this);

				IssueListCustomization customization = getCustomization();
				Form<?> form = new Form<Void>("form") {

					@Override
					protected void onError() {
						super.onError();
						RequestCycle.get().find(AjaxRequestTarget.class).add(this);
					}

				};
				
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(IssueListCustomization.class, "displayFields"); 
				IModel<List<String>> propertyModel = new IModel<List<String>>() {

					@Override
					public void detach() {
					}

					@SuppressWarnings("unchecked")
					@Override
					public List<String> getObject() {
						return (List<String>) propertyDescriptor.getPropertyValue(customization);
					}

					@Override
					public void setObject(List<String> object) {
						propertyDescriptor.setPropertyValue(customization, object);
					}
					
				};
				form.add(new MultiChoiceEditor("editor", propertyDescriptor, propertyModel));

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
						WebSession.get().setMetaData(CUSTOMIZATION_KEY, customization);
						setResponsePage(IssueListPage.this);
					}
					
				});
				
				form.add(new AjaxButton("saveAsDefault") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(getProject()));
					}

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						WebSession.get().setMetaData(CUSTOMIZATION_KEY, customization);
						getProject().setIssueListCustomization(customization);
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
			
		});
		
		IModel<List<SavedQuery>> userQueriesModel = new LoadableDetachableModel<List<SavedQuery>>() {

			@Override
			protected List<SavedQuery> load() {
				List<SavedQuery> savedQueries = new ArrayList<>();
				if (getLoginUser() != null) {
					for (Map.Entry<String, String> entry: getLoginUser().getIssueQueries().entrySet())
						savedQueries.add(new SavedQuery(entry.getKey(), entry.getValue()));
				}
				return savedQueries;
			}
			
		};
		
		IModel<List<SavedQuery>> projectQueriesModel = new LoadableDetachableModel<List<SavedQuery>>() {

			@Override
			protected List<SavedQuery> load() {
				List<SavedQuery> savedQueries = new ArrayList<>();
				for (Map.Entry<String, String> entry: getProject().getIssueListCustomization().getSavedQueries().entrySet())
					savedQueries.add(new SavedQuery(entry.getKey(), entry.getValue()));
				return savedQueries;
			}
			
		};

		WebMarkupContainer presetQueries = new WebMarkupContainer("presetQueries") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!userQueriesModel.getObject().isEmpty() 
						|| !projectQueriesModel.getObject().isEmpty());
			}
			
		};
		head.add(presetQueries);
		
		presetQueries.add(new ListView<SavedQuery>("userQueries", userQueriesModel) {

			@Override
			protected void populateItem(ListItem<SavedQuery> item) {
				SavedQuery savedQuery = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueListPage.class, IssueListPage.paramsOf(getProject(), savedQuery.query));
				link.add(new Label("label", savedQuery.name));
				item.add(link);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}

		});
		
		presetQueries.add(new ListView<SavedQuery>("projectQueries", projectQueriesModel) {

			@Override
			protected void populateItem(ListItem<SavedQuery> item) {
				SavedQuery savedQuery = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueListPage.class, IssueListPage.paramsOf(getProject(), savedQuery.query));
				link.add(new Label("label", savedQuery.name));
				item.add(link);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		AtomicReference<IssueQuery> parsedQuery = new AtomicReference<>(null);
		try {
			parsedQuery.set(IssueQuery.parse(getProject(), query));
		} catch (Exception e) {
			logger.error("Error parsing issue query: " + query, e);
			if (StringUtils.isNotBlank(e.getMessage()))
				form.error(e.getMessage());
			else
				form.error("Malformed issue query");
		}
		
		IDataProvider<Issue> dataProvider = new IDataProvider<Issue>() {

			private IssueManager getIssueManager() {
				return OneDev.getInstance(IssueManager.class);
			}
			
			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Issue> iterator(long first, long count) {
				return getIssueManager().query(parsedQuery.get(), (int)first, (int)count).iterator();
			}

			@Override
			public long size() {
				return getIssueManager().count(parsedQuery.get().getCriteria());
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
				
			});
		}
		
		body.setVisible(parsedQuery.get() != null);
		
		body.setOutputMarkupId(true);
		
		DataView<Issue> issuesView = new DataView<Issue>("issues", dataProvider) {

			@Override
			protected void populateItem(Item<Issue> item) {
				Issue issue = item.getModelObject();
				item.add(new Label("number", "#" + issue.getNumber()));
				Fragment titleFrag = new Fragment("title", "titleFrag", IssueListPage.this);
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueDetailPage.class, IssueDetailPage.paramsOf(issue));
				link.add(new Label("label", issue.getTitle()));
				titleFrag.add(link);
				item.add(titleFrag);
				
				item.add(new UserLink("submitterName", User.getForDisplay(issue.getSubmitter(), issue.getSubmitterName())));
				item.add(new Label("submitDate", DateUtils.formatAge(issue.getSubmitDate())));
				
				item.add(new IssueStateLabel("state", item.getModel()));
				
				RepeatingView fieldsView = new RepeatingView("fields");
				for (String fieldName: getCustomization().getDisplayFields()) {
					fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), new AbstractReadOnlyModel<MultiValueIssueField>() {

						@Override
						public MultiValueIssueField getObject() {
							return item.getModelObject().getMultiValueFields().get(fieldName);
						}
						
					}).add(AttributeAppender.append("title", fieldName)));
				}
				
				item.add(fieldsView);
				item.add(new Label("votes", issue.getVotes()));
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
				setVisible(dataProvider.size() == 0);
			}
			
		});
		add(body);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueListResourceReference()));
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
	private static class SavedQuery implements Serializable {
		
		final String name;
		
		final String query;
		
		SavedQuery(String name, String query) {
			this.name = name;
			this.query = query;
		}
	}
	
}
