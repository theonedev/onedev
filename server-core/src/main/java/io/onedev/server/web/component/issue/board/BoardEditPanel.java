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
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

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
		
		board.populateEditColumns();
		
		BeanEditor editor = BeanContext.edit("editor", board);
		
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (boardIndex != -1) { 
					BoardSpec oldBoard = boards.get(boardIndex);
					if (!board.getName().equals(oldBoard.getName()) && getBoard(board.getName()) != null) {
						editor.error(new Path(new PathNode.Named("name")), 
								"This name has already been used by another board");
					}
				} else if (getBoard(board.getName()) != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another board");
				}
				if (editor.isValid()) {
					board.populateColumns();
					
					if (boardIndex != -1) {
						boards.set(boardIndex, board);
					} else {
						boards.add(board);
					}
					onSave(target, board);
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

	protected abstract void onSave(AjaxRequestTarget target, BoardSpec board);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}
