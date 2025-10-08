package io.onedev.server.web.page.project.issues.boards;

import static io.onedev.server.model.Issue.NAME_BOARD_POSITION;
import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.board.BoardEditPanel;
import io.onedev.server.web.component.iteration.IterationDateLabel;
import io.onedev.server.web.component.iteration.IterationStatusLabel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.page.project.issues.iteration.IterationBurndownPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.editbean.IterationEditBean;

public class IssueBoardsPage extends ProjectIssuesPage {

	public static final String PARAM_BOARD = "board";
	
	private static final String PARAM_ITERATION = "iteration";
	
	private static final String PARAM_BACKLOG = "backlog";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_BACKLOG_QUERY = "backlog-query";

	private static final String ITERATION_ALL = "all";

	public static final String ITERATION_UNSCHEDULED = "unscheduled";

	private final List<BoardSpec> boards;
	
	private final int boardIndex;
	
	private final IModel<IterationSelection> iterationSelectionModel;
	
	private final boolean backlog;
	
	private String queryString;

	private String backlogQueryString;
	
	private Fragment contentFrag;
	
	private WebMarkupContainer body;
	
	private TextField<String> queryInput;
	
	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<>() {

		@Override
		protected IssueQuery load() {
			return parse(false, getBoard().getBaseQuery(), queryString);
		}

	};
	
	private final IModel<IssueQuery> backlogQueryModel = new LoadableDetachableModel<>() {

		@Override
		protected IssueQuery load() {
			return parse(true, getBoard().getBacklogBaseQuery(), backlogQueryString);
		}

	};
	
	private IFeedbackMessageFilter newFeedbackMessageFilter(boolean backlog) {
		return message -> ((QueryParseMessage)message.getMessage()).backlog == backlog;		
	}
	
	@Nullable
	private IssueQuery parse(boolean backlog, @Nullable String baseQueryString, @Nullable String queryString) {
		contentFrag.getFeedbackMessages().clear(newFeedbackMessageFilter(backlog));
		
		IssueQueryParseOption option = new IssueQueryParseOption()
				.withCurrentUserCriteria(true)
				.withCurrentProjectCriteria(true);
		
		IssueQuery query;
		try {
			query = IssueQuery.parse(getProject(), queryString, option, true);
		} catch (Exception e) {
			if (e instanceof ExplicitException)
				contentFrag.error(new QueryParseMessage(backlog, _T("Error parsing %squery: ") + e.getMessage()));
			else
				contentFrag.error(new QueryParseMessage(backlog, _T("Malformed %squery")));
			return null;
		}

		IssueQuery baseQuery;
		try {
			baseQuery = IssueQuery.parse(getProject(), baseQueryString, option, true);
		} catch (Exception e) {
			if (e instanceof ExplicitException)
				contentFrag.error(new QueryParseMessage(backlog, _T("Error parsing %sbase query: ") + e.getMessage()));
			else
				contentFrag.error(new QueryParseMessage(backlog, _T("Malformed %sbase query")));
			return null;
		}
		query = IssueQuery.merge(baseQuery, query);
		var sort = new EntitySort();
		sort.setDirection(EntitySort.Direction.ASCENDING);
		sort.setField(NAME_BOARD_POSITION);
		query.getSorts().add(sort);
		return query;
	}
	
	public IssueBoardsPage(PageParameters params) {
		super(params);
		
		boards = getProject().getHierarchyBoards();
		
		String boardName = params.get(PARAM_BOARD).toString();
		if (StringUtils.isNotBlank(boardName)) {
			boardIndex = BoardSpec.getBoardIndex(boards, boardName);
			if (boardIndex == -1)
				throw new ExplicitException(_T("Can not find issue board: ") + boardName);
		} else {
			boardIndex = 0;
		}

		var iterationParam = params.get(PARAM_ITERATION).toString();
		iterationSelectionModel = new LoadableDetachableModel<>() {
			
			private IterationSelection getCurrentIteration() {
				if (!getIterations().isEmpty()) {
					var iteration = getIterations().iterator().next();
					if (!iteration.isClosed())
						return new IterationSelection.Specified(iteration);
				}
				return new IterationSelection.Unscheduled();
			}
			
			@Override
			protected IterationSelection load() {
				if (ITERATION_UNSCHEDULED.equals(iterationParam)) {
					return new IterationSelection.Unscheduled();
				} else if (ITERATION_ALL.equals(iterationParam)) {
					return new IterationSelection.All();
				} else if (NumberUtils.isDigits(iterationParam)) {
					var iteration = getIterationService().load(Long.valueOf(iterationParam));
					if (getBoard().getIterationPrefix() != null && !iteration.getName().startsWith(getBoard().getIterationPrefix()) 
							|| !iteration.getProject().isSelfOrAncestorOf(getProject())) {
						return getCurrentIteration();
					} else {
						return new IterationSelection.Specified(iteration);
					}
				} else {
					return getCurrentIteration();
				}
			}
		};
		
		backlog = params.get(PARAM_BACKLOG).toBoolean() && getIterationSelection().getIteration() != null;
		queryString = params.get(PARAM_QUERY).toString();
		backlogQueryString = params.get(PARAM_BACKLOG_QUERY).toString();
	}
	
	private IterationService getIterationService() {
		return OneDev.getInstance(IterationService.class);
	}
	
	@Override
	protected void onDetach() {
		iterationSelectionModel.detach();
		queryModel.detach();
		backlogQueryModel.detach();
		super.onDetach();
	}

	@Nullable
	public BoardSpec getBoard() {
		if (boardIndex < boards.size())
			return boards.get(boardIndex);
		else
			return null;
	}
	
	public IterationSelection getIterationSelection() {
		return iterationSelectionModel.getObject();
	}
	
	private void doQuery(AjaxRequestTarget target) {
		if (backlog) {
			backlogQueryString = queryInput.getModelObject();
			getPageParameters().set(PARAM_BACKLOG_QUERY, backlogQueryString);
		} else { 
			queryString = queryInput.getModelObject();
			getPageParameters().set(PARAM_QUERY, queryString);
		}

		PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), 
				getIterationSelection(), backlog, queryString, backlogQueryString);
			
		CharSequence url = RequestCycle.get().urlFor(IssueBoardsPage.class, params);
		pushState(target, url.toString(), queryInput.getModelObject());
		
		target.add(body);
		resizeWindow(target);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getBoard() != null) {
			contentFrag = new Fragment("content", "hasBoardsFrag", this);
			
			Form<?> form = new Form<Void>("query");
			
			form.add(new DropdownLink("board") {

				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", getBoard().getName()));
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment menuFragment = new Fragment(id, "boardMenuFrag", IssueBoardsPage.this);
					if (SecurityUtils.canManageIssues(getProject())) {
						menuFragment.add(AttributeAppender.append("class", "administrative"));
					} 
					menuFragment.add(new Link<Void>("useDefault") {

						@Override
						public void onClick() {
							var oldAuditContent = VersionedXmlDoc.fromBean(getProject().getIssueSetting().getBoardSpecs()).toXML();
							getProject().getIssueSetting().setBoardSpecs(null);
							var newAuditContent = VersionedXmlDoc.fromBean(getProject().getIssueSetting().getBoardSpecs()).toXML();
							getProjectService().update(getProject());
							auditService.audit(getProject(), "changed issue boards", oldAuditContent, newAuditContent);
							setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject()));
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(SecurityUtils.canManageIssues(getProject()) 
									&& getProject().getIssueSetting().getBoardSpecs() != null);
						}
						
					}.add(new ConfirmClickModifier(_T("This will discard all project specific boards, do you want to continue?"))));
					
					menuFragment.add(new ListView<BoardSpec>("boards", boards) {

						@Override
						protected void populateItem(ListItem<BoardSpec> item) {
							item.add(new WebMarkupContainer("dragIndicator").setVisible(SecurityUtils.canManageIssues(getProject())));
							
							PageParameters params = IssueBoardsPage.paramsOf(
										getProject(), item.getModelObject(), getIterationSelection(), backlog, 
										backlogQueryString, queryString);
							Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
							link.add(new Label("name", item.getModelObject().getName()));
							item.add(link);

							item.add(new WebMarkupContainer("default").setVisible(item.getIndex() == 0));
							
							WebMarkupContainer actions = new WebMarkupContainer("actions") {

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setVisible(SecurityUtils.canManageIssues(getProject()));
								}
								
							};
							item.add(actions);
							actions.add(new BoardEditLink("edit", item.getIndex()) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									super.onClick(target);
									dropdown.close();
								}

							});
							actions.add(new CreateBoardLink("copy", SerializationUtils.clone(item.getModelObject())) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									super.onClick(target);
									dropdown.close();
								}

							});
							
							actions.add(new Link<Void>("delete") {

								@Override
								public void onClick() {
									var iterationSelection = getIterationSelection();
									BoardSpec boardToRemove = item.getModelObject();
									BoardSpec currentBoard = getBoard();
									boards.remove(boardToRemove);
									getProject().getIssueSetting().setBoardSpecs(boards);
									getProjectService().update(getProject());
									var oldAuditContent = VersionedXmlDoc.fromBean(boardToRemove).toXML();
									auditService.audit(getProject(), "deleted issue board \"" + boardToRemove.getName() + "\"", oldAuditContent, null);
									
									BoardSpec nextBoard;
									if (boardToRemove.getName().equals(currentBoard.getName())) 
										nextBoard = null;
									else
										nextBoard = currentBoard;
									PageParameters params = IssueBoardsPage.paramsOf(getProject(), 
											nextBoard, iterationSelection, backlog, queryString,
											backlogQueryString);
									setResponsePage(IssueBoardsPage.class, params);
								}

							}.add(new ConfirmClickModifier(MessageFormat.format(_T("Do you really want to delete board \"{0}\"?"), item.getModelObject().getName()))));
						}
						
					});
					
					if (SecurityUtils.canManageProject(getProject())) {
						menuFragment.add(new SortBehavior() {
							
							@Override
							protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
								CollectionUtils.move(boards, from.getItemIndex(), to.getItemIndex());
								var oldAuditContent = VersionedXmlDoc.fromBean(getProject().getIssueSetting().getBoardSpecs()).toXML();
								getProject().getIssueSetting().setBoardSpecs(boards);
								var newAuditContent = VersionedXmlDoc.fromBean(getProject().getIssueSetting().getBoardSpecs()).toXML();
								getProjectService().update(getProject());
								auditService.audit(getProject(), "reordered issue boards", oldAuditContent, newAuditContent);
								target.add(menuFragment);
							}
							
						}.items(".board"));
					}
					
					menuFragment.add(new CreateBoardLink("newBoard", new BoardSpec()) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							super.onClick(target);
							dropdown.close();
						}
						
						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(SecurityUtils.canManageIssues(getProject()));
						}
						
					});
					
					menuFragment.setOutputMarkupId(true);
					
					return menuFragment;
				}
				
			});
			form.add(new AjaxLink<Void>("backlog") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					if (backlog)
						add(AttributeAppender.append("class", "active"));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					PageParameters params = paramsOf(getProject(), getBoard(), getIterationSelection(), 
							!backlog, queryString, backlogQueryString);
					setResponsePage(IssueBoardsPage.class, params);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(getIterationSelection().getIteration() != null);
				}
				
			});
			
			form.add(new DropdownLink("iteration") {

				private boolean showClosed;
				
				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					if (getIterationSelection() instanceof IterationSelection.All) {
						add(new Label("label", "<i>" + _T("All") + "</i>").setEscapeModelStrings(false));
						add(AttributeAppender.append("class", "btn-outline-secondary btn-hover-primary"));
					} else if (getIterationSelection() instanceof IterationSelection.Unscheduled) {
						add(new Label("label", "<i>" + _T("Unscheduled") + "</i>").setEscapeModelStrings(false));
						add(AttributeAppender.append("class", "btn-outline-secondary btn-hover-primary"));
					} else {
						var iteration = getIterationSelection().getIteration();
						add(new Label("label", iteration.getName()));
						if (iteration.getDueDay() != null
								&& iteration.getDueDay() < DateUtils.toLocalDate(new Date()).toEpochDay()
								&& !iteration.isClosed()) {
							add(AttributeAppender.append("class", "btn-danger"));
							add(AttributeAppender.replace("title", "Iteration is due"));
						} else {
							add(AttributeAppender.append("class", "btn-outline-secondary btn-hover-primary"));
						}
					}
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment menuFragment = new Fragment(id, "iterationMenuFrag", IssueBoardsPage.this);
					if (SecurityUtils.canManageProject(getProject()))
						menuFragment.add(AttributeAppender.append("class", "administrative"));

					menuFragment.add(new ListView<Iteration>("iterations", new LoadableDetachableModel<>() {

						@Override
						protected List<Iteration> load() {
							List<Iteration> iterationss = getIterations()
									.stream()
									.filter(it -> !it.isClosed())
									.collect(toList());
							if (getIterationSelection().getIteration() != null && getIterationSelection().getIteration().isClosed() || showClosed) {
								List<Iteration> closedIterations = getIterations()
										.stream()
										.filter(it -> it.isClosed())
										.collect(toList());
								iterationss.addAll(closedIterations);
							}
							return iterationss;
						}

					}) {

						private void toggleClose(Iteration iteration) {							
							iteration.setClosed(!iteration.isClosed());
							if (iteration.equals(getIterationSelection().getIteration())) {
								getIterationService().createOrUpdate(iteration);
								setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject(), getBoard(), 
										getIterationSelection(), backlog, queryString, backlogQueryString));
							} else {
								getIterationService().createOrUpdate(iteration);
							}
							if (iteration.isClosed()) 
								auditService.audit(iteration.getProject(), "closed iteration \"" + iteration.getName() + "\"", null, null);
							else
								auditService.audit(iteration.getProject(), "reopened iteration \"" + iteration.getName() + "\"", null, null);

							dropdown.close();
							if (iteration.isClosed())
								Session.get().success(MessageFormat.format(_T("Iteration \"{0}\" is closed"), iteration.getName()));
							else
								Session.get().success(MessageFormat.format(_T("Iteration \"{0}\" is reopened"), iteration.getName()));
						}
						
						@Override
						protected void populateItem(ListItem<Iteration> item) {
							Iteration iteration = item.getModelObject();
							PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), 
									new IterationSelection.Specified(iteration), backlog, queryString, backlogQueryString);
							Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
							link.add(new Label("name", iteration.getName()));
							item.add(link);
							item.add(new WebMarkupContainer("inherited") {
								
								@Override
								protected void onConfigure() {
									super.onConfigure();
									setVisible(!item.getModelObject().getProject().equals(getProject()));
								}
								
							});
							item.add(new BookmarkablePageLink<Void>("burndown", IterationBurndownPage.class, 
									IterationBurndownPage.paramsOf(getProject(), iteration)) {
								@Override
								protected void onConfigure() {
									super.onConfigure();
									var iteration = item.getModelObject();
									setVisible(iteration.getStartDay() != null && iteration.getDueDay() != null);
								}
							});
							
							item.add(new IterationStatusLabel("status", item.getModel()));
							item.add(new IterationDateLabel("dueDate", item.getModel()));

							if (SecurityUtils.canManageIssues(getProject()) 
									&& item.getModelObject().getProject().equals(getProject())) {
								Fragment fragment = new Fragment("actions", "iterationActionsFrag", IssueBoardsPage.this);
								
								fragment.add(new AjaxLink<Void>("close") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										toggleClose(item.getModelObject());
									}

									@Override
									protected void onConfigure() {
										super.onConfigure();
										setVisible(!item.getModelObject().isClosed());
									}
									
								});
								fragment.add(new AjaxLink<Void>("reopen") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										toggleClose(item.getModelObject());
									}

									@Override
									protected void onConfigure() {
										super.onConfigure();
										setVisible(item.getModelObject().isClosed());
									}
									
								});
								
								var bean = IterationEditBean.of(item.getModelObject(), getBoard().getIterationPrefix());
								fragment.add(new AjaxLink<Void>("edit") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										dropdown.close();
										
										new BeanEditModalPanel<>(target, bean, _T("Edit Iteration")) {

											@Override
											protected String onSave(AjaxRequestTarget target, IterationEditBean bean) {
												var iteration = item.getModelObject();
												var oldAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
												bean.update(iteration);
												var newAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
												getIterationService().createOrUpdate(iteration);
												auditService.audit(iteration.getProject(), "changed iteration \"" + iteration.getName() + "\"", oldAuditContent, newAuditContent);
												setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
														getProject(), getBoard(), new IterationSelection.Specified(iteration), 
														backlog, queryString, backlogQueryString));
												return null;
											}
										};
									}

								});
								fragment.add(new AjaxLink<Void>("delete") {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmClickListener(
												MessageFormat.format(_T("Do you really want to delete iteration \"{0}\"?"), item.getModelObject().getName())));
									}
									
									@Override
									public void onClick(AjaxRequestTarget target) {
										dropdown.close();
										Iteration iteration = item.getModelObject();
										if (iteration.equals(getIterationSelection().getIteration())) {
											getIterationService().delete(iteration);
											PageParameters params = IssueBoardsPage.paramsOf(
													getProject(), getBoard(), new IterationSelection.Unscheduled(), 
													backlog, queryString, backlogQueryString);
											setResponsePage(IssueBoardsPage.class, params);
										} else {
											getIterationService().delete(iteration);
										}
										var oldAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
										auditService.audit(iteration.getProject(), "deleted iteration \"" + iteration.getName() + "\"", oldAuditContent, null);
										Session.get().success(MessageFormat.format(_T("Iteration \"{0}\" deleted"), iteration.getName()));
									}

								});
								
								item.add(fragment);
							} else {
								item.add(new Label("actions", "&nbsp;").setEscapeModelStrings(false));
							}
							
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(!getModelObject().isEmpty());
						}
						
					});
					menuFragment.add(new AjaxLink<Void>("showClosed") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							showClosed = true;
							target.add(menuFragment);
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(!showClosed 
									&& (getIterationSelection().getIteration() == null || !getIterationSelection().getIteration().isClosed()) 
									&& getIterations().stream().anyMatch(Iteration::isClosed));
						}
						
					});
					
					PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), new IterationSelection.Unscheduled(),  
							backlog, queryString, backlogQueryString);
					menuFragment.add(new BookmarkablePageLink<Void>("unscheduled", IssueBoardsPage.class, params));

					params = IssueBoardsPage.paramsOf(getProject(), getBoard(), new IterationSelection.All(), 
							backlog, queryString, backlogQueryString);
					menuFragment.add(new BookmarkablePageLink<Void>("all", IssueBoardsPage.class, params) {
						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(!getIterations().isEmpty());
						}
					});
					
					menuFragment.add(new AjaxLink<Void>("newIteration") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							dropdown.close();
							var bean = IterationEditBean.ofNew(getProject(), getBoard().getIterationPrefix());
							new BeanEditModalPanel<>(target, bean, _T("Create Iteration")) {

								@Override
								protected String onSave(AjaxRequestTarget target, IterationEditBean bean) {
									var iteration = new Iteration();
									iteration.setProject(getProject());
									bean.update(iteration);
									getIterationService().createOrUpdate(iteration);
									var newAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
									auditService.audit(iteration.getProject(), "created iteration \"" + iteration.getName() + "\"", null, newAuditContent);
									setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
											getProject(), getBoard(), new IterationSelection.Specified(iteration), 
											backlog, queryString, backlogQueryString));
									return null;
								}

								@Override
								protected boolean isDirtyAware() {
									return false;
								}
							};
						}
						
						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(SecurityUtils.canManageIssues(getProject()));
						}
						
					});
					menuFragment.setOutputMarkupId(true);
					return menuFragment;
				}

			});		
			
			queryInput = new TextField<String>("input", new IModel<>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return backlog ? backlogQueryString : queryString;
				}

				@Override
				public void setObject(String object) {
					if (backlog)
						backlogQueryString = object;
					else
						queryString = object;
				}

			});
			
			IssueQueryParseOption option = new IssueQueryParseOption()
					.withCurrentUserCriteria(true).withCurrentProjectCriteria(true).forBoard(true);
			queryInput.add(new IssueQueryBehavior(projectModel, option));
			
			queryInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					doQuery(target);
				}
				
			});
			
			if (backlog)
				queryInput.add(AttributeAppender.append("placeholder", _T("Filter backlog issues")));
			else
				queryInput.add(AttributeAppender.append("placeholder", _T("Filter issues")));
				
			form.add(queryInput);
			
			form.add(new AjaxButton("submit") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					doQuery(target);
				}
				
			});
			
			contentFrag.add(form);
			
			body = new WebMarkupContainer("body");
			body.setOutputMarkupId(true);
			body.add(new FencedFeedbackPanel("feedback", contentFrag));
			contentFrag.add(body);
			
			RepeatingView columnsView = new RepeatingView("columns");
			if (backlog) {
				columnsView.add(new BacklogColumnPanel("backlog") {

					@Override
					protected ProjectScope getProjectScope() {
						return IssueBoardsPage.this.getProjectScope();
					}

					@Override
					protected IssueQuery getBacklogQuery() {
						return backlogQueryModel.getObject();
					}

					@Override
					protected IterationSelection getIterationSelection() {
						return IssueBoardsPage.this.getIterationSelection();
					}

					@Override
					protected String getIterationPrefix() {
						return getBoard().getIterationPrefix();
					}

				});
			}
			
			for (String column: getBoard().getColumns()) {
				columnsView.add(new BoardColumnPanel(columnsView.newChildId()) {

					@Override
					protected ProjectScope getProjectScope() {
						return IssueBoardsPage.this.getProjectScope();
					}

					@Override
					protected BoardSpec getBoard() {
						return IssueBoardsPage.this.getBoard();
					}

					@Override
					protected IterationSelection getIterationSelection() {
						return IssueBoardsPage.this.getIterationSelection();
					}

					@Override
					protected String getIterationPrefix() {
						return getBoard().getIterationPrefix();
					}

					@Override
					protected String getColumn() {
						return column;
					}

					@Override
					protected IssueQuery getBoardQuery() {
						return queryModel.getObject();
					}
					
				});
			}
			body.add(columnsView);
		} else {
			contentFrag = new Fragment("content", "noBoardsFrag", this);
			contentFrag.add(new CreateBoardLink("newBoard", new BoardSpec()) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canManageIssues(getProject()));
				}
				
			});
			contentFrag.add(new Link<Void>("useDefault") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canManageIssues(getProject()) 
							&& getProject().getIssueSetting().getBoardSpecs() != null);
				}

				@Override
				public void onClick() {
					var oldAuditContent = VersionedXmlDoc.fromBean(getProject().getIssueSetting().getBoardSpecs()).toXML();
					getProject().getIssueSetting().setBoardSpecs(null);
					var newAuditContent = VersionedXmlDoc.fromBean(getProject().getIssueSetting().getBoardSpecs()).toXML();
					getProjectService().update(getProject());
					auditService.audit(getProject(), "changed issue boards", oldAuditContent, newAuditContent);
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject()));
				}
				
			});
		}
		contentFrag.setOutputMarkupId(true);
		add(contentFrag);
	}

	private ProjectScope getProjectScope() {
		return new ProjectScope(getProject(), true, true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueBoardsResourceReference()));
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		if (backlog) {
			backlogQueryString = (String) data; 
			getPageParameters().set(PARAM_BACKLOG_QUERY, backlogQueryString);
		} else {
			queryString = (String) data;
			getPageParameters().set(PARAM_QUERY, queryString);
		}
		
		target.add(contentFrag);
	}
	
	private List<Iteration> getIterations() {
		return getProject().getSortedHierarchyIterations().stream()
				.filter(it -> getBoard().getIterationPrefix() == null || it.getName().startsWith(getBoard().getIterationPrefix()))
				.collect(toList());		
	}
	
	public static PageParameters paramsOf(Project project, @Nullable BoardSpec board,
										  IterationSelection iterationSelection, boolean backlog,
										  @Nullable String query, @Nullable String backlogQuery) {
		PageParameters params = paramsOf(project);
		if (board != null)
			params.add(PARAM_BOARD, board.getName());
		if (iterationSelection instanceof IterationSelection.All)
			params.add(PARAM_ITERATION, ITERATION_ALL);
		else if (iterationSelection instanceof IterationSelection.Unscheduled)
			params.add(PARAM_ITERATION, ITERATION_UNSCHEDULED);
		else
			params.add(PARAM_ITERATION, iterationSelection.getIteration().getId());
		params.add(PARAM_BACKLOG, backlog);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (backlogQuery != null)
			params.add(PARAM_BACKLOG_QUERY, backlogQuery);
		return params;
	}
	
	private class CreateBoardLink extends ModalLink {

		private final BoardSpec newBoard;
		
		public CreateBoardLink(String id, BoardSpec newBoard) {
			super(id);
			this.newBoard = newBoard;
		}

		@Override
		protected Component newContent(String id, ModalPanel modal) {
			return new NewBoardPanel(id, boards, newBoard) {

				@Override
				protected Project getProject() {
					return IssueBoardsPage.this.getProject();
				}

				@Override
				protected void onBoardCreated(AjaxRequestTarget target, BoardSpec board) {
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), board, getIterationSelection(), backlog, queryString, 
							backlogQueryString));
					modal.close();
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					modal.close();
				}
				
			};
		}
		
	}
	
	private abstract class BoardEditLink extends ModalLink {

		private final int boardIndex;
		
		public BoardEditLink(String id, int boardIndex) {
			super(id);
			this.boardIndex = boardIndex;
		}

		@Override
		protected Component newContent(String id, ModalPanel modal) {
			var oldAuditContent = VersionedXmlDoc.fromBean(boards.get(boardIndex)).toXML();
			return new BoardEditPanel(id, boards, boardIndex) {

				@Override
				protected void onSave(AjaxRequestTarget target, BoardSpec board) {
					getProject().getIssueSetting().setBoardSpecs(boards);
					var newAuditContent = VersionedXmlDoc.fromBean(board).toXML();
					getProjectService().update(getProject());
					auditService.audit(getProject(), "changed issue board \"" + board.getName() + "\"", oldAuditContent, newAuditContent);
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), board, getIterationSelection(), backlog, queryString, 
							backlogQueryString));
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					modal.close();
				}

			};
		}
	}	
	
	private static class QueryParseMessage implements Serializable {
		
		final boolean backlog;
		
		final String template;
		
		public QueryParseMessage(boolean backlog, String template) {
			this.backlog = backlog;
			this.template = template;
		}

		@Override
		public String toString() {
			return String.format(template, backlog?_T("backlog "):"");
		}
		
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Issue Boards"));
	}
	
	@Override
	protected String getPageTitle() {
		return _T("Issue Boards") + " - " + getProject().getPath();
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, IssueBoardsPage.class, IssueBoardsPage.paramsOf(project));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
