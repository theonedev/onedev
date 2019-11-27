package io.onedev.server.web.component.modal.confirm;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmModal extends ModalPanel {

	public ConfirmModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected Component newContent(String id) {
		return new ConfirmPanel(id) {
			
			@Override
			protected void onConfirm(AjaxRequestTarget target) {
				ConfirmModal.this.onConfirm(target);
				close();
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
			}

			@Override
			protected String getConfirmMessage() {
				return ConfirmModal.this.getConfirmMessage();
			}

			@Override
			protected String getConfirmInput() {
				return ConfirmModal.this.getConfirmInput();
			}

		};
	}

	protected abstract void onConfirm(AjaxRequestTarget target);
	
	@Nullable
	protected abstract String getConfirmInput();
	
	protected abstract String getConfirmMessage();
	
}
