package io.onedev.server.web.component.beaneditmodal;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class BeanEditModalPanel extends ModalPanel {

	public BeanEditModalPanel(AjaxRequestTarget target, Serializable bean) {
		super(target, Model.of(bean));
	}
	
	@Override
	protected Component newContent(String id) {
		return new BeanEditContentPanel(id, (Serializable)getDefaultModelObject()) {
			
			@Override
			protected void onSave(AjaxRequestTarget target, Serializable bean) {
				close();
				BeanEditModalPanel.this.onSave(target, bean);
			}
			
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				close();
			}
			
		};
	}

	protected abstract void onSave(AjaxRequestTarget target, Serializable bean);
	
}
