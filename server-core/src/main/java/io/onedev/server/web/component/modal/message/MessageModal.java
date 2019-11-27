package io.onedev.server.web.component.modal.message;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class MessageModal extends ModalPanel {

	public MessageModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected Component newContent(String id) {
		return new MessagePanel(id) {
			
			@Override
			protected Component newMessageContent(String componentId) {
				return MessageModal.this.newMessageContent(componentId);
			}

			@Override
			protected void close(AjaxRequestTarget target) {
				MessageModal.this.close();
			}

		};
	}

	protected abstract Component newMessageContent(String componentId);
	
}
