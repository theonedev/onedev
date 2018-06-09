package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueBoardManager;
import io.onedev.server.model.IssueBoard;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.page.project.issues.IssuesPage;

@SuppressWarnings("serial")
public class NewBoardPage extends IssuesPage {

	public NewBoardPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueBoard issueBoard = new IssueBoard();
		
		BeanEditor editor = BeanContext.editBean("editor", issueBoard);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				IssueBoardManager issueBoardManager = OneDev.getInstance(IssueBoardManager.class);
				IssueBoard issueBoardWithSameName = issueBoardManager.find(getProject(), issueBoard.getName());
				if (issueBoardWithSameName != null) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another issue board in the project");
				} 
				if (!editor.hasErrors(true)){
					issueBoard.setProject(getProject());
					issueBoardManager.save(issueBoard);
					Session.get().success("New issue board created");
					setResponsePage(IssueBoardsPage.class, IssueBoardsPage.paramsOf(getProject(), issueBoard, null));
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

}
