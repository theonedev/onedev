package io.onedev.server.web.component.modal.confirm;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmModalPanel extends ModalPanel {

	public ConfirmModalPanel(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected Component newContent(String id) {
		return new ContentPanel(id) {
			
			@Override
			protected void onConfirm(AjaxRequestTarget target) {
				ConfirmModalPanel.this.onConfirm(target);
				close();
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
				ConfirmModalPanel.this.onCancel(target);
			}

			@Override
			protected String getConfirmMessage() {
				return ConfirmModalPanel.this.getConfirmMessage();
			}

			@Override
			protected String getConfirmInput() {
				return ConfirmModalPanel.this.getConfirmInput();
			}

		};
	}

	protected abstract void onConfirm(AjaxRequestTarget target);
	
	protected void onCancel(AjaxRequestTarget target) {
	}
	
	@Nullable
	protected abstract String getConfirmInput();
	
	protected abstract String getConfirmMessage();
	
}
