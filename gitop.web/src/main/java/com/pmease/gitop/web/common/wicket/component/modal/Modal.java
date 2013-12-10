package com.pmease.gitop.web.common.wicket.component.modal;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.pmease.gitop.web.common.util.Options;

@SuppressWarnings("serial")
public class Modal extends Panel {

	public static final String CONTENT_ID = "content";
	
	public static class DefaultModalListener implements ModalListener {

		@Override
		public void onShown(Modal modal, AjaxRequestTarget target) {
			// do nothing
		}

		@Override
		public void onHidden(Modal modal, AjaxRequestTarget target) {
			modal.setContent(new Label(CONTENT_ID, "Loading ..."));
			target.add(modal.modal);
			target.appendJavaScript("$(document.body).removeClass('modal-open')");
		}
	}
	
	private ModalListener modalListener = new DefaultModalListener();
	
	protected Component createContent(String id) {
		return new WebMarkupContainer(id).setOutputMarkupId(true);
	}
	
	private WebMarkupContainer modal;
	
	public Modal(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		modal = new WebMarkupContainer("modalspan");
		modal.setOutputMarkupId(true);
		add(modal);
		
		modal.add(createContent(CONTENT_ID));
		
		modal.add(new AjaxEventBehavior("shown.bs.modal") {

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				modalListener.onShown(Modal.this, target);
			}
			
		});
		
		modal.add(new AjaxEventBehavior("hidden.bs.modal") {

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				modalListener.onHidden(Modal.this, target);
			}
		});
	}
	
	public void show(AjaxRequestTarget target) {
		show(target, new Options());
	}
	
	public void show(AjaxRequestTarget target, Options options) {
		Preconditions.checkNotNull(options, "options");
		target.add(modal);
		target.appendJavaScript(String.format("$('#%s').modal(%s).modal('show')", 
				modal.getMarkupId(true), options.toString()));
	}
	
	public void hide(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("$('#%s').modal('hide')", 
				modal.getMarkupId()));
	}
	
	public Modal setContent(Component component) {
		Preconditions.checkState(Objects.equal(component.getId(), CONTENT_ID), 
				"component should use id 'content'");
		
		component.setOutputMarkupId(true);
		modal.addOrReplace(component);
		return this;
	}
	
	public WebMarkupContainer getModalContainer() {
		return modal;
	}
	
	public Modal setModalListener(ModalListener listener) {
		this.modalListener = listener;
		return this;
	}
}
