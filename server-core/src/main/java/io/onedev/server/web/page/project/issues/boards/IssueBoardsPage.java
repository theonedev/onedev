package io.onedev.server.web.page.project.issues.boards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
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
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.board.BoardEditPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.milestone.MilestoneDueLabel;
import io.onedev.server.web.component.milestone.MilestoneStatusLabel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.orderedit.OrderEditPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public class IssueBoardsPage extends ProjectIssuesPage {

	private static final String PARAM_BOARD = "board";
	
	private static final String PARAM_MILESTONE = "milestone";
	
	private static final String PARAM_BACKLOG = "backlog";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_BACKLOG_QUERY = "backlog-query";

	private final List<BoardSpec> boards;
	
	private final int boardIndex;
	
	private final IModel<Milestone> milestoneModel;
	
	private final boolean backlog;
	
	private String queryString;
	
	private String backlogQueryString;
	
	private Fragment contentFrag;
	
	private WebMarkupContainer body;
	
	private TextField<String> queryInput;
	
	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			return parse(false, getBoard().getBaseQuery(), queryString);
		}
		
	};
	
	private final IModel<IssueQuery> backlogQueryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			return parse(true, getBoard().getBacklogBaseQuery(), backlogQueryString);
		}
		
	};
	
	private IFeedbackMessageFilter newFeedbackMessageFilter(boolean backlog) {
		return new IFeedbackMessageFilter() {
			
			@Override
			public boolean accept(FeedbackMessage message) {
				return ((QueryParseMessage)message.getMessage()).backlog == backlog;
			}
			
		};		
	}
	
	@Nullable
	private IssueQuery parse(boolean backlog, @Nullable String baseQueryString, @Nullable String queryString) {
		contentFrag.getFeedbackMessages().clear(newFeedbackMessageFilter(backlog));
		
		IssueQuery query;
		try {
			query = IssueQuery.parse(getProject(), queryString, true, true, false, false, false);
		} catch (ExplicitException e) {
			contentFrag.error(new QueryParseMessage(backlog, "Error parsing %squery: " + e.getMessage()));
			return null;
		} catch (Exception e) {
			contentFrag.warn(new QueryParseMessage(backlog, "Not a valid %sformal query, performing fuzzy query"));
			try {
				EntityQuery.getProjectScopedNumber(getProject(), queryString);
				query = new IssueQuery(new NumberCriteria(getProject(), queryString, IssueQueryLexer.Is));
			} catch (Exception e2) {
				query = new IssueQuery(new TitleCriteria(queryString));
			}
		}

		IssueQuery baseQuery;
		try {
			baseQuery = IssueQuery.parse(getProject(), baseQueryString, true, true, false, false, false);
		} catch (ExplicitException e) {
			contentFrag.error(new QueryParseMessage(backlog, "Error parsing %sbase query: " + e.getMessage()));
			return null;
		} catch (Exception e) {
			contentFrag.error(new QueryParseMessage(backlog, "Malformed %sbase query: " + e.getMessage()));
			return null;
		}
		return IssueQuery.merge(baseQuery, query);
	}
	
	public IssueBoardsPage(PageParameters params) {
		super(params);
		
		boards = getProject().getIssueSetting().getBoardSpecs(true);
		
		String boardName = params.get(PARAM_BOARD).toString();
		if (StringUtils.isNotBlank(boardName)) {
			boardIndex = BoardSpec.getBoardIndex(boards, boardName);
			if (boardIndex == -1)
				throw new ExplicitException("Can not find issue board: " + boardName);
		} else if (!boards.isEmpty()) {
			boardIndex = 0;
		} else {
			boardIndex = -1;
		}

		Milestone milestone;
		String milestoneName = params.get(PARAM_MILESTONE).toString();
		if (milestoneName != null) {
			milestone = getProject().getMilestone(milestoneName);
			if (milestone == null)
				throw new ExplicitException("Can not find milestone: " + milestoneName);
		} else if (!getProject().getSortedMilestones().isEmpty()) {
			milestone = getProject().getSortedMilestones().iterator().next();
		} else {
			milestone = null;
		}

		Long milestoneId = Milestone.idOf(milestone);
		milestoneModel = new LoadableDetachableModel<Milestone>() {

			@Override
			protected Milestone load() {
				if (milestoneId != null)
					return getMilestoneManager().load(milestoneId);
				else
					return null;
			}
			
		};
		
		backlog = params.get(PARAM_BACKLOG).toBoolean() && getMilestone() != null;
		queryString = params.get(PARAM_QUERY).toString();
		backlogQueryString = params.get(PARAM_BACKLOG_QUERY).toString();
	}
	
	private MilestoneManager getMilestoneManager() {
		return OneDev.getInstance(MilestoneManager.class);
	}
	
	@Override
	protected void onDetach() {
		milestoneModel.detach();
		queryModel.detach();
		backlogQueryModel.detach();
		super.onDetach();
	}

	@Nullable
	public BoardSpec getBoard() {
		if (boardIndex != -1)
			return boards.get(boardIndex);
		else
			return null;
	}
	
	@Nullable
	public Milestone getMilestone() {
		return milestoneModel.getObject();
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
				getMilestone(), backlog, queryString, backlogQueryString);
			
		CharSequence url = RequestCycle.get().urlFor(IssueBoardsPage.class, params);
		pushState(target, url.toString(), queryInput.getModelObject());
		
		target.add(body);
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
							getProject().getIssueSetting().setBoardSpecs(null);
							OneDev.getInstance(ProjectManager.class).save(getProject());
							setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject()));
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(SecurityUtils.canManageIssues(getProject()) 
									&& getProject().getIssueSetting().getBoardSpecs(false) != null);
						}
						
					}.add(new ConfirmClickModifier("This will discard all project specific boards, do you want to continue?")));
					
					menuFragment.add(new ListView<BoardSpec>("boards", boards) {

						@Override
						protected void populateItem(ListItem<BoardSpec> item) {
							item.add(new WebMarkupContainer("dragIndicator").setVisible(SecurityUtils.canManageIssues(getProject())));
							
							PageParameters params = IssueBoardsPage.paramsOf(
									getProject(), item.getModelObject(), getMilestone(), 
									backlog, backlogQueryString, queryString);
							Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
							link.add(new Label("name", item.getModelObject().getName()));
							item.add(link);

							item.add(new WebMarkupContainer("primary").setVisible(item.getIndex() == 0));
							
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
							
							actions.add(new Link<Void>("delete") {

								@Override
								public void onClick() {
									BoardSpec boardToRemove = item.getModelObject();
									BoardSpec currentBoard = getBoard();
									boards.remove(boardToRemove);
									getProject().getIssueSetting().setBoardSpecs(boards);
									OneDev.getInstance(ProjectManager.class).save(getProject());
									
									BoardSpec nextBoard;
									if (boardToRemove.getName().equals(currentBoard.getName())) 
										nextBoard = null;
									else
										nextBoard = currentBoard;
									PageParameters params = IssueBoardsPage.paramsOf(getProject(), nextBoard, getMilestone(), backlog, queryString, backlogQueryString);
									setResponsePage(IssueBoardsPage.class, params);
								}

							}.add(new ConfirmClickModifier("Do you really want to delete board '" + item.getModelObject().getName() + "'?") ));
						}
						
					});
					
					if (SecurityUtils.canManage(getProject())) {
						menuFragment.add(new SortBehavior() {
							
							@Override
							protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
								BoardSpec board = boards.get(from.getItemIndex());
								boards.set(from.getItemIndex(), boards.set(to.getItemIndex(), board));
								getProject().getIssueSetting().setBoardSpecs(boards);
								OneDev.getInstance(ProjectManager.class).save(getProject());
								target.add(menuFragment);
							}
							
						}.items(".board"));
					}
					
					menuFragment.add(new CreateBoardLink("newBoard") {

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
			form.add(new Link<Void>("backlog") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					if (backlog)
						add(AttributeAppender.append("class", "active"));
				}

				@Override
				public void onClick() {
					PageParameters params = paramsOf(getProject(), getBoard(), getMilestone(), !backlog, queryString, backlogQueryString);
					setResponsePage(IssueBoardsPage.class, params);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(getMilestone() != null);
				}
				
			});
			if (getMilestone() != null) {
				Fragment milestoneFragment = new Fragment("milestone", "hasMilestoneFrag", this);
				milestoneFragment.setRenderBodyOnly(true);
				milestoneFragment.add(new DropdownLink("link") {

					private boolean showClosed;
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return getMilestone().getName();
							}
							
						}));

						if (getMilestone().getDueDate() != null 
								&& getMilestone().getDueDate().before(new Date()) 
								&& !getMilestone().isClosed()) {
							add(AttributeAppender.append("class", "btn-danger"));
							add(AttributeAppender.replace("title", "Milestone is due"));
						} else {
							add(AttributeAppender.append("class", "btn-outline-secondary btn-hover-primary"));
						}
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						Fragment menuFragment = new Fragment(id, "milestoneMenuFrag", IssueBoardsPage.this);
						if (SecurityUtils.canManage(getProject()))
							menuFragment.add(AttributeAppender.append("class", "administrative"));

						menuFragment.add(new ListView<Milestone>("milestones", new LoadableDetachableModel<List<Milestone>>() {

							@Override
							protected List<Milestone> load() {
								List<Milestone> milestones = getProject().getSortedMilestones().stream().filter(it->!it.isClosed()).collect(Collectors.toList());
								if (getMilestone().isClosed() || showClosed) {
									List<Milestone> closedMilestones = getProject().getSortedMilestones().stream().filter(it->it.isClosed()).collect(Collectors.toList());
									milestones.addAll(closedMilestones);
								}
								return milestones;
							}
							
						}) {

							private void toggleClose(Milestone milestone) {
								milestone.setClosed(!milestone.isClosed());
								if (milestone.equals(IssueBoardsPage.this.getMilestone())) {
									getMilestoneManager().save(milestone);
									setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
											getProject(), getBoard(), milestone, backlog, queryString, backlogQueryString));
								} else {
									getMilestoneManager().save(milestone);
								}
								dropdown.close();
								if (milestone.isClosed())
									Session.get().success("Milestone '" + milestone.getName() + "' is closed");
								else
									Session.get().success("Milestone '" + milestone.getName() + "' is reopened");
							}
							
							@Override
							protected void populateItem(ListItem<Milestone> item) {
								Milestone milestone = item.getModelObject();
								PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), milestone, backlog, queryString, backlogQueryString);
								Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
								link.add(new Label("name", milestone.getName()));
								item.add(link);
								
								item.add(new MilestoneStatusLabel("status", item.getModel()));
								item.add(new MilestoneDueLabel("dueDate", item.getModel()));
								WebMarkupContainer actions = new WebMarkupContainer("actions") {

									@Override
									protected void onConfigure() {
										super.onConfigure();
										setVisible(SecurityUtils.canManageIssues(getProject()));
									}
									
								};
								item.add(actions);
								
								actions.add(new AjaxLink<Void>("close") {

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
								actions.add(new AjaxLink<Void>("reopen") {

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
								actions.add(new MilestoneEditLink("edit", item.getModelObject().getId()) {

									@Override
									public void onClick(AjaxRequestTarget target) {
										super.onClick(target);
										dropdown.close();
									}

								});
								actions.add(new AjaxLink<Void>("delete") {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmClickListener(
												"Do you really want to delete milestone '" + item.getModelObject().getName() + "'?"));
									}
									
									@Override
									public void onClick(AjaxRequestTarget target) {
										dropdown.close();
										Milestone milestone = item.getModelObject();
										if (milestone.equals(IssueBoardsPage.this.getMilestone())) {
											getMilestoneManager().delete(milestone);
											setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
													getProject(), getBoard(), null, backlog, queryString, backlogQueryString));
										} else {
											getMilestoneManager().delete(milestone);
										}
										Session.get().success("Milestone '" + milestone.getName() + "' deleted");
									}

								});
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
								setVisible(!showClosed && !getMilestone().isClosed() 
										&& getProject().getMilestones().stream().anyMatch(it->it.isClosed()));
							}
							
						});
						menuFragment.add(new CreateMilestoneLink("newMilestone") {

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
				form.add(milestoneFragment);
			} else if (SecurityUtils.canManageIssues(getProject())) {
				form.add(new CreateMilestoneLink("milestone") {

					@Override
					public IModel<?> getBody() {
						return Model.of(String.format("<svg class='icon mr-2'><use xlink:href='%s'/></svg>Add Milestone", 
								SpriteImage.getVersionedHref(IconScope.class, "plus")));
					}
					
				}.setEscapeModelStrings(false).add(AttributeAppender.append("class", "btn btn-outline-secondary bg-white btn-hover-primary")));
			} else {
				form.add(new WebMarkupContainer("milestone").setVisible(false));
			}
			
			queryInput = new TextField<String>("input", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return backlog?backlogQueryString:queryString;
				}

				@Override
				public void setObject(String object) {
					if (backlog)
						backlogQueryString = object;
					else
						queryString = object;
				}
				
			});
			
			queryInput.add(new IssueQueryBehavior(projectModel, true, true, false, false, false));
			
			queryInput.add(new AjaxFormComponentUpdatingBehavior("clear") {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					doQuery(target);
				}
				
			});
			
			if (backlog)
				queryInput.add(AttributeAppender.append("placeholder", "Filter backlog issues..."));
			else
				queryInput.add(AttributeAppender.append("placeholder", "Filter issues..."));
				
			form.add(queryInput);

			form.add(new DropdownLink("orderBy") {

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					List<String> orderFields = new ArrayList<>(Issue.ORDER_FIELDS.keySet());
					orderFields.remove(Issue.NAME_PROJECT);
					for (FieldSpec field: getIssueSetting().getFieldSpecs()) {
						if (field instanceof NumberField || field instanceof ChoiceField || field instanceof DateField) 
							orderFields.add(field.getName());
					}
					
					return new OrderEditPanel(id, orderFields, new IModel<List<EntitySort>> () {

						@Override
						public void detach() {
						}

						@Override
						public List<EntitySort> getObject() {
							IssueQuery query;
							
							if (backlog) 
								query = parse(true, null, backlogQueryString);
							else 
								query = parse(false, null, queryString);
							
							contentFrag.getFeedbackMessages().clear(newFeedbackMessageFilter(backlog));
							if (query != null) 
								return query.getSorts();
							else
								return new ArrayList<>();
						}

						@Override
						public void setObject(List<EntitySort> object) {
							IssueQuery query;
							
							if (backlog) 
								query = parse(true, null, backlogQueryString);
							else 
								query = parse(false, null, queryString);
							
							contentFrag.getFeedbackMessages().clear(newFeedbackMessageFilter(backlog));
							
							if (query == null)
								query = new IssueQuery();
							query.getSorts().clear();
							query.getSorts().addAll(object);
							
							if (backlog) {
								backlogQueryString = query.toString();
							} else {
								queryString = query.toString();
							}
							
							AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class); 
							target.add(queryInput);
							
							doQuery(target);
						}
						
					});
				}
				
			});	
			
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
					protected Project getProject() {
						return IssueBoardsPage.this.getProject();
					}

					@Override
					protected IssueQuery getBacklogQuery() {
						return backlogQueryModel.getObject();
					}

				});
			}
			
			for (String column: getBoard().getColumns()) {
				columnsView.add(new BoardColumnPanel(columnsView.newChildId()) {

					@Override
					protected Project getProject() {
						return IssueBoardsPage.this.getProject();
					}

					@Override
					protected BoardSpec getBoard() {
						return IssueBoardsPage.this.getBoard();
					}

					@Override
					protected Milestone getMilestone() {
						return IssueBoardsPage.this.getMilestone();
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
			contentFrag.add(new CreateBoardLink("newBoard") {

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
							&& getProject().getIssueSetting().getBoardSpecs(false) != null);
				}

				@Override
				public void onClick() {
					getProject().getIssueSetting().setBoardSpecs(null);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject()));
				}
				
			});
		}
		contentFrag.setOutputMarkupId(true);
		add(contentFrag);
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
	
	public static PageParameters paramsOf(Project project, @Nullable BoardSpec board, 
			@Nullable Milestone milestone, boolean backlog, @Nullable String query, 
			@Nullable String backlogQuery) {
		PageParameters params = paramsOf(project);
		if (board != null)
			params.add(PARAM_BOARD, board.getName());
		if (milestone != null)
			params.add(PARAM_MILESTONE, milestone.getName());
		params.add(PARAM_BACKLOG, backlog);
		if (query != null)
			params.add(PARAM_QUERY, query);
		if (backlogQuery != null)
			params.add(PARAM_BACKLOG_QUERY, backlogQuery);
		return params;
	}
	
	private class CreateBoardLink extends ModalLink {

		public CreateBoardLink(String id) {
			super(id);
		}

		@Override
		protected Component newContent(String id, ModalPanel modal) {
			return new NewBoardPanel(id, boards) {

				@Override
				protected Project getProject() {
					return IssueBoardsPage.this.getProject();
				}

				@Override
				protected void onBoardCreated(AjaxRequestTarget target, BoardSpec board) {
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), board, getMilestone(), backlog, queryString, backlogQueryString));
					modal.close();
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					modal.close();
				}
				
			};
		}
		
	}

	private class CreateMilestoneLink extends ModalLink {

		public CreateMilestoneLink(String id) {
			super(id);
		}

		@Override
		protected Component newContent(String id, ModalPanel modal) {
			return new NewMilestonePanel(id) {

				@Override
				protected Project getProject() {
					return IssueBoardsPage.this.getProject();
				}

				@Override
				protected void onMilestoneCreated(AjaxRequestTarget target, Milestone milestone) {
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), getBoard(), milestone, backlog, queryString, backlogQueryString));
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					modal.close();
				}
				
			};
		}
		
	}	
	
	private abstract class MilestoneEditLink extends ModalLink {

		private final Long milestoneId;
		
		public MilestoneEditLink(String id, Long milestoneId) {
			super(id);
			this.milestoneId = milestoneId;
		}

		@Override
		protected Component newContent(String id, ModalPanel modal) {
			return new MilestoneEditPanel(id, milestoneId) {

				@Override
				protected void onMilestoneSaved(AjaxRequestTarget target, Milestone milestone) {
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), getBoard(), milestone, backlog, queryString, backlogQueryString));
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
			return new BoardEditPanel(id, boards, boardIndex) {

				@Override
				protected void onSave(AjaxRequestTarget target, BoardSpec board) {
					getProject().getIssueSetting().setBoardSpecs(boards);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), board, getMilestone(), backlog, queryString, backlogQueryString));
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
			return String.format(template, backlog?"backlog ":"");
		}
		
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Issue Boards");
	}
	
	@Override
	protected String getPageTitle() {
		return "Issue Boards - " + getProject().getName();
	}
	
}
