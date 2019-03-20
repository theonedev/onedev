package io.onedev.server.web.component.issue.board;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
public abstract class BoardEditPanel extends Panel {

	private final List<BoardSpec> boards;
	
	private final int boardIndex;
	
	public BoardEditPanel(String id, List<BoardSpec> boards, int boardIndex) {
		super(id);
		
		this.boards = boards;
		this.boardIndex = boardIndex;
	}

	@Nullable
	private BoardSpec getBoard(String boardName) {
		for (BoardSpec board: boards) {
			if (board.getName().equals(boardName))
				return board;
		}
		return null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BoardSpec board;
		if (boardIndex != -1)
			board = SerializationUtils.clone(boards.get(boardIndex));
		else
			board = new BoardSpec();
		
		board.getEditColumns().clear();
		for (String column: board.getColumns()) 
			board.getEditColumns().add(column!=null?column:BoardSpec.NULL_COLUMN);
		
		BeanEditor editor = BeanContext.editBean("editor", board);
		
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (boardIndex != -1) { 
					BoardSpec oldBoard = boards.get(boardIndex);
					if (!board.getName().equals(oldBoard.getName()) && getBoard(board.getName()) != null) {
						editor.getErrorContext(new PathElement.Named("name"))
								.addError("This name has already been used by another board");
					}
				} else if (getBoard(board.getName()) != null) {
					editor.getErrorContext(new PathElement.Named("name"))
							.addError("This name has already been used by another board");
				}
				if (!editor.hasErrors(true)) {
					board.getColumns().clear();
					for (String column: board.getEditColumns()) 
						board.getColumns().add(column.equals(BoardSpec.NULL_COLUMN)?null:column);
					
					if (boardIndex != -1) {
						boards.set(boardIndex, board);
					} else {
						boards.add(board);
					}
					onBoardSaved(target, board);
				} else {
					target.add(form);
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

	protected abstract void onBoardSaved(AjaxRequestTarget target, BoardSpec board);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}
