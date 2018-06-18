package io.onedev.server.web.page.project.issues.issueboards;

import java.util.ArrayList;
import java.util.Comparator;
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
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.IssueQueryBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.MilestoneStatusLabel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.page.project.issues.IssuesPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class IssueBoardsPage extends IssuesPage {

	private static final Logger logger = LoggerFactory.getLogger(IssueBoardsPage.class);
	
	private static final String PARAM_BOARD = "board";
	
	private static final String PARAM_MILESTONE = "milestone";
	
	private static final String PARAM_BACKLOG = "backlog";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_BACKLOG_QUERY = "backlog-query";
	
	private final IModel<IssueBoard> boardModel;
	
	private final IModel<Milestone> milestoneModel;
	
	private final boolean backlog;
	
	private final String query;
	
	private final String backlogQuery;
	
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
		
		String boardName = params.get(PARAM_BOARD).toString();
		String milestoneName = params.get(PARAM_MILESTONE).toString();
		
		boardModel = new LoadableDetachableModel<IssueBoard>() {

			@Override
			protected IssueBoard load() {
				if (boardName != null) {
					for (IssueBoard board: getProject().getIssueBoards()) {
						if (board.getName().equals(boardName))
							return board;
					}
					throw new OneException("Can not find issue board: " + boardName);
				} else if (!getProject().getIssueBoards().isEmpty()) {
					return getProject().getIssueBoards().iterator().next();
				} else {
					return null;
				}
			}
			
		};
		
		milestoneModel = new LoadableDetachableModel<Milestone>() {

			@Override
			protected Milestone load() {
				if (milestoneName != null) {
					Milestone milestone = getProject().getMilestone(milestoneName);
					if (milestone != null)
						return milestone;
					else
						throw new OneException("Can not find milestone: " + milestoneName);
				} else {
					for (Milestone each: getMilestones()) {
						if (!each.isClosed())
							return each;
					}
					Milestone milestone = null;
					for (Milestone each: getMilestones()) {
						if (each.isClosed())
							milestone = each;
					}
					return milestone;
				}
			}
			
		};
		
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
		boardModel.detach();
		milestoneModel.detach();
		parsedQueryModel.detach();
		parsedBacklogQueryModel.detach();
		super.onDetach();
	}

	@Nullable
	private IssueBoard getBoard() {
		return boardModel.getObject();
	}
	
	@Nullable
	private Milestone getMilestone() {
		return milestoneModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getBoard() != null) {
			Fragment fragment = new Fragment("content", "hasBoardsFrag", this);

			Form<?> form = new Form<Void>("query");
			if (!SecurityUtils.canManage(getProject()))
				form.add(AttributeAppender.append("style", "margin-right: 0;"));
			
			form.add(new DropdownLink("boardMenu") {

				protected void onInitialize() {
					super.onInitialize();
					add(new Label("board", getBoard().getName()));
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment fragment = new Fragment(id, "boardMenuFrag", IssueBoardsPage.this);
					
					ArrayList<IssueBoard> boards = getProject().getIssueBoards();
					fragment.add(new ListView<IssueBoard>("boards", boards) {

						@Override
						protected void populateItem(ListItem<IssueBoard> item) {
							item.add(new WebMarkupContainer("dragHandle").setVisible(SecurityUtils.canManage(getProject())));
							
							PageParameters params = IssueBoardsPage.paramsOf(
									getProject(), item.getModelObject(), getMilestone(), 
									backlog, backlogQuery, query);
							Link<Void> link = new BookmarkablePageLink<Void>("select", IssueBoardsPage.class, params);
							link.add(new Label("name", item.getModelObject().getName()));
							item.add(link);
							
							params = BoardEditPage.paramsOf(getProject(), getBoard());
							item.add(new BookmarkablePageLink<Void>("edit", BoardEditPage.class, params) {

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setVisible(SecurityUtils.canManage(getProject()));
								}
								
							});
							
							item.add(new Link<Void>("delete") {

								@Override
								public void onClick() {
									getProject().getIssueBoards().remove(getBoard());
									OneDev.getInstance(ProjectManager.class).save(getProject());
									
									PageParameters params;
									if (item.getModelObject().getName().equals(getBoard().getName())) 
										params = IssueBoardsPage.paramsOf(getProject(), null, getMilestone(), backlog, query, backlogQuery);
									else
										params = IssueBoardsPage.paramsOf(getProject(), getBoard(), getMilestone(), backlog, query, backlogQuery);
									setResponsePage(IssueBoardsPage.class, params);
								}

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setVisible(SecurityUtils.canManage(getProject()));
								}
								
							}.add(new ConfirmOnClick("Do you really want to delete board '" + item.getModelObject().getName() + "'?") ));
							
							if (item.getModelObject().getName().equals(getBoard().getName()))
								item.add(AttributeAppender.append("class", "active"));
						}
						
					});
					
					fragment.add(new SortBehavior() {
						
						@Override
						protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
							IssueBoard board = boards.get(from.getItemIndex());
							boards.set(from.getItemIndex(), boards.set(to.getItemIndex(), board));
							getProject().setIssueBoards(boards);
							OneDev.getInstance(ProjectManager.class).save(getProject());
							target.add(fragment);
						}
						
					}.handle(".drag-handle").items(".board"));
					
					fragment.setOutputMarkupId(true);
					
					return fragment;
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
			form.add(new DropdownLink("milestoneMenu") {

				private boolean showClosed;
				
				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("milestone", new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return getMilestone().getName();
						}
						
					}));
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment fragment = new Fragment(id, "milestoneMenuFrag", IssueBoardsPage.this);

					fragment.add(new ListView<Milestone>("milestones", new LoadableDetachableModel<List<Milestone>>() {

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
							params = MilestoneDetailPage.paramsOf(milestone, null);
							item.add(new BookmarkablePageLink<Void>("detail", MilestoneDetailPage.class, params));
							if (item.getModelObject().getName().equals(getMilestone().getName()))
								item.add(AttributeAppender.append("class", "active"));
						}
						
					});
					fragment.add(new AjaxLink<Void>("showClosed") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							showClosed = true;
							target.add(fragment);
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(!showClosed && !getMilestone().isClosed() 
									&& getMilestones().stream().anyMatch(it->it.isClosed()));
						}
						
					});
					fragment.setOutputMarkupId(true);
					return fragment;
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(getMilestone() != null);
				}
				
			});
			
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
			
			fragment.add(form);
			
			fragment.add(new BookmarkablePageLink<Void>("newBoard", 
					NewBoardPage.class, NewBoardPage.paramsOf(getProject())) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canManage(getProject()));
				}
				
			});
			
			fragment.add(feedback = new NotificationPanel("feedback", IssueBoardsPage.this));
			feedback.setOutputMarkupPlaceholderTag(true);
			
			RepeatingView columnsView = new RepeatingView("columns");
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
					protected IssueBoard getBoard() {
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
			fragment.add(columnsView);
			
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "noBoardsFrag", this);
			fragment.add(new BookmarkablePageLink<Void>("newBoard", NewBoardPage.class, NewBoardPage.paramsOf(getProject())) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canManage(getProject()));
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
	
	public static PageParameters paramsOf(Project project, @Nullable IssueBoard board, 
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
	
}
