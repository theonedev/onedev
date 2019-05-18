package io.onedev.server.web.component.confirmaction;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmActionModal extends ModalPanel {

	public ConfirmActionModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected Component newContent(String id) {
		return new ConfirmActionPanel(id) {
			
			@Override
			protected void onConfirm(AjaxRequestTarget target) {
				ConfirmActionModal.this.onConfirm(target);
				close();
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
				ConfirmActionModal.this.onCancel(target);
			}

			@Override
			protected String getConfirmMessage() {
				return ConfirmActionModal.this.getConfirmMessage();
			}

			@Override
			protected String getConfirmInput() {
				return ConfirmActionModal.this.getConfirmInput();
			}

		};
	}

	protected abstract void onConfirm(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	@Nullable
	protected abstract String getConfirmInput();
	
	protected abstract String getConfirmMessage();
	
}
