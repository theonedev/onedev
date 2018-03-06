package io.onedev.server.web.component.confirmdelete;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteModal extends ModalPanel {

	public ConfirmDeleteModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected Component newContent(String id) {
		return new ConfirmDeletePanel(id) {
			
			@Override
			protected void onDelete(AjaxRequestTarget target) {
				close();
				doDelete(target);
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
			}

			@Override
			protected String getWarningMessage() {
				return ConfirmDeleteModal.this.getWarningMessage();
			}

			@Override
			protected String getConfirmInput() {
				return ConfirmDeleteModal.this.getConfirmInput();
			}
			
		};
	}

	protected abstract void doDelete(AjaxRequestTarget target);
	
	protected abstract String getConfirmInput();
	
	protected abstract String getWarningMessage();
}
