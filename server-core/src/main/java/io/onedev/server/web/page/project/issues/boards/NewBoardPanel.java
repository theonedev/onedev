package io.onedev.server.web.page.project.issues.boards;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

abstract class NewBoardPanel extends Panel {

	private final List<BoardSpec> boards;
	
	private final BoardSpec newBoard;
	
	public NewBoardPanel(String id, List<BoardSpec> boards, BoardSpec newBoard) {
		super(id);
		this.boards = boards;
		this.newBoard = newBoard;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		newBoard.populateEditColumns();
		BeanEditor editor = BeanContext.edit("editor", newBoard);
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				int indexWithSameName = BoardSpec.getBoardIndex(boards, newBoard.getName());
				if (indexWithSameName != -1) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another issue board in the project"));
				} 
				if (editor.isValid()){
					newBoard.populateColumns();					
					boards.add(newBoard);
					getProject().getIssueSetting().setBoardSpecs(boards);
					var newAuditContent = VersionedXmlDoc.fromBean(newBoard).toXML();
					OneDev.getInstance(ProjectService.class).update(getProject());
					OneDev.getInstance(AuditService.class).audit(getProject(), "created issue board \"" + newBoard.getName() + "\"", null, newAuditContent);
					Session.get().success(_T("New issue board created"));
					onBoardCreated(target, newBoard);
				} else {
					target.add(NewBoardPanel.this);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(NewBoardPanel.this);
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
	
	protected abstract void onBoardCreated(AjaxRequestTarget target, BoardSpec board);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}
