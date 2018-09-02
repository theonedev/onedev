package io.onedev.server.web.page.project.issues.issueboards;

import java.util.ArrayList;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class NewBoardPage extends IssuesPage {

	private final ArrayList<IssueBoard> boards;
	
	public NewBoardPage(PageParameters params) {
		super(params);
		boards = getProject().getIssueBoards();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueBoard board = new IssueBoard();
		
		BeanEditor editor = BeanContext.editBean("editor", board);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				int indexWithSameName = IssueBoard.getBoardIndex(boards, board.getName());
				if (indexWithSameName != -1) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another issue board in the project");
				} 
				if (!editor.hasErrors(true)){
					boards.add(board);
					getProject().setIssueBoards(boards);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					Session.get().success("New issue board created");
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject(), board, null, false, null, null));
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new IssueBoardsResourceReference()));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAdministrate(getProject().getFacade());
	}

}
