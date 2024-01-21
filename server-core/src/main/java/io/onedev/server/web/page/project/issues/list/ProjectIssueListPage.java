package io.onedev.server.web.page.project.issues.list;

import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueQueryPersonalizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueQueryPersonalization;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.ProjectIssueSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.LinkSide;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.component.issue.link.IssueLinksPanel;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import io.onedev.server.web.component.issue.list.IssuePinStatusChanged;
import io.onedev.server.web.component.issue.milestone.MilestoneCrumbPanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.issue.progress.IssueProgressPanel;
import io.onedev.server.web.component.issue.title.IssueTitlePanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.savedquery.NamedQueriesBean;
import io.onedev.server.web.component.savedquery.PersonalQuerySupport;
import io.onedev.server.web.component.savedquery.SaveQueryPanel;
import io.onedev.server.web.component.savedquery.SavedQueriesPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.NamedIssueQueriesBean;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
public class ProjectIssueListPage extends ProjectIssuesPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private String query;
	
	private final IModel<List<Issue>> pinnedIssuesModel = new LoadableDetachableModel<>() {
		@Override
		protected List<Issue> load() {
			return getIssueManager().queryPinned(getProject());
		}

	};
	
	private SavedQueriesPanel<NamedIssueQuery> savedQueries;
	
	private IssueListPanel issueList;
	
	private Component pinnedIssuesContainer;
	
	public ProjectIssueListPage(PageParameters params) {
		super(params);
		query = params.get(PARAM_QUERY).toOptionalString();
	}

	private IssueQueryPersonalizationManager getIssueQueryPersonalizationManager() {
		return OneDev.getInstance(IssueQueryPersonalizationManager.class);		
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(pinnedIssuesContainer = new WebMarkupContainer("pinnedIssues") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new ListView<>("pinnedIssues", pinnedIssuesModel) {

					@Override
					protected void populateItem(ListItem<Issue> item) {
						item.add(newContent("content", item.getModel().getObject().getId()));
					}
					
					private Component newContent(String componentId, Long issueId) {
						Issue issue = getIssueManager().load(issueId);

						Fragment fragment = new Fragment(componentId, "pinnedIssueFrag", ProjectIssueListPage.this);

						List<String> displayFields = issueList.getListFields();

						RepeatingView fieldsView = new RepeatingView("fields");
						for (String fieldName: displayFields) {
							if (fieldName.equals(Issue.NAME_STATE)) {
								Fragment stateFragment = new Fragment(fieldsView.newChildId(),
										"stateFrag", ProjectIssueListPage.this);
								AjaxLink<Void> transitLink = new TransitionMenuLink("transit") {

									@Override
									protected Issue getIssue() {
										return getIssueManager().load(issueId);
									}

								};

								transitLink.add(new IssueStateBadge("state", new LoadableDetachableModel<Issue>() {
									@Override
									protected Issue load() {
										return getIssueManager().load(issueId);
									}
								}));
								stateFragment.add(transitLink);

								fieldsView.add(stateFragment.setOutputMarkupId(true));
							} else if (fieldName.equals(IssueSchedule.NAME_MILESTONE)) {
								fieldsView.add(new MilestoneCrumbPanel(fieldsView.newChildId()) {
									@Override
									protected Issue getIssue() {
										return getIssueManager().load(issueId);
									}
								});
							} else {
								Input field = issue.getFieldInputs().get(fieldName);
								if (field != null && !field.getValues().isEmpty()) {
									fieldsView.add(new FieldValuesPanel(fieldsView.newChildId(), Mode.AVATAR, true) {

										@Override
										protected Issue getIssue() {
											return getIssueManager().load(issueId);
										}

										@Override
										protected Input getField() {
											var issue = getIssueManager().load(issueId);
											if (issue.isFieldVisible(fieldName))
												return field;
											else
												return null;
										}

										@SuppressWarnings("deprecation")
										@Override
										protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
											return new AttachAjaxIndicatorListener(
													fieldsView.get(fieldsView.size()-1), AttachAjaxIndicatorListener.AttachMode.APPEND, false);
										}

									}.setOutputMarkupId(true));
								}
							}
						}

						fragment.add(fieldsView);
						fragment.add(new IssueTitlePanel("numberAndTitle") {

							@Override
							protected Issue getIssue() {
								return getIssueManager().load(issueId);
							}

							@Override
							protected Project getCurrentProject() {
								return getProject();
							}

							@Nullable
							@Override
							protected Cursor getCursor() {
								return null;
							}

						});
						fragment.add(new IssueProgressPanel("progress") {

							@Override
							protected Issue getIssue() {
								return getIssueManager().load(issueId);
							}

						});

						fragment.add(new CopyToClipboardLink("copy",
								Model.of(issue.getTitle() + " (#" + issue.getNumber() + ")")));
						
						var linksPanel = new IssueLinksPanel("links") {

							@Override
							protected Issue getIssue() {
								return getIssueManager().load(issueId);
							}

							@Override
							protected List<String> getDisplayLinks() {
								return issueList.getListLinks();
							}

							@Override
							protected void onToggleExpand(AjaxRequestTarget target) {
								target.add(fragment);
							}
						};
						fragment.add(linksPanel);
						
						fragment.add(new AjaxLink<Void>("unpin") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								getIssueManager().togglePin(getIssueManager().load(issueId));
								send(getPage(), Broadcast.BREADTH, new IssuePinStatusChanged(target, issueId));
							}

							@Override
							protected void onConfigure() {
								super.onConfigure();
								setVisible(SecurityUtils.canManageIssues(getIssueManager().load(issueId).getProject()));
							}
							
						});
						
						fragment.add(new ListView<Issue>("linkedIssues", new LoadableDetachableModel<>() {

							@Override
							protected List<Issue> load() {
								Issue issue = getIssueManager().load(issueId);
								OneDev.getInstance(IssueLinkManager.class).loadDeepLinks(issue);
								LinkSide side = new LinkSide(linksPanel.getExpandedLink());
								return issue.findLinkedIssues(side.getSpec(), side.isOpposite());
							}

						}) {

							@Override
							protected void populateItem(ListItem<Issue> item) {
								item.add(newContent("content", item.getModel().getObject().getId()));
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
								Component content = newContent(componentId, issueId);
								fragment.replaceWith(content);
								handler.add(content);
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
				
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof IssuePinStatusChanged) 
					((IssuePinStatusChanged) event.getPayload()).getHandler().add(this);
				event.dontBroadcastDeeper();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getPinnedIssues().isEmpty());
			}
			
		});
		
		add(savedQueries = new SavedQueriesPanel<>("savedQueries") {

			@Override
			protected NamedQueriesBean<NamedIssueQuery> newNamedQueriesBean() {
				return new NamedIssueQueriesBean();
			}

			@Override
			protected Link<Void> newQueryLink(String componentId, NamedIssueQuery namedQuery) {
				PageParameters params = ProjectIssueListPage.paramsOf(
						getProject(), namedQuery.getQuery(), 0);
				return new BookmarkablePageLink<Void>(componentId, ProjectIssueListPage.class, params);
			}

			@Override
			protected QueryPersonalization<NamedIssueQuery> getQueryPersonalization() {
				return getProject().getIssueQueryPersonalizationOfCurrentUser();
			}

			@Override
			protected ArrayList<NamedIssueQuery> getCommonQueries() {
				return (ArrayList<NamedIssueQuery>) getProject().getIssueSetting().getNamedQueries();
			}

			@Override
			protected void onSaveCommonQueries(ArrayList<NamedIssueQuery> namedQueries) {
				getProject().getIssueSetting().setNamedQueries(namedQueries);
				OneDev.getInstance(ProjectManager.class).update(getProject());
			}

			@Override
			protected ArrayList<NamedIssueQuery> getInheritedCommonQueries() {
				if (getProject().getParent() != null)
					return (ArrayList<NamedIssueQuery>) getProject().getParent().getNamedIssueQueries();
				else
					return (ArrayList<NamedIssueQuery>) getIssueSetting().getNamedQueries();
			}

		});
		
		add(issueList = new IssueListPanel("issues", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				CharSequence url = RequestCycle.get().urlFor(ProjectIssueListPage.class, params);
				pushState(RequestCycle.get().find(AjaxRequestTarget.class), url.toString(), query);
			}
			
		}) {

			@Override
			protected PagingHistorySupport getPagingHistorySupport() {
				return new PagingHistorySupport() {

					@Override
					public PageParameters newPageParameters(int currentPage) {
						return paramsOf(getProject(), query, currentPage+1);
					}
					
					@Override
					public int getCurrentPage() {
						return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
					}
					
				};
			}

			@Override
			protected void onDisplayFieldsAndLinksUpdated(AjaxRequestTarget target) {
				target.add(pinnedIssuesContainer);
			}

			@Override
			protected void onBatchUpdated(AjaxRequestTarget target) {
				target.add(pinnedIssuesContainer);
			}

			@Override
			protected void onBatchDeleted(AjaxRequestTarget target) {
				target.add(pinnedIssuesContainer);
			}

			@Override
			protected QuerySaveSupport getQuerySaveSupport() {
				return new QuerySaveSupport() {

					@Override
					public void onSaveQuery(AjaxRequestTarget target, String query) {
						new ModalPanel(target)  {

							@Override
							protected Component newContent(String id) {
								return new SaveQueryPanel(id, new PersonalQuerySupport() {

									@Override
									public void onSave(AjaxRequestTarget target, String name) {
										IssueQueryPersonalization setting = getProject().getIssueQueryPersonalizationOfCurrentUser();
										NamedIssueQuery namedQuery = NamedQuery.find(setting.getQueries(), name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											setting.getQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										if (setting.isNew())
											getIssueQueryPersonalizationManager().create(setting);
										else
											getIssueQueryPersonalizationManager().update(setting);
										target.add(savedQueries);
										close();
									}
									
								}) {

									@Override
									protected void onSave(AjaxRequestTarget target, String name) {
										ProjectIssueSetting setting = getProject().getIssueSetting();
										if (setting.getNamedQueries() == null) 
											setting.setNamedQueries(new ArrayList<>(getIssueSetting().getNamedQueries()));
										NamedIssueQuery namedQuery = getProject().getNamedIssueQuery(name);
										if (namedQuery == null) {
											namedQuery = new NamedIssueQuery(name, query);
											setting.getNamedQueries().add(namedQuery);
										} else {
											namedQuery.setQuery(query);
										}
										OneDev.getInstance(ProjectManager.class).update(getProject());
										target.add(savedQueries);
										close();
									}

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										close();
									}
									
								};
							}
							
						};
					}

					@Override
					public boolean isSavedQueriesVisible() {
						savedQueries.configure();
						return savedQueries.isVisible();
					}

				};
			}

			@Override
			protected Project getProject() {
				return ProjectIssueListPage.this.getProject();
			}

		});
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(issueList);
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String query, int page) {
		PageParameters params = paramsOf(project);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (page != 0)
			params.add(PARAM_PAGE, page);
		return params;
	}
	
	public static PageParameters paramsOf(Project project, int page) {
		String query = null;
		if (project.getIssueQueryPersonalizationOfCurrentUser() != null 
				&& !project.getIssueQueryPersonalizationOfCurrentUser().getQueries().isEmpty()) {
			query = project.getIssueQueryPersonalizationOfCurrentUser().getQueries().iterator().next().getQuery();
		} else if (!project.getNamedIssueQueries().isEmpty()) {
			query = project.getNamedIssueQueries().iterator().next().getQuery();
		}
		return paramsOf(project, query, page);
	}

	private List<Issue> getPinnedIssues() {
		return pinnedIssuesModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		pinnedIssuesModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectIssueListCssResourceReference()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Issues");
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project, 0));
		else 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
