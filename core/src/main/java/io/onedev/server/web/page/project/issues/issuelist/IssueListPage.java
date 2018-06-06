package io.onedev.server.web.page.project.issues.issuelist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueQuerySettingManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.IssueQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.NamedQuery;
import io.onedev.server.model.support.issue.WatchStatus;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.issuelist.IssueListPanel;
import io.onedev.server.web.component.issuelist.QuerySaveSupport;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.watchstatus.WatchStatusLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public class IssueListPage extends IssuesPage {

	private static final String PARAM_CURRENT_PAGE = "currentPage";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private Component side;
	
	public IssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
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
	
	@Nullable
	private IssueQuerySetting getIssueQuerySetting() {
		return getProject().getIssueQuerySettingOfCurrentUser();
	}
	
	private ArrayList<NamedQuery> getUserQueries() {
		if (getIssueQuerySetting() != null)
			return getIssueQuerySetting().getUserQueries();
		else
			return new ArrayList<>();
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
		
		side.add(new ModalLink("edit") {

			private static final String TAB_PANEL_ID = "tabPanel";
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (SecurityUtils.canManage(getProject())) {
					setVisible(!getUserQueries().isEmpty() || !getProject().getSavedIssueQueries().isEmpty());
				} else {
					setVisible(getLoginUser() != null && !getUserQueries().isEmpty());
				}
			}

			private Component newUserQueriesEditor(String componentId, ModalPanel modal, ArrayList<NamedQuery> userQueries) {
				return new NamedQueriesEditor(componentId, userQueries) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, ArrayList<NamedQuery> queries) {
						IssueQuerySetting setting = getIssueQuerySetting();
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
			
			private Component newProjectQueriesEditor(String componentId, ModalPanel modal, ArrayList<NamedQuery> projectQueries) {
				return new NamedQueriesEditor(componentId, projectQueries) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, ArrayList<NamedQuery> queries) {
						getProject().setSavedIssueQueries(queries);
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

				ArrayList<NamedQuery> userQueries = getUserQueries();
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
				
				ArrayList<NamedQuery> projectQueries = getProject().getSavedIssueQueries();
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
				if (getLoginUser() != null) 
					return getUserQueries();
				else
					return new ArrayList<>();
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
						IssueQuerySetting setting = getIssueQuerySetting();
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
						IssueQuerySetting setting = getIssueQuerySetting();
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
				for (NamedQuery namedQuery: getProject().getSavedIssueQueries()) {
					try {
						if (getLoginUser() != null || !IssueQuery.parse(getProject(), namedQuery.getQuery(), true).needsLogin())
							namedQueries.add(namedQuery);
					} catch (Exception e) {
						namedQueries.add(namedQuery);
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
						IssueQuerySetting setting = getIssueQuerySetting();
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
						IssueQuerySetting setting = getIssueQuerySetting();
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

		add(side = newSideContainer());
		
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
		
		add(new IssueListPanel("main", new PropertyModel<String>(this, "query")) {

			@Override
			protected Project getProject() {
				return IssueListPage.this.getProject();
			}

			@Override
			protected IssueCriteria getBaseCriteria() {
				return null;
			}

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return pagingHistorySupport;
			}

			@Override
			protected void onQueryUpdated(AjaxRequestTarget target) {
				setResponsePage(IssueListPage.class, paramsOf(getProject(), query));
			}

			@Override
			protected QuerySaveSupport getQuerySaveSupport() {
				return new QuerySaveSupport() {

					@Override
					public void onSaveQuery(AjaxRequestTarget target) {
						new ModalPanel(target)  {

							@Override
							protected Component newContent(String id) {
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
										IssueQuerySetting setting = getIssueQuerySetting();
										if (setting == null) {
											setting = new IssueQuerySetting();
											setting.setProject(getProject());
											setting.setUser(getLoginUser());
											getProject().setIssueQuerySettingOfCurrentUser(setting);
										}
										NamedQuery namedQuery = setting.getUserQuery(bean.getName());
										if (namedQuery == null) {
											namedQuery = new NamedQuery(bean.getName(), query);
											setting.getUserQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										getIssueQuerySettingManager().save(setting);
										target.add(side);
										close();
									}
									
								});
								form.add(new AjaxButton("saveForAll") {

									@Override
									protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
										super.onSubmit(target, form);
										NamedQuery namedQuery = getProject().getSavedIssueQuery(bean.getName());
										if (namedQuery == null) {
											namedQuery = new NamedQuery(bean.getName(), query);
											getProject().getSavedIssueQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(ProjectManager.class).save(getProject());
										target.add(side);
										close();
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
										close();
									}
									
								});
								form.add(new AjaxLink<Void>("cancel") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										close();
									}
									
								});
								form.setOutputMarkupId(true);
								fragment.add(form);
								return fragment;
							}
							
						};
					}
					
				};
			}

		});
		
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
	
	private abstract class NamedQueriesEditor extends Fragment {

		private final NamedQueriesBean bean;
		
		public NamedQueriesEditor(String id, ArrayList<NamedQuery> queries) {
			super(id, "editSavedQueriesContentFrag", IssueListPage.this);
			bean = new NamedQueriesBean();
			bean.getQueries().addAll(queries);
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
					
					Set<String> names = new HashSet<>();
					for (NamedQuery namedQuery: bean.getQueries()) {
						if (names.contains(namedQuery.getName())) {
							form.error("Duplicate name found: " + namedQuery.getName());
							return;
						} else {
							names.add(namedQuery.getName());
						}
					}
					onSave(target, (ArrayList<NamedQuery>)bean.getQueries());
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
		
		protected abstract void onSave(AjaxRequestTarget target, ArrayList<NamedQuery> queries);
		
		protected abstract void onCancel(AjaxRequestTarget target);
	}
}
