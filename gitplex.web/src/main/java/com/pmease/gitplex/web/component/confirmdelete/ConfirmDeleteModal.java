package com.pmease.gitplex.web.component.confirmdelete;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.behavior.modal.ModalBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteModal extends ModalPanel {

	public ConfirmDeleteModal(String id) {
		super(id);
	}

	@Override
	protected Component newContent(String id, ModalBehavior behavior) {
		final ConfirmDeleteModalBehavior confirmDeleteBehavior = (ConfirmDeleteModalBehavior) behavior;
		
		return new ConfirmDeletePanel(id) {
			
			@Override
			protected void onDelete(AjaxRequestTarget target) {
				close(target);
				doDelete(target, confirmDeleteBehavior);
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close(target);
			}

			@Override
			protected String getWarningMessage() {
				return confirmDeleteBehavior.getWarningMessage();
			}

			@Override
			protected String getConfirmInput() {
				return confirmDeleteBehavior.getConfirmInput();
			}
			
		};
	}

	protected abstract void doDelete(AjaxRequestTarget target, ConfirmDeleteModalBehavior behavior);
}
