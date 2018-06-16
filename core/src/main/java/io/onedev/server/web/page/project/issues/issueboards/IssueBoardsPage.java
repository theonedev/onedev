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

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueBoard;
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

	private static final String PARAM_BOARD = "board";
	
	private static final String PARAM_MILESTONE = "milestone";
	
	private static final String PARAM_QUERY = "query";
	
	private final IModel<IssueBoard> boardModel;
	
	private final IModel<Milestone> milestoneModel;
	
	private final String query;
	
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
		
		query = params.get(PARAM_QUERY).toString();
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
							
							PageParameters params = IssueBoardsPage.paramsOf(getProject(), item.getModelObject(), getMilestone(), query);
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
										params = IssueBoardsPage.paramsOf(getProject(), null, getMilestone(), query);
									else
										params = IssueBoardsPage.paramsOf(getProject(), getBoard(), getMilestone(), query);
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
							PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), milestone, query);
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
			
			TextField<String> input = new TextField<String>("input", Model.of(query));
			input.add(new IssueQueryBehavior(projectModel));
			form.add(input);

			form.add(new Button("submit") {

				@Override
				public void onSubmit() {
					super.onSubmit();
					PageParameters params = IssueBoardsPage.paramsOf(getProject(), getBoard(), getMilestone(), input.getModelObject());
					setResponsePage(IssueBoardsPage.class, params);
				}
				
			});
			
			fragment.add(form);
			
			fragment.add(new BookmarkablePageLink<Void>("newBoard", NewBoardPage.class, NewBoardPage.paramsOf(getProject())) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canManage(getProject()));
				}
				
			});
			
			fragment.add(new BoardColumnPanel("backlog") {

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
				protected int getColumnIndex() {
					return -1;
				}
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(getMilestone() != null);
				}
				
			});
			
			RepeatingView columnsView = new RepeatingView("columns");
			for (int i=0; i<getBoard().getColumns().size(); i++) {
				int index = i;
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
					protected int getColumnIndex() {
						return index;
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
			@Nullable Milestone milestone, @Nullable String query) {
		PageParameters params = paramsOf(project);
		if (board != null)
			params.add(PARAM_BOARD, board.getName());
		if (milestone != null)
			params.add(PARAM_MILESTONE, milestone.getName());
		if (query != null)
			params.add(PARAM_QUERY, query);
		return params;
	}
	
}
