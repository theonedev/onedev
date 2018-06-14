package io.onedev.server.web.page.project.issues.issueboards;

import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class BoardEditPage extends IssuesPage {

	private static final String PARAM_BOARD = "board";
	
	private final ArrayList<IssueBoard> boards;
	
	private final int index;
	
	public BoardEditPage(PageParameters params) {
		super(params);
		
		boards = getProject().getIssueBoards();
		String name = params.get(PARAM_BOARD).toString();
		index = IssueBoard.getBoardIndex(boards, name);
		if (index == -1)
			throw new OneException("Can not find board: " + name);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueBoard board = SerializationUtils.clone(boards.get(index));
		BeanEditor editor = BeanContext.editBean("editor", board);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				int indexWithSameName = IssueBoard.getBoardIndex(boards, board.getName());
				if (indexWithSameName != -1 && indexWithSameName != index) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another issue board in the project");
				} 
				if (!editor.hasErrors(true)){
					boards.set(index, board);
					getProject().setIssueBoards(boards);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					Session.get().success("Issue board saved");
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject(), board, null, null));
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

	public static PageParameters paramsOf(Project project, IssueBoard board) {
		PageParameters params = paramsOf(project);
		params.add(PARAM_BOARD, board.getName());
		return params;
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getProject());
	}
	
}
