package io.onedev.server.web.page.project.issues.boards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.board.BoardEditPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.milestone.MilestoneDueLabel;
import io.onedev.server.web.component.milestone.MilestoneStatusLabel;
import io.onedev.server.web.component.milestone.closelink.MilestoneCloseLink;
import io.onedev.server.web.component.milestone.deletelink.MilestoneDeleteLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.model.EntityModel;

@SuppressWarnings("serial")
public class IssueBoardsPage extends IssuesPage {

	private static final Logger logger = LoggerFactory.getLogger(IssueBoardsPage.class);
	
	private static final String PARAM_BOARD = "board";
	
	private static final String PARAM_MILESTONE = "milestone";
	
	private static final String PARAM_BACKLOG = "backlog";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_BACKLOG_QUERY = "backlog-query";

	private final List<BoardSpec> boards;
	
	private final int boardIndex;
	
	private final IModel<Milestone> milestoneModel;
	
	private final boolean backlog;
	
	private final String query;
	
	private final String backlogQuery;
	
	private RepeatingView columnsView;
	
	private final IModel<IssueQuery> parsedQueryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			return parse(false, getBoard().getBaseQuery(), query);
		}
		
	};
	
	private final IModel<IssueQuery> parsedBacklogQueryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			return parse(true, getBoard().getBacklogBaseQuery(), backlogQuery);
		}
		
	};
	
	private NotificationPanel feedback;
	
	private IssueQuery parse(boolean backlog, @Nullable String baseQueryString, @Nullable String additionalQueryString) {
		IssueQuery additionalQuery;
		try {
			additionalQuery = IssueQuery.parse(getProject(), additionalQueryString, true);
		} catch (Exception e) {
			String prefix;
			if (backlog)
				prefix = "Error parsing backlog query: ";
			else
				prefix = "Error parsing issue query: ";
			logger.error(prefix + additionalQueryString, e);
			error(prefix + e.getMessage());
			return null;
		}			

		if (SecurityUtils.getUser() == null && additionalQuery.needsLogin()) { 
			error("Please login to perform this query");
			return null;
		} else { 
			IssueQuery baseQuery;
			try {
				baseQuery = IssueQuery.parse(getProject(), baseQueryString, true);
			} catch (Exception e) {
				String prefix;
				if (backlog)
					prefix = "Error parsing backlog base query: ";
				else
					prefix = "Error parsing base query: ";
					
				logger.error(prefix + baseQueryString, e);
				error(prefix + e.getMessage());
				return null;
			}
			if (SecurityUtils.getUser() == null && baseQuery.needsLogin()) {
				if (backlog)
					error("Login is required for backlog base query");
				else
					error("Login is required for base query");
				return null;
			} else {
				return IssueQuery.merge(baseQuery, additionalQuery);
			}
		} 
	}
	
	public IssueBoardsPage(PageParameters params) {
		super(params);
		
		boards = getProject().getIssueSetting().getBoardSpecs(true);
		
		String boardName = params.get(PARAM_BOARD).toString();
		if (boardName != null) {
			boardIndex = BoardSpec.getBoardIndex(boards, boardName);
			if (boardIndex == -1)
				throw new OneException("Can not find issue board: " + boardName);
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
				throw new OneException("Can not find milestone: " + milestoneName);
		} else {
			milestone = null;
			for (Milestone each: getMilestones()) {
				if (!each.isClosed()) {
					milestone = each;
					break;
				}
			}
			if (milestone == null) {
				for (Milestone each: getMilestones()) {
					if (each.isClosed()) {
						milestone = each;
						break;
					}
				}
			}
		}
		
		milestoneModel = new EntityModel<Milestone>(milestone);
		
		backlog = params.get(PARAM_BACKLOG).toBoolean() && getMilestone() != null;
		query = params.get(PARAM_QUERY).toString();
		backlogQuery = params.get(PARAM_BACKLOG_QUERY).toString();
	}
	
	private List<Milestone> getMilestones() {
		List<Milestone> milestones = new ArrayList<>(getProject().getMilestones());
		Collections.sort(milestones, new Comparator<Milestone>() {

			@Override
			public int compare(Milestone o1, Milestone o2) {
				return o1.getDueDate().compareTo(o2.getDueDate());
			}
			
		});
		return milestones;
	}

	@Override
	protected void onDetach() {
		milestoneModel.detach();
		parsedQueryModel.detach();
		parsedBacklogQueryModel.detach();
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
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getBoard() != null) {
			Fragment boardFragment = new Fragment("content", "hasBoardsFrag", this);

			Form<?> form = new Form<Void>("query");
			if (!SecurityUtils.canAdministrate(getProject().getFacade()))
				form.add(AttributeAppender.append("style", "margin-right: 0;"));
			
			form.add(new DropdownLink("board") {

				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", getBoard().getName()));
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment menuFragment = new Fragment(id, "boardMenuFrag", IssueBoardsPage.this);
					if (SecurityUtils.canAdministrate(getProject().getFacade())) {
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
							setVisible(SecurityUtils.canAdministrate(getProject().getFacade()) 
									&& getProject().getIssueSetting().getBoardSpecs(false) != null);
						}
						
					}.add(new ConfirmOnClick("This will discard all project specific boards, do you want to continue?")));
					
					menuFragment.add(new ListView<BoardSpec>("boards", boards) {

						@Override
						protected void populateItem(ListItem<BoardSpec> item) {
							item.add(new WebMarkupContainer("dragIndicator").setVisible(SecurityUtils.canAdministrate(getProject().getFacade())));
							
							PageParameters params = IssueBoardsPage.paramsOf(
									getProject(), item.getModelObject(), getMilestone(), 
									backlog, backlogQuery, query);
							Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
							link.add(new Label("name", item.getModelObject().getName()));
							item.add(link);
							
							WebMarkupContainer actions = new WebMarkupContainer("actions") {

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
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
									PageParameters params = IssueBoardsPage.paramsOf(getProject(), nextBoard, getMilestone(), backlog, query, backlogQuery);
									setResponsePage(IssueBoardsPage.class, params);
								}

							}.add(new ConfirmOnClick("Do you really want to delete board '" + item.getModelObject().getName() + "'?") ));
						}
						
					});
					
					if (SecurityUtils.canAdministrate(getProject().getFacade())) {
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
							setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
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
					PageParameters params = paramsOf(getProject(), getBoard(), getMilestone(), !backlog, query, backlogQuery);
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
				milestoneFragment.add(new DropdownLink("link") {

					private boolean showClosed;
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return getMilestone().getName() + " (" + DateUtils.formatDate(getMilestone().getDueDate()) + ")";
							}
							
						}));

						if (getMilestone().getDueDate().before(new Date()) && !getMilestone().isClosed()) {
							add(AttributeAppender.append("class", "btn-danger"));
							add(AttributeAppender.replace("title", "Milestone is due"));
						} else {
							add(AttributeAppender.append("class", "btn-default"));
						}
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						Fragment menuFragment = new Fragment(id, "milestoneMenuFrag", IssueBoardsPage.this);
						if (SecurityUtils.canAdministrate(getProject().getFacade()))
							menuFragment.add(AttributeAppender.append("class", "administrative"));

						menuFragment.add(new ListView<Milestone>("milestones", new LoadableDetachableModel<List<Milestone>>() {

							@Override
							protected List<Milestone> load() {
								List<Milestone> milestones = getMilestones().stream().filter(it->!it.isClosed()).collect(Collectors.toList());
								if (getMilestone().isClosed() || showClosed) {
									List<Milestone> closedMilestones = getMilestones().stream().filter(it->it.isClosed()).collect(Collectors.toList());
									Collections.reverse(closedMilestones);
									milestones.addAll(closedMilestones);
								}
								return milestones;
							}
							
						}) {

							@Override
							protected void populateItem(ListItem<Milestone> item) {
								Milestone milestone = item.getModelObject();
								PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), milestone, backlog, query, backlogQuery);
								Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
								link.add(new Label("name", milestone.getName()));
								item.add(link);
								
								item.add(new MilestoneStatusLabel("status", item.getModel()));
								item.add(new MilestoneDueLabel("dueDate", item.getModel()));
								WebMarkupContainer actions = new WebMarkupContainer("actions") {

									@Override
									protected void onConfigure() {
										super.onConfigure();
										setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
									}
									
								};
								item.add(actions);
								
								actions.add(new MilestoneCloseLink("close") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										super.onClick(target);
										dropdown.close();
									}

									@Override
									protected Milestone getMilestone() {
										return item.getModelObject();
									}

									@Override
									protected void onConfigure() {
										super.onConfigure();
										setVisible(!item.getModelObject().isClosed());
									}
									
									@Override
									protected void onMilestoneClosed(AjaxRequestTarget target) {
										setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
												getProject(), getBoard(), null, backlog, query, backlogQuery));
									}
									
								});
								actions.add(new AjaxLink<Void>("reopen") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										Milestone milestone = getMilestone();
										milestone.setClosed(false);
										OneDev.getInstance(MilestoneManager.class).save(milestone);
										setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
												getProject(), getBoard(), null, backlog, query, backlogQuery));
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
								actions.add(new MilestoneDeleteLink("delete") {

									@Override
									public void onClick(AjaxRequestTarget target) {
										super.onClick(target);
										dropdown.close();
									}

									@Override
									protected Milestone getMilestone() {
										return item.getModelObject();
									}

									@Override
									protected void onMilestoneDeleted(AjaxRequestTarget target) {
										Milestone nextMilestone;
										if (item.getModelObject().equals(IssueBoardsPage.this.getMilestone()))
											nextMilestone = null;
										else
											nextMilestone = IssueBoardsPage.this.getMilestone();
										setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
												getProject(), getBoard(), nextMilestone, backlog, query, backlogQuery));
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
										&& getMilestones().stream().anyMatch(it->it.isClosed()));
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
								setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
							}
							
						});
						menuFragment.setOutputMarkupId(true);
						return menuFragment;
					}

				});		
				form.add(milestoneFragment);
			} else if (SecurityUtils.canAdministrate(getProject().getFacade())) {
				form.add(new CreateMilestoneLink("milestone") {

					@Override
					public IModel<?> getBody() {
						return Model.of("<i class=\"fa fa-plus\"></i> Add Milestone");
					}
					
				}.setEscapeModelStrings(false).add(AttributeAppender.append("class", "btn btn-default")));
			} else {
				form.add(new WebMarkupContainer("milestone").setVisible(false));
			}
			
			IModel<String> model;
			if (backlog)
				model = Model.of(backlogQuery);
			else
				model = Model.of(query);
			TextField<String> input = new TextField<String>("input", model);
			input.add(new IssueQueryBehavior(projectModel));
			if (backlog)
				input.add(AttributeAppender.append("placeholder", "Filter backlog issues"));
			else
				input.add(AttributeAppender.append("placeholder", "Filter issues"));
				
			form.add(input);

			form.add(new Button("submit") {

				@Override
				public void onSubmit() {
					super.onSubmit();
					PageParameters params;
					if (backlog) {
						params = IssueBoardsPage.paramsOf(getProject(), getBoard(), 
								getMilestone(), backlog, query, input.getModelObject());
					} else {
						params = IssueBoardsPage.paramsOf(getProject(), getBoard(), 
								getMilestone(), backlog, input.getModelObject(), backlogQuery);
					}
						
					setResponsePage(IssueBoardsPage.class, params);
				}
				
			});
			
			boardFragment.add(form);
			
			boardFragment.add(feedback = new NotificationPanel("feedback", IssueBoardsPage.this));
			feedback.setOutputMarkupPlaceholderTag(true);
			
			columnsView = new RepeatingView("columns");
			if (backlog) {
				columnsView.add(new BacklogColumnPanel("backlog") {

					@Override
					protected Project getProject() {
						return IssueBoardsPage.this.getProject();
					}

					@Override
					protected IssueQuery getBacklogQuery() {
						return parsedBacklogQueryModel.getObject();
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
						return parsedQueryModel.getObject();
					}

				});
			}
			boardFragment.add(columnsView);
			
			add(boardFragment);
		} else {
			Fragment fragment = new Fragment("content", "noBoardsFrag", this);
			fragment.add(new CreateBoardLink("newBoard") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canAdministrate(getProject().getFacade()));
				}
				
			});
			fragment.add(new Link<Void>("useDefault") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canAdministrate(getProject().getFacade()) 
							&& getProject().getIssueSetting().getBoardSpecs(false) != null);
				}

				@Override
				public void onClick() {
					getProject().getIssueSetting().setBoardSpecs(null);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject()));
				}
				
			});
			add(fragment);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueBoardsResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueBoards.onDomReady();"));
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
							getProject(), board, getMilestone(), backlog, query, backlogQuery));
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
							getProject(), getBoard(), milestone, backlog, query, backlogQuery));
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
							getProject(), getBoard(), milestone, backlog, query, backlogQuery));
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
				protected void onBoardSaved(AjaxRequestTarget target, BoardSpec board) {
					getProject().getIssueSetting().setBoardSpecs(boards);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(
							getProject(), board, getMilestone(), backlog, query, backlogQuery));
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					modal.close();
				}

			};
		}
	}	
}
