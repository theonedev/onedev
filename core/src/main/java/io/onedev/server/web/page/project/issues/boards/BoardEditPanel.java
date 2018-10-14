package io.onedev.server.web.page.project.issues.boards;

import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
abstract class BoardEditPanel extends Panel {

	private final ArrayList<IssueBoard> boards;
	
	private final int boardIndex;
	
	public BoardEditPanel(String id, ArrayList<IssueBoard> boards, int boardIndex) {
		super(id);
		
		this.boards = boards;
		this.boardIndex = boardIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueBoard board = SerializationUtils.clone(boards.get(boardIndex));
		BeanEditor editor = BeanContext.editBean("editor", board);
		
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				int indexWithSameName = IssueBoard.getBoardIndex(boards, board.getName());
				if (indexWithSameName != -1 && indexWithSameName != boardIndex) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another issue board in the project");
				} 
				if (!editor.hasErrors(true)){
					boards.set(boardIndex, board);
					getProject().setIssueBoards(boards);
					OneDev.getInstance(ProjectManager.class).save(getProject());
					Session.get().success("Issue board saved");
					onBoardSaved(target, board);
				} else {
					target.add(BoardEditPanel.this);
				}
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(BoardEditPanel.this);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		add(form);
		
		setOutputMarkupId(true);
	}

	protected abstract Project getProject();
	
	protected abstract void onBoardSaved(AjaxRequestTarget target, IssueBoard board);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}
