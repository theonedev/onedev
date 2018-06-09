package io.onedev.server.web.page.project.issues.issueboards;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.IssueBoard;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class IssueBoardsPage extends IssuesPage {

	private static final String PARAM_BOARD = "board";
	
	private static final String PARAM_MILESTONE = "milestone";
	
	public IssueBoardsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (!getProject().getIssueBoards().isEmpty()) {
			Fragment fragment = new Fragment("content", "hasBoardsFrag", this);
			
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
			@Nullable Milestone milestone) {
		PageParameters params = paramsOf(project);
		if (board != null)
			params.add(PARAM_BOARD, board.getName());
		if (milestone != null)
			params.add(PARAM_MILESTONE, milestone.getName());
		return params;
	}
	
}
